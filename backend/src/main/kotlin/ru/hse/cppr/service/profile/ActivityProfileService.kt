package ru.hse.cppr.service.profile

import com.auth0.jwt.interfaces.DecodedJWT
import com.jsoniter.any.Any
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.CommonFields
import ru.hse.cppr.security.ActivityProfileSecurityProvider
import ru.hse.cppr.service.crud.formatted.FormattedActivitiesService
import java.util.*

class ActivityProfileService(override val serviceName: String) : KoinComponent, ProfileService {
    private val log: Log                                                            by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    private val formattedActivityService: FormattedActivitiesService                by inject()

    private val securityProvider = ActivityProfileSecurityProvider()

    override fun getProfile(id: UUID): Map<String, kotlin.Any?> {
        return formattedActivityService.get(id)
    }

    override fun getProfile(id: UUID, jwt: DecodedJWT): Map<String, kotlin.Any?> {
        val profile = getProfile(id) as MutableMap
        profile[CommonFields.MODIFY_ALLOWED.value] = securityProvider.isModifyAllowed(id, jwt)

        return profile
    }


    override fun editProfile(id: UUID, body: Any, jwt: DecodedJWT) {
        securityProvider.checkModifyAllowed(id, jwt)
        formattedActivityService.update(id, body)
    }

}