package ru.hse.cppr.application

import arrow.core.toT
import arrow.fx.ForIO
import arrow.fx.fix
import arrow.fx.typeclasses.Concurrent
import arrow.syntax.function.curried
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.dependencies.ApplicationScope
import ru.hse.cppr.utils.PropertyKey
import ru.hse.cppr.utils.PropertyValue
import ru.hse.cppr.utils.getProperty
import ru.hse.cppr.utils.getPropertySync
import java.util.concurrent.ConcurrentHashMap

object AppConfig : KoinComponent {

    private val applicationScope: ApplicationScope                        by inject()
    private val MA: Concurrent<ForIO>                                     by inject()
    private val properties: ConcurrentHashMap<PropertyKey, PropertyValue> by inject()

    private fun readProperty(props: String, key: PropertyKey): PropertyValue {
        return properties.computeIfAbsent(key, ::getPropertySync.curried()(props))
    }

    private fun getNamedProperty(propName : String): PropertyValue {
        return getProperty(MA, applicationScope.propertiesDir, "backend" toT propName)
            .run(::readProperty)
            .fix()
            .unsafeRunSync()
    }

    fun getServerHost() : PropertyValue {
        return getNamedProperty("server.host")
    }

    fun getServerHostPort() : PropertyValue {
        return getNamedProperty("server.host_port")
    }

    fun getServerPort() : Int {
        return getNamedProperty("server.port").toInt()
    }

    fun getServerThreadsIo() : Int {
        return getNamedProperty("server.threads.io").toInt()
    }

    fun getServerThreadsCore() : Int {
        return getNamedProperty("server.threads.core").toInt()
    }

    fun getServerDomain() : PropertyValue  {
        return getNamedProperty("server.domain")
    }

    fun getAuthClientId() : PropertyValue {
        return getNamedProperty("auth.client_id")
    }

    fun getAuthClientSecret() : PropertyValue {
        return getNamedProperty("auth.client_secret")
    }

    fun getAuthCallbackUrl() : PropertyValue {
        return getNamedProperty("auth.callback_url")
    }

    fun getAuthTokenUrl() : PropertyValue {
        return getNamedProperty("auth.token.url")
    }

    fun getAuthDomain() : PropertyValue {
        return getNamedProperty("auth.domain")
    }

    fun getAuthDomainUrl() : PropertyValue {
        return getNamedProperty("auth.domain.url")
    }

    fun getVersion() : PropertyValue {
        return getNamedProperty("version")
    }

    fun getAuthAdminAudience() : PropertyValue {
        return getNamedProperty("auth.admin.audience")
    }

    fun getAuthAdminGetUserByEmailUrl() : PropertyValue {
        return getNamedProperty("auth.admin.get_user_by_email_url")
    }

    fun getCreateUserUrl() : PropertyValue {
        return getNamedProperty("auth.admin.create_user_url")
    }

    fun getDatabaseUri() : PropertyValue {
        return getNamedProperty("database.uri")
    }

    fun getDatabaseUsername() : PropertyValue {
        return getNamedProperty("database.username")
    }

    fun getDatabasePassword() : PropertyValue {
        return getNamedProperty("database.password")
    }

    fun getDatabasePoolCore() : PropertyValue {
        return getNamedProperty("database.pool.core")
    }

    fun getDatabasePoolMax() : PropertyValue {
        return getNamedProperty("database.pool.max")
    }

    fun getClaimNamespace() : PropertyValue {
        return getNamedProperty("auth.claim.namespace")
    }

    fun getEnvironment() : PropertyValue {
        return getNamedProperty("env")
    }
}