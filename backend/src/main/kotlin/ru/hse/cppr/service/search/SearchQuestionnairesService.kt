package ru.hse.cppr.service.search

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL.using
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.QuestionnaireFields
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import ru.hse.cppr.utils.convertIsQuestionnaireFilled
import java.util.*
import kotlin.collections.HashMap

class SearchQuestionnairesService(override val serviceName: String) : KoinComponent, BaseSearchService() {

    private val formattedSchemaService: FormattedSchemaService                by inject()
    private val provider: TxProvider<ForIO>                                   by inject()


    private val questionnairesTable = Tables.QUESTIONAIRES
    private val users = Tables.USERS


    override fun search(params: Map<String, Deque<String>>): Map<String, Any>? {
        val filterParams = params["filter_params"]?.first()
        val name = params["name"]?.first()
        val schemaId = params["schemaId"]?.first()

        val questionnairesM = provider.tx { configuration ->
            val formattedSchema = formattedSchemaService.getFormattedSchemaProgram(
                configuration,
                UUID.fromString(schemaId)
            )

            val questionnaires = using(configuration)
                .select()
                .from(questionnairesTable)
                .innerJoin(schemaContent)
                .on(questionnairesTable.SCHEMA_CONTENT_ID.eq(schemaContent.ID))
                .innerJoin(users)
                .on(questionnairesTable.FILL_BY.eq(users.ID))
                .where(schemaContent.SCHEMA_ID.eq(UUID.fromString(schemaId)))
                .fetch()
                .fold(mutableListOf<Map<String, Any?>>()) { list, row ->

                    if (satisfiesFilterParamSearch(filterParams, row)) {
                        list.add(
                            mapOf(
                                QuestionnaireFields.ID.value to row[questionnairesTable.ID].toString(),
                                QuestionnaireFields.NAME.value to row[questionnairesTable.NAME].toString(),
                                QuestionnaireFields.IS_FILLED.value to convertIsQuestionnaireFilled(row[questionnairesTable.IS_FILLED]),
                                QuestionnaireFields.FILL_BY.value to mapOf(
                                    CommonFields.ID.value to row[questionnairesTable.FILL_BY].toString(),
                                    CommonFields.NAME.value to row[users.NAME].toString(),
                                    "type" to "user"
                                ),
                                CommonFields.SCHEMA_ID.value to row[schemaContent.SCHEMA_ID].toString(),
                                CommonFields.SCHEMA_CONTENT.value to row[schemaContent.CONTENT].toString()
                            )
                        )
                    }

                    list
                }

            val result = HashMap<String, Any>()

            result[CommonFields.SCHEMA.value] = formattedSchema
            result[CommonFields.RECORDS.value] = questionnaires

            result
        }

        return runBlocking {
            when (val cb = Either.catch { questionnairesM.fix().unsafeRunSync() }) {
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