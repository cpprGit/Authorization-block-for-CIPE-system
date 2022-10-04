package ru.hse.cppr.service

import com.google.gson.JsonParser
import com.jsoniter.any.Any
import io.undertow.util.BadRequestException
import io.undertow.util.StatusCodes
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.hse.cppr.application.AppConfig
import ru.hse.cppr.application.CommandLineApplicationRuntime
import ru.hse.cppr.exception.UserExistsException
import ru.hse.cppr.logging.Log
import ru.hse.cppr.representation.enums.fields.RegistrationFields
import ru.hse.cppr.utils.`as`


object Auth0Service : KoinComponent {

    private val client: OkHttpClient                                      by inject()
    private val log: Log                                                  by inject() {
        parametersOf(
            CommandLineApplicationRuntime::class
        )
    }

    fun createUserInAuth0(bodyJson : Any, id: String) {
        val token = getAdminBearerToken()

        checkIfUserExistsInAuth0(token, bodyJson[RegistrationFields.EMAIL.value].`as`())
        createUserInAuth0(token, bodyJson, id)
    }


    private fun createUserInAuth0(token : String, bodyJson : Any, id: String) {
        val response = client.newCall(formCreateUserRequestToAuth0(token, bodyJson, id)).execute()
        if (response.code != StatusCodes.CREATED) {
            throw BadRequestException(response.body?.string())
        }
    }

    private fun checkIfUserExistsInAuth0(token : String, email : String) {
        val response = client.newCall(formFindUserByEmailRequestToAuth0(token, email)).execute()

        if (response.code != StatusCodes.OK) {
            throw BadRequestException(response.body?.string())
        }

        //Auth0 returns body with '[]' if user is not present
        val responseBodyJson = response.body?.string()?.trim('[')?.trim(']')
        if (responseBodyJson?.isEmpty()!!){
            return
        }

        val responseJsonObject = JsonParser().parse(responseBodyJson).asJsonObject

        if (responseJsonObject["email_verified"].asBoolean) {
            throw UserExistsException("User with such email already exists.")
        }

        log.i(responseJsonObject.toString())
    }

    private fun getAdminBearerToken() : String {
        val response = client.newCall(formGetBearerTokenRequestToAuth0()).execute()

        if (response.code != StatusCodes.OK) {
            throw BadRequestException("Couldn't obtain admin bearer token")
        }
        return JsonParser().parse(response.body?.string()).asJsonObject["access_token"].asString
    }

    private fun formFindUserByEmailRequestToAuth0(token : String, email : String): Request {
        return Request.Builder()
            .url(AppConfig.getAuthAdminGetUserByEmailUrl() + email)
            .get()
            .addHeader("cache-control", "no-cache")
            .addHeader("Authorization", "Bearer $token")
            .build()
    }


    private fun formCreateUserRequestToAuth0(token : String, bodyJson : Any, id: String) : Request {
//        val body = FormBody.Builder()
//            .add("connection", "Username-Password-Authentication")
//            .add("email", bodyJson["email"].`as`())
//            .add("password", bodyJson["password"].`as`())
//            .add("email_verified", "false")
//            .build();

        //TODO: Looks ugly, refactor
        val mediaType = "application/json".toMediaTypeOrNull()
        val body = ("{" +
                "\n \"email\": \"${bodyJson["email"].`as`<String>()}\"," +
                "\n \"password\": \"${bodyJson["password"].`as`<String>()}\"," +
                "\n \"connection\" : \"Username-Password-Authentication\"," +
                "\n \"email_verified\" : false," +
                "\n \"user_metadata\": {" +
                  "\n \t\"role\": \"${bodyJson["role"].`as`<String>()}\"," +
                  "\n \t\"name\": \"${bodyJson["name"].`as`<String>()}\"," +
                  "\n \t\"user_id\": \"$id\"" +
                   "\n }" +
                "\n}")
            .toRequestBody(mediaType)

        return Request.Builder()
            .url(AppConfig.getCreateUserUrl())
            .post(body)
            .addHeader("cache-control", "no-cache")
            .addHeader("content-type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
    }


    private fun formGetBearerTokenRequestToAuth0() : Request {
        val body = FormBody.Builder()
            .add("client_id", AppConfig.getAuthClientId())
            .add("client_secret", AppConfig.getAuthClientSecret())
            .add("audience", AppConfig.getAuthAdminAudience())
            .add("grant_type", "client_credentials")
            .build();

        return Request.Builder()
            .url(AppConfig.getAuthTokenUrl())
            .post(body)
            .addHeader("cache-control", "no-cache")
            .addHeader("content-type", "application/json")
            .build()
    }

}