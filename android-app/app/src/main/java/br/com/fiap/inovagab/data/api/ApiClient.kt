package br.com.fiap.inovagab.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiException(val status: Int, val code: String, message: String) : Exception(message)

object ApiSession {
    var token: String? = null
        private set
    var role: String? = null
        private set

    fun start(accessToken: String, userRole: String) {
        token = accessToken
        role = userRole
    }

    fun clear() {
        token = null
        role = null
    }
}

object ApiClient {
    // Emulador Android -> backend local. Endereço de dispositivo físico será configurado na documentação final.
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1"

    suspend fun request(path: String, method: String = "GET", body: JSONObject? = null): String =
        withContext(Dispatchers.IO) {
            val connection = (URL("$BASE_URL$path").openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 10_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/json")
                ApiSession.token?.let { setRequestProperty("Authorization", "Bearer $it") }
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }
            }
            try {
                if (body != null) connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                    it.write(body.toString())
                }
                val status = connection.responseCode
                val content = (if (status in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (status !in 200..299) {
                    val error = runCatching { JSONObject(content) }.getOrNull()
                    throw ApiException(status, error?.optString("code").orEmpty(),
                        error?.optString("message")?.takeIf { it.isNotBlank() }
                            ?: "Falha na API (HTTP $status)")
                }
                content
            } finally {
                connection.disconnect()
            }
        }
}
