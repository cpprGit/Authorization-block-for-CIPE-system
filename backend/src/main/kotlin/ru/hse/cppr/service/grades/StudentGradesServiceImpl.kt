package ru.hse.cppr.service.grades

import arrow.fx.ForIO
import com.auth0.jwt.interfaces.DecodedJWT
import io.undertow.util.BadRequestException
import org.jooq.Configuration
import org.jooq.Record
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.UserRoles
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.representation.enums.fields.StageFields
import ru.hse.cppr.representation.enums.fields.StudentGradeFields
import ru.hse.cppr.representation.formats.ProfileTypes
import ru.hse.cppr.security.JwtClaims
import ru.hse.cppr.security.ProjectProfileSecurityProvider
import ru.hse.cppr.service.file.FileService
import java.util.*

class StudentGradesServiceImpl(override val serviceName: String) : KoinComponent, StudentGradesService {

    private val provider: TxProvider<ForIO>                               by inject()
    private val log: Log                                                  by inject() { parametersOf(CommandLineApplicationRuntime::class) }
    private val fileService: FileService                                  by inject()
    private val projectSecurityProvider: ProjectProfileSecurityProvider               by inject()

    private val studentGradesTable = Tables.STUDENT_GRADES
    private val stagesTable = Tables.STAGE
    private val activityStageTable = Tables.ACTIVITY_STAGE
    private val tasksTable = Tables.TASK
    private val taskStageTable = Tables.TASK_STAGE
    private val filesTable = Tables.FILES
    private val projects = Tables.PROJECTS


    val COLUMNS = arrayListOf(
        studentGradesTable.STUDENT_ID,
        studentGradesTable.ACTIVITY_ID,
        studentGradesTable.PROJECT_ID,
        studentGradesTable.STAGE_ID,
        studentGradesTable.MENTOR_GRADE,
        studentGradesTable.MANAGER_GRADE
    )

    var COLUMNS_WITH_ID = ArrayList(COLUMNS).apply { add(0, studentGradesTable.ID) }

    val fetchMapValues = { record: Record ->
        mapOf(
            StudentGradeFields.ID.value to record[studentGradesTable.ID]?.toString(),
            StudentGradeFields.STUDENT_ID.value to record[studentGradesTable.STUDENT_ID]?.toString(),
            StudentGradeFields.ACTIVITY_ID.value to record[studentGradesTable.ACTIVITY_ID]?.toString(),
            StudentGradeFields.PROJECT_ID.value to record[studentGradesTable.PROJECT_ID]?.toString(),
            StudentGradeFields.STAGE_ID.value to record[studentGradesTable.STAGE_ID]?.toString(),
            StudentGradeFields.MENTOR_GRADE.value to record[studentGradesTable.MENTOR_GRADE]?.toString(),
            StudentGradeFields.MANGER_GRADE.value to record[studentGradesTable.MANAGER_GRADE]?.toString()
        )
    }


    override fun initGradesProgram(studentId: UUID, projectId: UUID, configuration: Configuration) {
        val activityId =
            DSL.using(configuration)
                .selectFrom(projects)
                .where(projects.ID.eq(projectId))
                .fetchOne()
                .map {it[projects.ACTIVITY_ID]}

        val activityStages =
            DSL.using(configuration)
                .selectFrom(activityStageTable)
                .where(activityStageTable.ACTIVITY_ID.eq(activityId))
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[activityStageTable.STAGE_ID])
                    list
                }

        for (stageId in activityStages) {
            DSL.using(configuration)
                .insertInto(studentGradesTable)
                .columns(COLUMNS)
                .values(
                    studentId,
                    activityId,
                    projectId,
                    stageId,
                    null,
                    null
                )
                .execute()
        }
    }

    override fun deleteGradesProgram(studentId: UUID, projectId: UUID, configuration: Configuration) {
        DSL.using(configuration)
            .deleteFrom(studentGradesTable)
            .where(studentGradesTable.STUDENT_ID.eq(studentId)
                .and(studentGradesTable.PROJECT_ID.eq(projectId)))
            .execute()
    }


    override fun updateStudentGrade(projectId: UUID, studentId: UUID, stageId: UUID, gradeType: String, grade: Int?, configuration: Configuration) {

        val gradeField = when(gradeType) {
            "mentor" -> studentGradesTable.MENTOR_GRADE
            "manager" -> studentGradesTable.MANAGER_GRADE
            else -> throw BadRequestException("Invalid grade type.")
        }

        DSL.using(configuration)
            .update(studentGradesTable)
            .set(gradeField, grade)
            .where(studentGradesTable.PROJECT_ID.eq(projectId)
                .and(studentGradesTable.STUDENT_ID.eq(studentId))
                .and(studentGradesTable.STAGE_ID.eq(stageId)))
            .execute()
    }


    override fun getActivityStudentGradesProgram(
        activityId: UUID,
        studentId: UUID,
        configuration: Configuration
    ): MutableList<Map<String, Any?>> {
        return DSL.using(configuration)
            .select()
            .from(studentGradesTable)
            .innerJoin(stagesTable)
            .on(studentGradesTable.STAGE_ID.eq(stagesTable.ID))
            .where(
                studentGradesTable.ACTIVITY_ID.eq(activityId).and(
                    studentGradesTable.STUDENT_ID.eq(studentId)
                )
            )
            .fetch()
            .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                list.add(
                    mapOf(
                        StageFields.NAME.value to "stage:${record[stagesTable.ID]}",
                        StageFields.GRADE_COEFF.value to record[stagesTable.GRADE_COEFFICIENT]?.toString(),
                        StudentGradeFields.GRADE.value to getStageGrade(record)
                    )
                )

                list
            }
    }


    override fun getActivityGradesFieldsProgram(activityId: UUID, configuration: Configuration): MutableList<Map<String, Any?>> {
        val fields = DSL.using(configuration)
            .select()
            .from(activityStageTable)
            .innerJoin(stagesTable)
            .on(activityStageTable.STAGE_ID.eq(stagesTable.ID))
            .where(activityStageTable.ACTIVITY_ID.eq(activityId))
            .fetch()
            .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                list.add(mapOf(
                    CommonFields.NAME.value to "stage:${record[stagesTable.ID]}",
                    CommonFields.TITLE.value to "Оценка за: ${record[stagesTable.NAME]}"
                ))

                list
            }

        fields.add(mapOf(
            CommonFields.NAME.value to "finalGrade",
            CommonFields.TITLE.value to "Итоговая оценка"
        ))

        fields.add(0, mapOf(
            CommonFields.NAME.value to "mentor",
            CommonFields.TITLE.value to "Имя ментора"
        ))

        fields.add(0, mapOf(
            CommonFields.NAME.value to "project",
            CommonFields.TITLE.value to "Наименование проекта"
        ))

        fields.add(0, mapOf(
            CommonFields.NAME.value to "student",
            CommonFields.TITLE.value to "Имя студента"
        ))

        return fields
    }

    override fun getProjectFieldsProgram(
        projectId: UUID,
        configuration: Configuration,
        jwt: DecodedJWT
    ): MutableList<Map<String, Any?>> {
        val fields = mutableListOf<Map<String, Any?>>()

        val activityId =
            DSL.using(configuration)
                .selectFrom(projects)
                .where(projects.ID.eq(projectId))
                .fetchOne()
                .map {it[projects.ACTIVITY_ID]}


        val stageIds =
            DSL.using(configuration)
                .select()
                .from(activityStageTable)
                .innerJoin(stagesTable)
                .on(activityStageTable.STAGE_ID.eq(stagesTable.ID))
                .where(activityStageTable.ACTIVITY_ID.eq(activityId))
                .orderBy(stagesTable.STAGE_NUMBER.asc())
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[activityStageTable.STAGE_ID])

                    list
                }

        for (stageId in stageIds) {
            val tasksWithFileFields =
                DSL.using(configuration)
                    .select()
                    .from(taskStageTable)
                    .innerJoin(tasksTable)
                    .on(tasksTable.ID.eq(taskStageTable.TASK_ID))
                    .where(taskStageTable.STAGE_ID.eq(stageId).and(tasksTable.IS_UPLOADABLE.eq(true)))
                    .fetch()
                    .fold(mutableListOf<Map<String, Any>>()) { list, record ->
                        list.add(
                            mapOf(
                                CommonFields.NAME.value to "task:${record[tasksTable.ID]}",
                                CommonFields.TITLE.value to record[tasksTable.NAME]
                            )
                        )

                        list
                    }

            for (taskField in tasksWithFileFields) {
                fields.add(taskField)
            }

            val stageName =
                DSL.using(configuration)
                    .selectFrom(stagesTable)
                    .where(stagesTable.ID.eq(stageId))
                    .fetchOne()
                    .map {
                        it[stagesTable.NAME]
                    }

            val mentorGradeField = mapOf(
                CommonFields.NAME.value to "stage:$stageId:mentor",
                CommonFields.TITLE.value to "Оценка ментора за: $stageName",


                //TODO: add role security check here:
                // If role == mentor && it is this mentor's project -> true
                // else false
                "isModifyAllowed" to mentorStageAccessCheck(projectId, jwt, configuration)
            )


            val managerGradeField = mapOf(
                CommonFields.NAME.value to "stage:$stageId:manager",
                CommonFields.TITLE.value to "Оценка менеджера за: $stageName",


                //TODO: add role security check here:
                // If role == manager -> true
                // else false
                "isModifyAllowed" to ((UserRoles.MANAGER.value == jwt.getClaim(JwtClaims.ROLE.value).asString()) ||
                        (UserRoles.SUPERVISOR.value == jwt.getClaim(JwtClaims.ROLE.value).asString()))
            )

            fields.add(mentorGradeField)
            fields.add(managerGradeField)
        }


        fields.add(0, mapOf(
            CommonFields.NAME.value to "mentor",
            CommonFields.TITLE.value to "Имя ментора"
        ))

        fields.add(0, mapOf(
            CommonFields.NAME.value to "student",
            CommonFields.TITLE.value to "Имя студента"
        ))

        return fields
    }


    fun mentorStageAccessCheck(projectId: UUID, jwt: DecodedJWT, configuration: Configuration): Boolean {
        if(jwt.getClaim(JwtClaims.ROLE.value).asString() != UserRoles.MENTOR.value) {
            return false
        }

        val mentorId = UUID.fromString(jwt.getClaim(JwtClaims.ID.value).asString())

        return DSL.using(configuration)
            .selectFrom(Tables.PROJECTS)
            .where(Tables.PROJECTS.ID.eq(projectId)
                .and(Tables.PROJECTS.LEADER_ID.eq(mentorId)))
            .fetchAny() != null
    }


    override fun getProjectStudentGradesProgram(projectId: UUID, studentId: UUID, configuration: Configuration): MutableList<Map<String, Any?>> {
        val activityId =
            DSL.using(configuration)
                .selectFrom(projects)
                .where(projects.ID.eq(projectId))
                .fetchOne()
                .map {it[projects.ACTIVITY_ID]}


        return DSL.using(configuration)
            .select()
            .from(studentGradesTable)
            .innerJoin(stagesTable)
            .on(studentGradesTable.STAGE_ID.eq(stagesTable.ID))
            .where(
                studentGradesTable.ACTIVITY_ID.eq(activityId).and(
                    studentGradesTable.STUDENT_ID.eq(studentId)
                )
            )
            .fetch()
            .fold(mutableListOf<Map<String, Any?>>()) { list, record ->
                list.add(
                    mapOf(
                        StageFields.NAME.value to "stage:${record[stagesTable.ID]}:mentor",
                        StudentGradeFields.GRADE.value to getNullableGrade(record[studentGradesTable.MENTOR_GRADE])
                    )
                )

                list.add(
                    mapOf(
                        StageFields.NAME.value to "stage:${record[stagesTable.ID]}:manager",
                        StudentGradeFields.GRADE.value to getNullableGrade(record[studentGradesTable.MANAGER_GRADE])
                    )
                )

                list
            }
    }


    override fun getProjectStudentFilesProgram(projectId: UUID, studentId: UUID, configuration: Configuration): MutableList<Pair<String, Any?>> {
        val fields = mutableListOf<Pair<String, Any?>>()

        val activityId =
            DSL.using(configuration)
                .selectFrom(projects)
                .where(projects.ID.eq(projectId))
                .fetchOne()
                .map {it[projects.ACTIVITY_ID]}


        val stageIds =
            DSL.using(configuration)
                .select()
                .from(activityStageTable)
                .innerJoin(stagesTable)
                .on(activityStageTable.STAGE_ID.eq(stagesTable.ID))
                .where(activityStageTable.ACTIVITY_ID.eq(activityId))
                .orderBy(stagesTable.STAGE_NUMBER.asc())
                .fetch()
                .fold(mutableListOf<UUID>()) { list, record ->
                    list.add(record[activityStageTable.STAGE_ID])

                    list
                }

        for (stageId in stageIds) {
            val tasksWithFile =
                DSL.using(configuration)
                    .select()
                    .from(taskStageTable)
                    .innerJoin(tasksTable)
                    .on(tasksTable.ID.eq(taskStageTable.TASK_ID))
                    .where(taskStageTable.STAGE_ID.eq(stageId).and(tasksTable.IS_UPLOADABLE.eq(true)))
                    .fetch()
                    .fold(mutableListOf<Map<String, String>>()) { list, record ->
                        list.add(
                            mapOf(
                                CommonFields.ID.value to record[tasksTable.ID].toString(),
                                CommonFields.NAME.value to "task:${record[tasksTable.ID]}"
                            )
                        )

                        list
                    }

            for (taskField in tasksWithFile) {

                val fileRecord =
                    DSL.using(configuration)
                        .selectFrom(filesTable)
                        .where(filesTable.TASK_ID.eq(UUID.fromString(taskField[CommonFields.ID.value]))
                            .and(filesTable.UPLOADER_ID.eq(studentId)))
                        .fetchOne()

                val file = when (fileRecord) {
                    null -> null
                    else -> fileRecord.map {
                        mapOf(
                            CommonFields.ID.value to it[filesTable.ID].toString(),
                            CommonFields.NAME.value to it[filesTable.NAME].toString(),
                            CommonFields.TYPE.value to ProfileTypes.FILE.value

                        )
                    }
                }

                if (file != null) {
                    val fileField = Pair(taskField[CommonFields.NAME.value].toString(), file)
                    fields.add(fileField)
                }
            }
        }

        return fields
    }


    override fun getStudentStageGrade(projectId: UUID, studentId: UUID, stageId: UUID, configuration: Configuration): Map<String, Any>? {
        val grade =
            DSL.using(configuration)
                .selectFrom(studentGradesTable)
                .where(studentGradesTable.PROJECT_ID.eq(projectId)
                    .and(studentGradesTable.STUDENT_ID.eq(studentId))
                    .and(studentGradesTable.STAGE_ID.eq(stageId)))
                .fetchOne()

        return when (grade) {
            null -> null
            else -> grade.map { mapOf(
                StudentGradeFields.MANGER_GRADE.value to getNullableGrade(it[studentGradesTable.MANAGER_GRADE]),
                StudentGradeFields.MENTOR_GRADE.value to getNullableGrade(it[studentGradesTable.MENTOR_GRADE])
            )}
        }
    }


    private fun getStageGrade(record: Record): String {
        return when (record[stagesTable.MENTOR_GRADE_FINAL]) {
            true -> getNullableGrade(record[studentGradesTable.MENTOR_GRADE])
            false -> getNullableGrade(record[studentGradesTable.MANAGER_GRADE])
        }
    }

    private fun getNullableGrade(gradeField: Int?): String {
        return when(gradeField) {
            null -> ""
            else -> gradeField.toString()
        }
    }

}