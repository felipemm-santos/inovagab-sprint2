package br.com.fiap.inovagab.data.repository

import br.com.fiap.inovagab.data.api.ApiClient
import br.com.fiap.inovagab.data.api.ApiSession
import org.json.JSONObject

object AuthRepository {
    suspend fun login(email: String, password: String): String {
        val body = JSONObject().put("email", email).put("password", password)
        val response = JSONObject(ApiClient.request("/auth/login", "POST", body))
        val token = response.getString("accessToken")
        val role = response.getJSONObject("user").getString("role")
        ApiSession.start(token, role)
        return role
    }

    suspend fun currentUser(): JSONObject = JSONObject(ApiClient.request("/auth/me"))

    fun logout() = ApiSession.clear()
}
