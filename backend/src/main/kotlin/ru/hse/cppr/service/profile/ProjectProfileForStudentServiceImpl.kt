package ru.hse.cppr.service.profile

import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.fix
import kotlinx.coroutines.runBlocking
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.application.TxProvider
import ru.hse.cppr.data.database_generated.Tables
import ru.hse.cppr.exception.DatabaseException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.*
import ru.hse.cppr.service.crud.ProjectService
import ru.hse.cppr.service.crud.SchemaContentService
import ru.hse.cppr.service.crud.formatted.FormattedActivitiesService
import ru.hse.cppr.service.crud.formatted.FormattedSchemaContentService
import ru.hse.cppr.service.file.FileService
import ru.hse.cppr.service.grades.StudentGradesService
import ru.hse.cppr.service.posts.PostsService
import ru.hse.cppr.utils.getActivity
import ru.hse.cppr.utils.getOptUUID
import ru.hse.cppr.utils.getUser
import java.util.*
import kotlin.math.roundToInt

class ProjectProfileForStudentServiceImpl(override val serviceName: String) : KoinComponent, ProjectProfileForStudentService {

    private val log: Log                                                                by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }
    private val provider: TxProvider<ForIO>                                             by inject()
    private val studentGradesService: StudentGradesService                              by inject()

    private val projectsService: ProjectService                                         by inject()
    private val formattedSchemaContentService: FormattedSchemaContentService            by inject()
    private val formattedActivitiesService: FormattedActivitiesService                  by inject()

    private val fileService: FileService                                                by inject()

    private val projects = Tables.PROJECTS
    private val projectStudents = Tables.PROJECT_STUDENTS



    override fun getProfile(projectId: UUID, studentId: UUID): Map<String, kotlin.Any?> {
        val profileM = provider.tx { configuration ->

            val project = projectsService.getProjectProgram(projectId, configuration)

            val formattedSchema =
                formattedSchemaContentService.getFormattedSchemaContentProgram(
                    configuration,
                    UUID.fromString(project[CommonFields.SCHEMA_CONTENT_ID.value].toString())
                )
                    .toMutableMap()

            for (key in project.keys) {
                when (key) {
                    ProjectFields.ACTIVITY_ID.value -> formattedSchema[ProjectFields.ACTIVITY.value] =
                        getActivity(project[key].toString(), configuration)
                    ProjectFields.MENTOR_ID.value -> formattedSchema[ProjectFields.MENTOR.value] =
                        getUser(UUID.fromString(project[key].toString()), configuration)
                    ProjectFields.CONSULTANT_ID.value -> formattedSchema[ProjectFields.CONSULTANT.value] =
                        getUser(getOptUUID(project[key]), configuration)
                    else -> formattedSchema[key] = project[key]
                }
            }

            val projectStages = getProjectStages(projectId, studentId, configuration)

            when(val finalGrade = getFinalGrade(projectStages as List<Map<String, Any>>)) {
                null -> {}
                else -> {
                    formattedSchema["finalGrade"] = finalGrade

                    val fields = formattedSchema[CommonFields.FIELDS.value] as MutableList<Map<String, Any>>
                    fields.add(createFinalGradeAttribute())
                    formattedSchema[CommonFields.FIELDS.value] = fields
                }
            }


            val uploadAllowed =
                DSL.using(configuration)
                    .selectFrom(projectStudents)
                    .where(projectStudents.PROJECT_ID.eq(projectId)
                        .and(projectStudents.STUDENT_ID.eq(studentId)))
                    .fetchAny() != null

            formattedSchema[CommonFields.MODIFY_ALLOWED.value] = false
            formattedSchema[ActivityFields.STAGES.value] = projectStages
            formattedSchema["uploadAllowed"] = uploadAllowed

            formattedSchema
        }


        return runBlocking {
            when (val cb = Either.catch { profileM.fix().unsafeRunSync() }) {
                is Either.Left -> {
                    throw DatabaseException(cb.a.message, cb.a.cause)
                }
                is Either.Right -> {
                    return@runBlocking cb.b
                }
            }
        }
    }


    private fun createFinalGradeAttribute(): Map<String, Any> {
        return mapOf(
            CommonFields.NAME.value to "finalGrade",
            CommonFields.TITLE.value to "Итоговая оценка",
            CommonFields.ATTRIBUTE.value to mapOf(
                AttributeFields.ID.value to UUID.randomUUID().toString(),
                AttributeFields.USAGE.value to "short_text",
                AttributeFields.NAME.value to "Final Grade",
                AttributeFields.DESCRIPTION.value to "",
                AttributeFields.TITLE.value to "Итоговая оценка",
                AttributeFields.PLACEHOLDER.value to null,
                AttributeFields.STEP.value to null,
                AttributeFields.MIN.value to null,
                AttributeFields.MAX.value to null,
                AttributeFields.HINT.value to null,
                AttributeFields.MANDATORY.value to false,
                AttributeFields.VALUE_DEFAULT.value to "null",
                AttributeFields.VARIANTS.value to emptyList<String>(),
                AttributeFields.VALIDATORS.value to emptyList<String>(),
                AttributeFields.SEARCH_NAME.value to "finalGrade",
                AttributeFields.HAS_OTHER_VARIANT.value to false
            )
        )
    }


    private fun getFinalGrade(projectStages: List<Map<String,Any>>): String? {
        var finalGrade = 0.0

        for (stage in projectStages as List<Map<String,Any>>) {
            if (stage["grade"] == null) {
                return "Не выставлена"
            }
            val gradeMap = stage["grade"] as Map<String, Any>

            val grade = when (stage[StageFields.MENTOR_GRADE_FINAL.value] as Boolean) {
                true -> gradeMap[StudentGradeFields.MENTOR_GRADE.value].toString()
                false -> gradeMap[StudentGradeFields.MANGER_GRADE.value].toString()

            }

            if (grade.isNotEmpty()) {
                val coefficient = stage[StageFields.GRADE_COEFF.value].toString().toDouble()
                finalGrade += coefficient * grade.toInt()
            }
        }

        return finalGrade.roundToInt().toString()
    }

    private fun getProjectStages(projectId: UUID, studentId: UUID, configuration: Configuration): kotlin.Any? {
        val activityId =
            DSL.using(configuration)
                .selectFrom(projects)
                .where(projects.ID.eq(projectId))
                .fetchOne()
                .map {it[projects.ACTIVITY_ID]}


        val stages = formattedActivitiesService.getActivityProgram(
            activityId,
            configuration
        )[ActivityFields.STAGES.value] as MutableList<MutableMap<String, Any?>>


        for (stage in stages) {
            val stageId = UUID.fromString(stage[CommonFields.ID.value].toString())
            stage["grade"] = studentGradesService.getStudentStageGrade(projectId, studentId, stageId, configuration)

            val tasks = stage[ActivityFields.TASKS.value] as MutableList<MutableMap<String, Any?>>

            for (task in tasks) {
                if (task["isUploadable"] as Boolean) {
                    val taskId = UUID.fromString(task[CommonFields.ID.value].toString())

                    task["file"] = fileService.getFileForTaskAndUser(taskId, studentId)
                }
            }

            stage[ActivityFields.TASKS.value] = tasks
        }

        return stages
    }

}