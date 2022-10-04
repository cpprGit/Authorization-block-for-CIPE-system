package ru.hse.ccpr.utils

import com.jsoniter.JsonIterator
import com.jsoniter.any.Any
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

const val localhostPath = "http://localhost:8090/api/v1"


fun formPostRequest(body: String, path: String) : Request {
    val mediaType = "application/json".toMediaTypeOrNull()

    return Request.Builder()
        .url("$localhostPath$path")
        .post(body.toRequestBody(mediaType))
        .addHeader("cache-control", "no-cache")
        .addHeader("content-type", "application/json")
        .build()
}

fun formGetRequest(path: String) : Request {
    return Request.Builder()
        .url("$localhostPath$path")
        .addHeader("cache-control", "no-cache")
        .build()
}

fun jsonFromString(jsonString: String): com.jsoniter.any.Any? {
    return runBlocking { JsonIterator.deserialize(jsonString) }
}

fun compareJsonObjects(expected: Any?, saved: Any?) {
    if (expected != null && saved != null) {
        for (key in expected.keys()) {
            assertTrue(saved.keys().contains(key))
            when (key) {
                "id" -> doNothing()
                "attribute" -> compareJsonObjects(expected[key], saved[key])
                "chats" -> compareJsonObjects(expected[key], saved[key])
                "mail-fields" -> compareJsonObjects(expected[key], saved[key])

                "variants" -> compareVariants(expected[key], saved[key])

                "attributes" -> compareObjectLists(expected[key], saved[key])
                "schemaContentId" -> UUID.fromString(saved[key].toString())
                "stages" -> compareObjectLists(expected[key], saved[key])
                "tasks" -> compareObjectLists(expected[key], saved[key])
                "validators" -> compareObjectLists(expected[key], saved[key])
                "fields" -> compareObjectLists(expected[key], saved[key])
                "users" -> compareObjectLists(expected[key], saved[key])
                "student" -> compareObjectLists(expected[key], saved[key])
                "groups" -> compareObjectLists(expected[key], saved[key])

                else -> Assertions.assertEquals(expected[key].toString(), saved[key].toString())
            }
        }
    } else {
        Assertions.fail("Expected or Saved values can't be null")
    }
}

fun compareObjectLists(expected: Any?, saved: Any?) {
    val objectsExpected = expected?.asList()
    val objectsSaved = saved?.asList()
    Assertions.assertEquals(objectsExpected?.size, objectsSaved?.size)

    for (i in 0 until objectsExpected?.size!!) {
        compareJsonObjects(objectsExpected[i], objectsSaved!![i])
    }
}

fun compareVariants(expected: Any?, saved: Any?) {
    val objectsExpected = expected?.asList()
    val objectsSaved = saved?.asList()

    if (objectsExpected != null) {
        for (variant in objectsExpected) {
            if (objectsSaved != null) {
                assertTrue(objectsSaved.contains(variant))
            }
        }
    }
}

fun getIdFromResponse(response: Response): String {
    val json = jsonFromString(response.body?.string().toString())
    return json?.get("id").toString()
}


fun doNothing() {
}