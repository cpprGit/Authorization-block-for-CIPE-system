package ru.hse.cppr.service.crud

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.jsoniter.output.JsonStream
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.formats.MailGroupFormat
import ru.hse.cppr.logging.Log
import java.util.*

class MailGroupsService(override val serviceName: String) : KoinComponent, CRUDService {

    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }

    val table = Tables.MAIL_GROUP

    val COLUMNS = arrayListOf(
        table.NAME,
        table.CREATED_BY
    )
    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            table.ID.name to record[table.ID]?.toString(),
            table.NAME.name to record[table.NAME]?.toString(),
            table.CREATED_BY.name to record[table.CREATED_BY]?.toString()
        )
    }

    override fun create(body: com.jsoniter.any.Any): Map<String, Any?> {
        val mailGroupM = provider.tx { configuration ->
            createMailGroupProgram(MailGroupFormat(body), configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { mailGroupM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun list(vararg params: String): MutableList<Map<String, Any?>> {
        val mailGroupsM = provider.tx { configuration ->
            listMailGroupsProgram(configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { mailGroupsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun get(id: UUID): Map<String, Any?> {
        val mailGroupM = provider.tx { configuration ->
            getMailGroupProgram(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { mailGroupM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun update(id: UUID, body: com.jsoniter.any.Any): String {
        val mailGroupM = provider.tx { configuration ->
            updateMailGroup(id, MailGroupFormat(body), configuration)
        }

        val mailGroup = runBlocking {
            when (val cb = Either.catch { mailGroupM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return JsonStream.serialize(com.jsoniter.any.Any.wrap(mailGroup))
    }

    override fun delete(id: UUID): String {
        val mailGroupM = provider.tx { configuration ->
            deleteMailGroupProgram(id, configuration)
        }

        val mailGroup = runBlocking {
            when (val cb = Either.catch { mailGroupM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return JsonStream.serialize(com.jsoniter.any.Any.wrap(mailGroup))
    }

    fun createMailGroupProgram(body: MailGroupFormat, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .insertInto(table)
            .columns(COLUMNS)
            .values(
                body.getName(),
                body.getCreatedBy()
            )
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let { fetchMapValues(it) }
    }

    fun listMailGroupsProgram(configuration: Configuration): MutableList<Map<String, Any?>> {
        return DSL.using(configuration)
            .selectFrom(table)
            .fetch()
            .fold(mutableListOf()) { list, record ->
                list.add(fetchMapValues(record))

                list
            }

    }

    fun getMailGroupProgram(id: UUID, configuration: Configuration): Map<String, Any?> {
        return DSL.using(configuration)
            .selectFrom(table)
            .where(table.ID.eq(id))
            .fetchOne()
            .let { fetchMapValues(it) }
    }

    fun updateMailGroup(id: UUID, body: MailGroupFormat, configuration: Configuration): Map<String, kotlin.Any?> {
        return DSL.using(configuration)
            .update(table)
            .set(table.NAME, body.getName())
            .where(table.ID.eq(id))
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .map { fetchMapValues(it) }
    }

    fun  deleteMailGroupProgram(id: UUID, configuration: Configuration): Map<String, kotlin.Any?> {
        return DSL.using(configuration)
            .deleteFrom(table)
            .where(table.ID.eq(id))
            .returning(COLUMNS_WITH_ID)
            .fetchOne()
            .let {fetchMapValues(it)}
    }

}