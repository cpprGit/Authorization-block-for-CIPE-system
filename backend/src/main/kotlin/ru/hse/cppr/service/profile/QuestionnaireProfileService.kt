package ru.hse.cppr.service.profile

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.auth0.jwt.interfaces.DecodedJWT
import com.jsoniter.any.Any
import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.SchemaType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.QuestionnaireFields
import ru.hse.cppr.logging.Log
import ru.hse.cppr.service.crud.formatted.FormattedSchemaContentService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.getFields
import java.util.*

class QuestionnaireProfileService(override val serviceName: String) : KoinComponent, ProfileService {
    private val log: Log                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    private val provider: TxProvider<ForIO>                                         by inject()
    private val formattedSchemaService: FormattedSchemaService                      by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService        by inject()

    private val questionnairesTable = Tables.QUESTIONAIRES
    private val schemaContentTable = Tables.SCHEMA_CONTENT



    override fun getProfile(id: UUID): Map<String, kotlin.Any?> {
        val program = provider.tx { configuration ->
            val questionnaire = DSL.using(configuration)
                    .select(
                            questionnairesTable.ID,
                            questionnairesTable.NAME,
                            questionnairesTable.FILL_BY,
                            questionnairesTable.IS_FILLED,
                            schemaContentTable.SCHEMA_ID,
                            schemaContentTable.CONTENT
                    )
                    .from(questionnairesTable)
                    .innerJoin(schemaContentTable)
                    .on(questionnairesTable.SCHEMA_CONTENT_ID.eq(schemaContentTable.ID))
                    .where(questionnairesTable.ID.eq(id))
                    .fetchOne()
                    .map { record ->
                        val questionnaireM = mapOf(
                                QuestionnaireFields.NAME.value to record[questionnairesTable.NAME].toString(),
                                QuestionnaireFields.FILL_BY.value to record[questionnairesTable.FILL_BY].toString(),
                                QuestionnaireFields.IS_FILLED.value to record[questionnairesTable.IS_FILLED].toString(),
                                QuestionnaireFields.CONTENT.value to record[schemaContentTable.CONTENT].toString()
                        )

                        val result = formattedSchemaService.getWithoutFieldsProgram(
                                configuration,
                                record[schemaContentTable.SCHEMA_ID]
                        ) as MutableMap

                        for (key in questionnaireM.keys) {
                            result[key] = questionnaireM[key]
                        }

                        result
                    }


            questionnaire["fields"] = getFields(configuration, SchemaType.questionnaire)

            questionnaire

        }

        return runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun getProfile(id: UUID, jwt: DecodedJWT): Map<String, kotlin.Any?> {
        return getProfile(id)
    }

    override fun editProfile(id: UUID, body: Any, jwt: DecodedJWT) {
        val program = provider.tx { configuration ->

            var schemaContentId = DSL.using(configuration)
                .selectFrom(questionnairesTable)
                .where(questionnairesTable.ID.eq(id))
                .fetchOne()
                .let { it[questionnairesTable.SCHEMA_CONTENT_ID]}

            val answer = DSL.using(configuration)
                .update(schemaContentTable)
                .set(schemaContentTable.CONTENT, body["schemaContent"].toString())
                .where(schemaContentTable.ID.eq(schemaContentId))
                .execute()

            val isFilled = DSL.using(configuration)
                .update(questionnairesTable)
                .set(questionnairesTable.IS_FILLED, true)
                .where(questionnairesTable.ID.eq(id))
                .execute()
        }


        runBlocking {
            when (val cb = Either.catch { program.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }
}