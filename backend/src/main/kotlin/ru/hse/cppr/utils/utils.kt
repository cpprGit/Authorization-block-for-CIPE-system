package ru.hse.cppr.utils

import com.auth0.jwt.interfaces.DecodedJWT
import com.jsoniter.JsonIterator
import com.jsoniter.output.JsonStream
import io.undertow.util.BadRequestException
import org.apache.poi.hssf.usermodel.HeaderFooter.file
import org.jooq.Configuration
import org.jooq.impl.DSL
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.data.database_generated.enums.*
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.security.JwtClaims
import ru.hse.cppr.service.DefaultAttributesService
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


fun getFields(configuration: Configuration, schemaType: SchemaType): MutableList<Map<String, Any>>  {

    if (schemaType == SchemaType.student_profile) {
        return (getFields(configuration, SchemaType.student_registration) +
                getFields(configuration, SchemaType.student_profile_template)) as MutableList<Map<String, Any>>
    }

    val attributes = DefaultAttributesService.getDefaultAttributesForSchemaTypeProgram(
        schemaType.toString(),
        configuration
    )

    val result = mutableListOf<Map<String, Any>>()

    for (attribute in attributes) {
        result.add(mapOf(
            "name" to attribute["searchName"].toString(),
            "title" to attribute["title"].toString(),
            "attribute" to attribute
        ))
    }

    return result
}


fun convertUserRole(role: String?): UserType {
    return when(role) {
        UserRoles.STUDENT.value -> UserType.student
        UserRoles.MENTOR.value -> UserType.mentor
        UserRoles.REPRESENTATIVE.value -> UserType.representative
        UserRoles.MANAGER.value -> UserType.manager
        UserRoles.ADMIN.value -> UserType.administrator
        UserRoles.SUPERVISOR.value -> UserType.supervisor
        else -> UserType.student
    }
}


fun convertProjectType(type: String?): ProjectType {
    return when (type) {
        "Исследовательский" -> ProjectType.research
        "Программный" -> ProjectType.technical
        else -> ProjectType.other
    }
}

fun convertProjectType(type: Any?): String {
    if (type == null)
        return "Не определено"

    return when (ProjectType.valueOf(type.toString())) {
        ProjectType.research -> "Исследовательский"
        ProjectType.technical -> "Программный"
        else -> "Не определено"
    }
}

fun convertProjectIndividuality(is_group_project: String?): Boolean {
    return when (is_group_project) {
        "Групповой" -> true
        else -> false
    }
}


fun convertProjectIndividuality(is_group_project: Any?): String {
    if (is_group_project == null)
        return "Не определено"

    return when (is_group_project as Boolean) {
        true -> "Групповой"
        else -> "Индивидуальный"
    }
}


fun convertIsQuestionnaireFilled(is_filled: Any?): String {
    if (is_filled == null)
        return "Не определено"

    return when (is_filled as Boolean) {
        true -> "Заполнена"
        else -> "Не заполнена"
    }
}


fun convertProjectRequestStatus(status: String?) : ProjectRequestStatus {
    if (status == null) {
        throw BadRequestException("No such status: $status")
    }

    return when(status) {
        "На рассмотрении" -> ProjectRequestStatus.pending
        "Отклонена" -> ProjectRequestStatus.declined
        "Принята" -> ProjectRequestStatus.accepted
        else -> ProjectRequestStatus.pending
    }

}

fun convertProjectRequestStatus(status: ProjectRequestStatus?): String {
    if(status == null) {
        return "Не определено"
    }

    return when(status) {
        ProjectRequestStatus.pending -> "На рассмотрении"
        ProjectRequestStatus.declined -> "Отклонена"
        ProjectRequestStatus.accepted -> "Принята"
        else -> "Не определено"
    }

}

fun convertBlockedStatus(status: String?) : BlockedStatus {
    if (status == null) {
        throw BadRequestException("No such status: $status")
    }

    return when(status) {
        "Активный" -> BlockedStatus.active
        "Заблокированный" -> BlockedStatus.blocked
        else -> BlockedStatus.active
    }
}


fun convertBlockedStatus(status: BlockedStatus?) : Boolean? {
    if(status == null) {
        return null
    }

    return when(status) {
        BlockedStatus.active -> false
        BlockedStatus.blocked -> true
    }
}

fun convertBlockedStatus(status: Boolean) : BlockedStatus {
    return when (status) {
        false -> BlockedStatus.active
        else -> BlockedStatus.blocked
    }
}


fun convertStudentStatus(status: String?) : StudentStatus {
    if (status == null) {
        throw BadRequestException("No such status: $status")
    }

    return when(status) {
        "Обучающийся" -> StudentStatus.active
        "Выпускник" -> StudentStatus.graduated
        "Поступающий" -> StudentStatus.enrolling
        else -> StudentStatus.inactive
    }
}


fun convertStudentStatus(status: StudentStatus?) : String {
    if(status == null) {
        return "Не определено"
    }

    return when(status) {
        StudentStatus.active -> "Обучающийся"
        StudentStatus.graduated -> "Выпускник"
        StudentStatus.enrolling -> "Поступающий"
        StudentStatus.inactive ->  "Отчисленный"
    }
}

fun convertFaculty(faculty: FacultyType?): String {
    if (faculty == null)
        return "Не определено"

    return when (faculty) {
        FacultyType.PAD -> "Прикладной Анализ Данных"
        FacultyType.PMI -> "Прикладная Математика и Информатика"
        FacultyType.PI -> "Программная Инженерия"
        else -> "Не определено"
    }
}

fun convertFaculty(faculty: String?): FacultyType {
    if (faculty == null)
        throw BadRequestException("No such faculty: $faculty")

    return when (faculty) {
        "Прикладной Анализ Данных" -> FacultyType.PAD
        "Прикладная Математика и Информатика" -> FacultyType.PMI
        "Программная Инженерия" -> FacultyType.PI
        else -> throw BadRequestException("No such faculty: $faculty")
    }
}


fun convertIsHseDepartment(isHseDep: Boolean): String {
    return when(isHseDep) {
        true -> "Департамент НИУ ВШЭ"
        else -> "Организация"
    }
}

fun convertIsHseDepartment(isHseDep: String): Boolean {
    return when (isHseDep) {
        "Департамент НИУ ВШЭ" -> true
        else -> false
    }
}

fun convertActivityStatus(activityStatus: String?): ActivityStatus {
    return when(activityStatus) {
        "Ожидает начала" -> ActivityStatus.not_started
        "Доступна подача заявок" -> ActivityStatus.apply_open
        "Выполняется" -> ActivityStatus.started
        "Завершена" -> ActivityStatus.finished
        else -> ActivityStatus.not_started
    }
}

fun convertActivityStatus(activityStatus: ActivityStatus?): String {
    return when(activityStatus) {
        ActivityStatus.started -> "Выполняется"
        ActivityStatus.finished -> "Завершена"
        ActivityStatus.apply_open -> "Доступна подача заявок"
        else -> "Ожидает начала"
    }
}


fun stringOrEmpty(str: String?): String {
    return if (str == null || str == "null")
        ""
    else str
}

fun mergeMaps(mapOne: MutableMap<String, kotlin.Any?>, mapTwo: Map<String, kotlin.Any?>): MutableMap<String, kotlin.Any?> {
    for (key in mapTwo.keys) {
        mapOne[key] = mapTwo[key]
    }

    return mapOne
}

fun getUser(id: UUID?, configuration: Configuration): Map<String, Any>? {
    if (id == null) {
        return null
        return mapOf(
            CommonFields.NAME.value to ""
        )
    }

    return DSL.using(configuration)
        .selectFrom(Tables.USERS)
        .where(Tables.USERS.ID.eq(id))
        .fetchOne()
        .map { record ->
            mapOf(
                CommonFields.ID.value to record[Tables.USERS.ID].toString(),
                CommonFields.NAME.value to record[Tables.USERS.NAME].toString(),
                CommonFields.TYPE.value to ProfileTypes.USER.value
            )
        }
}


fun getActivity(id: String?, configuration: Configuration): Map<String, Any> {
    if (id == null || UUID.fromString(id) == UUID.fromString("00000000-0000-0000-0000-000000000000")) {
        return mapOf(
            CommonFields.NAME.value to "Активность не назначена"
        )
    }

    val table = Tables.ACTIVITY

    return DSL.using(configuration)
        .selectFrom(table)
        .where(table.ID.eq(UUID.fromString(id)))
        .fetchOne()
        .map { record ->
            mapOf(
                CommonFields.ID.value to record[table.ID].toString(),
                CommonFields.NAME.value to record[table.NAME].toString(),
                CommonFields.TYPE.value to ProfileTypes.ACTIVITY.value
            )
        }
}

fun bodyWithCreatedByField(body: com.jsoniter.any.Any, jwt: DecodedJWT): com.jsoniter.any.Any {
    val newBody = mutableMapOf<String, kotlin.Any?>()

    for (key in body.keys()) {
        newBody[key.toString()] = body[key.toString()]
    }

    newBody[CommonFields.CREATED_BY.value] = jwt.getClaim(JwtClaims.ID.value).asString()

    return JsonIterator.deserialize(JsonStream.serialize(com.jsoniter.any.Any.wrap(newBody)))
}

fun getManagersAndSupervisorIds(configuration: Configuration): List<UUID> {
    return DSL.using(configuration)
        .selectFrom(Tables.USERS)
        .where(Tables.USERS.TYPE.eq(UserType.manager)
            .or(Tables.USERS.TYPE.eq(UserType.supervisor)))
        .fetch()
        .fold (mutableListOf<UUID>()) { list, record ->
            list.add(record[Tables.USERS.ID])

            list
        }
}
fun readKeyFromFile(dir : File) : String{
    val reader = BufferedReader(FileReader(dir))
    var line: String? = null
    val sBuilder = StringBuilder()

    try {
        while((reader.readLine().also { line = it }) != null) {
            sBuilder.append(line)
        }

    } finally {
        reader.close()
    }
    var key = sBuilder.toString()
    key = key?.replace("-----BEGIN PUBLIC KEY-----", "")
    key = key?.replace("-----END PUBLIC KEY-----", "")
    key = key?.replace("-----BEGIN PRIVATE KEY-----", "")
    key = key?.replace("-----END PRIVATE KEY-----", "")
    key = key?.replace("-----END PRIVATE KEY-----", "")
    key = key?.replace("\n", "")
    return key
}