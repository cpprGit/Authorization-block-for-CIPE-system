package ru.hse.cppr.service

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.impl.DSL.using
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.UserType
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields


object SearchUtils : KoinComponent {

    private val provider: TxProvider<ForIO>                                   by inject()

    private val users = Tables.USERS
    private val organisations = Tables.ORGANISATIONS


    fun searchOrganisationsByName(name: String?): MutableList<Map<String, String>> {
        val orgsM = provider.tx { configuration ->
            using(configuration)
                .selectFrom(organisations)
                .where(organisations.NAME.containsIgnoreCase(name))
                .fetch()
                .fold(mutableListOf<Map<String, String>>()) { list, record ->
                    list.add(
                        mapOf(
                            CommonFields.ID.value to record[organisations.ID].toString(),
                            CommonFields.NAME.value to record[organisations.NAME].toString()
                        )
                    )

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { orgsM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    fun searchMentorsByName(mentorName: String?): MutableList<Map<String, String>> {
        val mentorsM = provider.tx {
            configuration ->

            using(configuration)
                .select(users.NAME, users.ID)
                .from(users)
                .where(users.NAME.contains(mentorName).and(users.TYPE.eq(UserType.mentor)))
                .fetch()
                .fold (mutableListOf<Map<String, String>>()) {list, record ->
                    list.add(mapOf(
                        CommonFields.ID.value to record[users.ID].toString(),
                        CommonFields.NAME.value to record[users.NAME].toString()
                    ))

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { mentorsM.fix().unsafeRunSync() }) {
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
