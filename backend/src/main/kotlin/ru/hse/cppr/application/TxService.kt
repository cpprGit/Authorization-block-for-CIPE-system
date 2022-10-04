package ru.hse.cppr.application

import arrow.core.Tuple2
import arrow.core.toT
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import com.jsoniter.output.JsonStream
import io.undertow.Handlers
import io.undertow.server.HttpHandler
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import ru.hse.cppr.utils.body
import ru.hse.cppr.utils.status
import java.lang.IllegalArgumentException

class TxService(val provider: TxProvider<ForIO>) {

    fun servlet(): HttpHandler = Handlers.routing()
        .post("/api/v1/database/") { exchange ->
            runBlocking {
                val bodyJson        = JsonIterator.deserialize(exchange.body())
                val schemaJson      = bodyJson["schema"]
                val valuesJson      = bodyJson["values"]
                val (table, schema) = schemaParse(schemaJson)

                val tx = provider.tx { configuration ->
                    tableCreate(DSL.using(configuration), table, schema)
                    for (valueJson in valuesJson) {
                        valuesInsert(DSL.using(configuration), table, schema, valueJson)
                    }
                }

                try {
                    tx.fix().unsafeRunSync()

                    exchange.status(204)
                        .endExchange()
                } catch (t: Throwable) {
                    t.printStackTrace()

                    exchange.status(500)
                        .body(JsonStream.serialize(Any.wrap(mapOf("error" to "Internal server error; ${t.stackTrace.toString()}"))))
                        .endExchange()
                }
            }
        }
        .get("/api/v1/database/") { exchange ->
            exchange.status(200)
                .body(JsonStream.serialize(Any.wrap("message" to "ok")))
                .endExchange()
        }

    private fun schemaParse(schemaJson: Any): Tuple2<String, List<TxValue<*>>> {
        val fieldsJson = schemaJson["fields"]

        return schemaJson["table"].toString() toT fieldsJson.keys().map { name ->
            val nameString = name.toString()
            TxValue(nameString, fieldsJson[nameString])
        }
    }

    private fun tableCreate(ctx: DSLContext, table: String, schema: List<TxValue<*>>): Int {

        return ctx.createTableIfNotExists(table)
            .columns(schema.map { field -> field.column })
            .let { step ->
                val pks = schema.filter { field -> field.primaryKey }
                if (pks.isNotEmpty()) {
                    step.constraint(DSL.primaryKey(* pks.map { pkField -> pkField.column }.toTypedArray()))
                } else {
                    step
                }
            }
            .execute()
    }

    private fun valuesInsert(ctx: DSLContext, table: String, schema: List<TxValue<*>>, valuesJson: Any): Int {

        return ctx.insertInto(DSL.table(table))
            .columns(schema.map { field -> field.column })
            .values (schema.map { field -> field.extract(valuesJson) })
            .let { step ->
                val pks    = schema.filter    { field -> field.primaryKey }
                val values = schema.filterNot { field -> pks.contains(field) }
                    .map { field -> field.column to field.extract(valuesJson) }
                    .toMap()

                if (pks.isNotEmpty()) {
                    step.onConflict(pks.map { pkField -> pkField.column })
                        .doUpdate()
                        .set(values)
                } else {
                    step
                }
            }
            .execute()
    }
}

sealed class TxValue<A>(val type: String, open val primaryKey: Boolean, open val name: String) {

    companion object {

        @JvmStatic operator fun invoke(name: String, json: Any): TxValue<*> {
            val type       = json["type" ]      .toString()
            val primaryKey = json["primary_key"].toBoolean()

            return when (type) {
                "BIGINT"  -> TxLong   (primaryKey, name)
                "TEXT"    -> TxText   (primaryKey, name)
                "REAL"    -> TxReal   (primaryKey, name)
                "BOOLEAN" -> TxBoolean(primaryKey, name)
                else      -> throw IllegalArgumentException("Unknown type: $type; name: $name")
            }
        }
    }


    abstract val column: Field<A>

    abstract fun parse(a: Any): A


    fun extract(a: Any): A {

        return parse(a[name]["value"])
    }
}

data class TxLong(override val primaryKey: Boolean, override val name: String)
    : TxValue<Long>(type = "BIGINT", primaryKey = primaryKey, name = name) {

    override val column: Field<Long>
        get() = DSL.field(name, Long::class.java)


    override fun parse(a: Any): Long
            = a.toLong()
}

data class TxText(override val primaryKey: Boolean, override val name: String)
    : TxValue<String>(type = "TEXT", primaryKey = primaryKey, name = name) {

    override val column: Field<String>
        get() = DSL.field(name, String::class.java)


    override fun parse(a: Any): String
            = a.toString()
}

data class TxReal(override val primaryKey: Boolean, override val name: String)
    : TxValue<Double>(type = "REAL", primaryKey = primaryKey, name = name) {

    override val column: Field<Double>
        get() = DSL.field(name, Double::class.java)


    override fun parse(a: Any): Double
            = a.toDouble()
}

data class TxBoolean(override val primaryKey: Boolean, override val name: String)
    : TxValue<Boolean>(type = "BOOLEAN", primaryKey = primaryKey, name = name) {

    override val column: Field<Boolean>
        get() = DSL.field(name, Boolean::class.java)


    override fun parse(a: Any): Boolean
            = a.toBoolean()
}
