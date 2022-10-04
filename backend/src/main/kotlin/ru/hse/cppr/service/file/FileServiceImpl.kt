package ru.hse.cppr.service.file

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.FileFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.service.crud.formatted.FormattedSchemaService
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*

class FileServiceImpl(override val serviceName: String) : FileService, KoinComponent {
    private val provider: TxProvider<ForIO>                                   by inject()

    private val filesTable = Tables.FILES

    private val tempFilesDirectory = "temp-files/"
    private val storedFilesDirectory = "stored-files/"

    override fun saveFile(requestBody: ByteArray, taskId: UUID?, uploaderId: UUID?): String {

        //TODO: check if both profileId and taskId == null, then throw exception

        //Read contents of a sent body into a temp file
        val file = File("${tempFilesDirectory}TEMP-${UUID.randomUUID()}")

        val outputStream = FileOutputStream(file)
        outputStream.write(requestBody)
        outputStream.close()

        // Count the size of the descriptive header in bytes
        // so we can delete it with byte precision
        // in order not to corrupt the file's contents.

        // Descriptive header example:

        //        ------WebKitFormBoundaryGueeWtJyeb6puGlR
        //        Content-Disposition: form-data; name="name"
        //
        //        Screenshot 2020-05-08 at 18.42.33.png
        //        ------WebKitFormBoundaryGueeWtJyeb6puGlR
        //        Content-Disposition: form-data; name="file"; filename="Screenshot.png"
        //        Content-Type: image/png

        val carriageReturnKey = 13
        val numberOfLinesInDescriptiveHeader = 8

        var index = 0
        var byteSize = 0
        while(index < numberOfLinesInDescriptiveHeader) {
            if (requestBody[byteSize].toInt() == carriageReturnKey) {
                index++
            }
            byteSize++
        }

        // Read saved file line by line so we can get file's meta-info from
        // it's descriptive header.
        var fileName = "null"
        var contentType = "null"
        var boundary = "null"

        val boundaryValueLineNumber = 0
        val fileNameValueLineNumber = 3
        val contentTypeValueLineNumber = 6

        val fileReader = FileReader(file)
        val bufferedReader = BufferedReader(fileReader)
        var line = bufferedReader.readLine()

        for (i in 0..7) {
            when (i) {
                boundaryValueLineNumber -> boundary = line
                fileNameValueLineNumber -> fileName = line
                contentTypeValueLineNumber -> contentType = line.split(" ")[1]
            }
            line = bufferedReader.readLine()
        }

        fileReader.close()


        val content = requestBody.asList().subList(byteSize + 1, requestBody.size - 1 - boundary.length - 3).toByteArray()

        val fileLocation = saveFileInfoToDB(fileName, contentType, taskId, uploaderId)

        val fileOutputStream = FileOutputStream(File("$storedFilesDirectory${fileLocation.replace("-", "")}"))
        fileOutputStream.write(content)
        fileOutputStream.close()

        // Delete Temp file
        file.delete()

        return fileLocation
    }

    fun saveFileInfoToDB(fileName: String, contentType: String,  taskId: UUID?, uploaderId: UUID?): String {
        val fileM = provider.tx { configuration ->
            val fileId = DSL.using(configuration)
                .insertInto(filesTable)
                .columns(
                    filesTable.NAME,
                    filesTable.MIMETYPE,
                    filesTable.TASK_ID,
                    filesTable.UPLOADER_ID
                )
                .values(fileName, contentType, taskId, uploaderId)
                .returning(filesTable.ID)
                .fetchOne()
                .let { it[filesTable.ID] }

            val location = fileId.toString().replace("-", "")

            DSL.using(configuration)
                .update(filesTable)
                .set(filesTable.LOCATION, location)
                .where(filesTable.ID.eq(fileId))
                .execute()

            fileId.toString()
        }


        return runBlocking {
            when (val cb = Either.catch { fileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun downloadFile(fileId: UUID): Map<String, String>? {
        val fileM = provider.tx { configuration ->
            DSL.using(configuration)
                .selectFrom(filesTable)
                .where(filesTable.ID.eq(fileId))
                .fetchOne()
                .map {
                    mapOf(
                        "fileName" to it[filesTable.NAME],
                        "location" to storedFilesDirectory + it[filesTable.LOCATION],
                        "mimetype" to it[filesTable.MIMETYPE]
                    )
                }
        }

        return runBlocking {
            when (val cb = Either.catch { fileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    override fun getFileForTask(id: UUID): Map<String, Any>? {
        val fileM = provider.tx { configuration ->
            val record = DSL.using(configuration)
                .selectFrom(filesTable)
                .where(filesTable.TASK_ID.eq(id))
                .fetchOne()

            when (record) {
                null -> null
                else -> record.map {
                    mapOf(
                        FileFields.ID.value to it[filesTable.ID].toString(),
                        FileFields.NAME.value to it[filesTable.NAME]
                    )
                }
            }

        }

        return runBlocking {
            when (val cb = Either.catch { fileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

    }

    override fun getFileForTaskAndUser(taskId: UUID, userId: UUID): Map<String, Any>? {
        val fileM = provider.tx { configuration ->
            val record = DSL.using(configuration)
                .selectFrom(filesTable)
                .where(filesTable.TASK_ID.eq(taskId)
                    .and(filesTable.UPLOADER_ID.eq(userId)))
                .fetchOne()

            when (record) {
                null -> null
                else -> record.map {
                    mapOf(
                        CommonFields.ID.value to it[filesTable.ID].toString(),
                        CommonFields.NAME.value to it[filesTable.NAME],
                        CommonFields.TYPE.value to ProfileTypes.FILE.value
                    )
                }
            }

        }

        return runBlocking {
            when (val cb = Either.catch { fileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }

    override fun getFilesForProfile(id: UUID): MutableList<Map<String, Any>> {
        val fileM = provider.tx { configuration ->
            DSL.using(configuration)
                .selectFrom(filesTable)
                .where(filesTable.PROFILE_ID.eq(id))
                .fetch()
                .fold (mutableListOf<Map<String, Any>>()) {list, record ->
                    list.add(mapOf(
                        FileFields.ID.value to record[filesTable.ID],
                        FileFields.NAME.value to record[filesTable.NAME]
                    ))

                    list
                }
        }

        return runBlocking {
            when (val cb = Either.catch { fileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }

    }

    override fun deleteFile(id: UUID, configuration: Configuration): Map<String, Any> {
        val fileLocation =
            DSL.using(configuration)
                .selectFrom(filesTable)
                .where(filesTable.ID.eq(id))
                .fetchOne()
                .map { it[filesTable.LOCATION]}

        val file = File("$storedFilesDirectory$fileLocation")
        file.delete()

        return DSL.using(configuration)
            .deleteFrom(filesTable)
            .where(filesTable.ID.eq(id))
            .returning()
            .fetchOne()
            .map { record -> mapOf(
                FileFields.ID.value to record[filesTable.ID],
                FileFields.NAME.value to record[filesTable.NAME]
            ) }
    }

    override fun deleteFile(id: UUID): Map<String, Any> {
        val fileM = provider.tx { configuration ->
            deleteFile(id, configuration)
        }

        return runBlocking {
            when (val cb = Either.catch { fileM.fix().unsafeRunSync() }) {
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