package ru.hse.cppr.service.posts

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import com.auth0.jwt.interfaces.DecodedJWT
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import kotlinx.coroutines.runBlocking
import org.jooq.*
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.tables.records.PostsRecord
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.PostFields
import ru.hse.cppr.representation.formats.PostFormat
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.security.*
import ru.hse.cppr.service.file.FileService
import java.util.*

class PostsServiceImpl(override val serviceName: String) : KoinComponent, PostsService {
    private val provider: TxProvider<ForIO>             by inject()
    private val log: Log                                by inject() { parametersOf(CommandLineApplicationRuntime::class) }
    private val fileService: FileService                by inject()

    private val userProfileSecurityProvider = UserProfileSecurityProvider()
    private val postsSecurityProvider = PostsSecurityProvider()

    private val table = Tables.POSTS
    private val usersTable = Tables.USERS

    val COLUMNS = arrayListOf(
        table.CREATED_BY,
        table.PROFILE_ID,
        table.MESSAGE,
        table.TEXT,
        table.FILE_ID
    )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, table.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            PostFields.ID.value to record[table.ID]?.toString(),
            PostFields.CREATED_BY.value to record[table.CREATED_BY]?.toString(),
            PostFields.PROFILE.value to record[table.PROFILE_ID]?.toString(),
            PostFields.MESSAGE.value to record[table.MESSAGE]?.toString(),
            PostFields.DATE_CREATED.value to record[table.DATE_CREATED]?.toString(),
            PostFields.TEXT.value to record[table.TEXT]?.toString(),
            PostFields.FILE.value to record[table.FILE_ID]?.toString()
        )
    }

    override fun createPost(profileId: UUID, body: com.jsoniter.any.Any, jwt: DecodedJWT): Map<String, Any?> {
        checkModifyAllowed(profileId, body[CommonFields.PROFILE_TYPE.value].toString(), jwt)

        val bodyFormat = PostFormat(body)

        val postM = provider.tx { configuration ->
            DSL.using(configuration)
                .insertInto(table)
                .columns(COLUMNS)
                .values(
                    bodyFormat.getCreatedBy(),
                    profileId,
                    bodyFormat.getMessage(),
                    bodyFormat.getText(),
                    bodyFormat.getFile()
                )
                .returning(COLUMNS_WITH_ID)
                .fetchOne()
                .map { fetchMapValues(it) }
        }

        return runBlocking {
            when (val cb = Either.catch { postM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

    }

    override fun getPostsByProfileId(id: UUID): MutableList<Map<String, Any?>> {
        val postM = provider.tx { configuration ->
            getPostsByProfileIdProgram(id, configuration)
        }


        return runBlocking {
            when (val cb = Either.catch { postM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun getPostsByProfileIdProgram(id: UUID, configuration: Configuration): MutableList<Map<String, Any?>> {
        val posts = DSL.using(configuration)
            .selectFrom(table)
            .where(table.PROFILE_ID.eq(id))
            .orderBy(table.DATE_CREATED.asc())
            .fetch()
            .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                list.add(fetchMapValues(record))
                list
            }

        val result = mutableListOf<Map<String, Any?>>()

        for (post in posts) {
            val postInfo = mutableMapOf<String, Any?>()
            postInfo[PostFields.ID.value] = post[PostFields.ID.value].toString()
            postInfo[PostFields.DATE_CREATED.value] = post[PostFields.DATE_CREATED.value].toString()
            postInfo[PostFields.MESSAGE.value] = post[PostFields.MESSAGE.value]
            postInfo[PostFields.TEXT.value] = post[PostFields.TEXT.value]

            val createdById = UUID.fromString(post[PostFields.CREATED_BY.value].toString())

            val userName =
                DSL.using(configuration)
                    .selectFrom(Tables.USERS)
                    .where(Tables.USERS.ID.eq(createdById))
                    .fetchOne()
                    .map { it[Tables.USERS.NAME] }

            postInfo[PostFields.CREATED_BY.value] = mapOf(
                CommonFields.ID.value to createdById.toString(),
                CommonFields.NAME.value to userName,
                CommonFields.TYPE.value to ProfileTypes.USER.value
            )


            if (post[PostFields.FILE.value] != null) {
                val fileId = UUID.fromString(post[PostFields.FILE.value].toString())

                postInfo[PostFields.FILE.value] =
                    DSL.using(configuration)
                        .selectFrom(Tables.FILES)
                        .where(Tables.FILES.ID.eq(fileId))
                        .fetchOne()
                        .map {
                            mapOf(
                                CommonFields.ID.value to it[Tables.FILES.ID].toString(),
                                CommonFields.NAME.value to it[Tables.FILES.NAME],
                                CommonFields.TYPE.value to ProfileTypes.FILE.value
                            )
                        }
            } else {
                postInfo[PostFields.FILE.value] = null
            }

            result.add(postInfo)
        }

        return result
    }


    override fun updatePost(id: UUID, body: com.jsoniter.any.Any, jwt: DecodedJWT): String {
        postsSecurityProvider.checkModifyAllowed(id, jwt)
        val bodyFormat = PostFormat(body)

        val postM = provider.tx { configuration ->
            var fileToDelete: UUID? = null
            val updateStatement = body.keys().fold<kotlin.Any?, UpdateSetStep<PostsRecord>>(
                DSL.using(configuration).update(table)
            ) { statement, key ->
                when (val key = key.toString()) {
                    PostFields.TEXT.value ->
                        statement.set(table.TEXT, bodyFormat.getText())
                    PostFields.FILE.value -> {

                        val fileId =
                            DSL.using(configuration)
                                .selectFrom(table)
                                .where(table.ID.eq(id))
                                .fetchOne()
                                .map { it[table.FILE_ID] }

                        val step = statement.set(table.FILE_ID, bodyFormat.getFile())

                        if (bodyFormat.getFile() != fileId) {
                            fileToDelete = fileId
                        }

                        step
                    }
                    else ->
                        statement
                }
            }

            when (updateStatement) {
                is UpdateSetMoreStep -> {
                    val optional = updateStatement
                        .where(table.ID.eq(id))
                        .returning(COLUMNS_WITH_ID)
                        .fetchOptional()
                        .map { record ->
                            fetchMapValues(record)
                        }

                    if (fileToDelete != null) {
                        fileService.deleteFile(fileToDelete!!, configuration)
                    }

                    optional
                }

                is UpdateSetFirstStep ->
                    Optional.empty()

                else ->
                    Optional.empty()
            }
        }

        val post = runBlocking {
            when (val cb = Either.catch { postM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    log.e("Unable to update post.")
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        when (post.isPresent) {
            false -> throw BadRequestException("Post with id=$id not found.")
            true -> return JsonStream.serialize(com.jsoniter.any.Any.wrap(post.get()))
        }
    }

    override fun deletePost(id: UUID, jwt: DecodedJWT): String {
        postsSecurityProvider.checkModifyAllowed(id, jwt)

        val postM = provider.tx { configuration ->
            val fileId =
                DSL.using(configuration)
                    .selectFrom(table)
                    .where(table.ID.eq(id))
                    .fetchOne()
                    .map { it[table.FILE_ID] }

            DSL.using(configuration)
                .deleteFrom(table)
                .where(table.ID.eq(id))
                .execute()

            if (fileId != null) {
                fileService.deleteFile(fileId, configuration)
            }
        }

        runBlocking {
            when (val cb = Either.catch { postM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

        return "{ \"status\": \"Success\"}"
    }


    private fun checkModifyAllowed(profileId: UUID, profileType: String, jwt: DecodedJWT) {
        val securityProvider =  when (profileType) {
            ProfileTypes.USER.value -> UserProfileSecurityProvider()
            ProfileTypes.PROJECT.value -> ProjectProfileSecurityProvider()
            ProfileTypes.PROJECT_REQUEST.value -> ProjectRequestProfileSecurityProvider()
            ProfileTypes.ORGANISATION.value -> OrganisationProfileSecurityProvider()
            ProfileTypes.ACTIVITY.value -> ActivityProfileSecurityProvider()
            else -> throw BadRequestException("No such type: $profileType")
        }

        securityProvider.checkModifyAllowed(getEntityId(profileId, profileType), jwt)
    }

    private fun getEntityId(profileId: UUID, profileType: String): UUID {
        val txResult = provider.tx { configuration ->
            when (profileType) {
                ProfileTypes.USER.value -> {
                    DSL.using(configuration)
                        .selectFrom(Tables.USERS)
                        .where(Tables.USERS.SCHEMA_CONTENT_ID.eq(profileId))
                        .fetchOne()
                        .map { it[Tables.USERS.ID] }
                }
                ProfileTypes.PROJECT.value -> {
                    DSL.using(configuration)
                        .selectFrom(Tables.PROJECTS)
                        .where(Tables.PROJECTS.SCHEMA_CONTENT_ID.eq(profileId))
                        .fetchOne()
                        .map { it[Tables.PROJECTS.ID] }
                }
                ProfileTypes.PROJECT_REQUEST.value -> {
                    DSL.using(configuration)
                        .selectFrom(Tables.PROJECT_REQUESTS)
                        .where(Tables.PROJECT_REQUESTS.SCHEMA_CONTENT_ID.eq(profileId))
                        .fetchOne()
                        .map { it[Tables.PROJECT_REQUESTS.ID] }
                }
                ProfileTypes.ORGANISATION.value -> {
                    DSL.using(configuration)
                        .selectFrom(Tables.ORGANISATIONS)
                        .where(Tables.ORGANISATIONS.SCHEMA_CONTENT_ID.eq(profileId))
                        .fetchOne()
                        .map { it[Tables.ORGANISATIONS.ID] }
                }
                ProfileTypes.ACTIVITY.value -> {
                    DSL.using(configuration)
                        .selectFrom(Tables.ACTIVITY)
                        .where(Tables.ACTIVITY.SCHEMA_CONTENT_ID.eq(profileId))
                        .fetchOne()
                        .map { it[Tables.ACTIVITY.ID] }
                }
                else -> throw BadRequestException("No such type: $profileType")
            }
        }

        return runBlocking {
            when (val cb = Either.catch { txResult.fix().unsafeRunSync() }) {
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