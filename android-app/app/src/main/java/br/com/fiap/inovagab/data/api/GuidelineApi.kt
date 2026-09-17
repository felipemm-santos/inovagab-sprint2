package br.com.fiap.inovagab.data.api

import br.com.fiap.inovagab.data.model.StrategicGuideline
import org.json.JSONArray
import org.json.JSONObject

object GuidelineApi {
    suspend fun list(): List<StrategicGuideline> {
        val array = JSONArray(ApiClient.request("/guidelines"))
        return (0 until array.length()).map { array.getJSONObject(it).toGuideline() }
    }

    suspend fun get(id: String): StrategicGuideline =
        JSONObject(ApiClient.request("/guidelines/$id")).toGuideline()

    suspend fun save(value: StrategicGuideline): StrategicGuideline {
        val body = JSONObject()
            .put("title", value.title.trim())
            .put("description", value.description.trim())
            .put("category", value.category.trim())
            .put("campaign", value.campaign.trim())
            .put("status", value.status)
            .put("validFrom", value.validFrom ?: JSONObject.NULL)
            .put("validUntil", value.validUntil ?: JSONObject.NULL)
        val path = if (value.id.isBlank()) "/guidelines" else "/guidelines/${value.id}"
        val method = if (value.id.isBlank()) "POST" else "PUT"
        return JSONObject(ApiClient.request(path, method, body)).toGuideline()
    }

    suspend fun delete(id: String) { ApiClient.request("/guidelines/$id", "DELETE") }

    private fun JSONObject.toGuideline() = StrategicGuideline(
        id = getString("id"),
        title = getString("title"),
        description = getString("description"),
        category = getString("category"),
        campaign = getString("campaign"),
        status = getString("status"),
        validFrom = optString("validFrom").takeIf { it.isNotBlank() && it != "null" },
        validUntil = optString("validUntil").takeIf { it.isNotBlank() && it != "null" },
        version = optInt("version", 1)
    )
}
