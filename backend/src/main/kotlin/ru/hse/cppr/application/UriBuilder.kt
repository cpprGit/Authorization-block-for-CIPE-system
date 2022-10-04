package ru.hse.cppr.application

import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException

class UriBuilder(private var host: String) {
    private val folders: StringBuilder = StringBuilder()
    private val params: StringBuilder = StringBuilder()
    private var connType: String? = null

    val url: String
        @Throws(URISyntaxException::class, MalformedURLException::class)
        get() {
            val uri = URI(
                connType, host, folders.toString(),
                params.toString(), null
            )
            return uri.toURL().toString()
        }

    val relativeURL: String
        @Throws(URISyntaxException::class, MalformedURLException::class)
        get() {
            val uri = URI(null, null, folders.toString(), params.toString(), null)
            return uri.toString()
        }

    fun setConnectionType(conn: String) : UriBuilder {
        connType = conn
        return this
    }

    fun addSubfolder(folder: String) : UriBuilder {
        folders.append("/")
        folders.append(folder)
        return this
    }

    fun addPathResource(resource: String) : UriBuilder {
        folders.append(resource)
        return this
    }

    fun addParameter(parameter: String, value: String)  : UriBuilder {
        if (params.toString().isNotEmpty()) {
            params.append("&")
        }
        params.append(parameter)
        params.append("=")
        params.append(value)

        return this
    }
}