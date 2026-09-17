package br.com.fiap.inovagab.data.api

import br.com.fiap.inovagab.data.model.InnovationIdea
import br.com.fiap.inovagab.data.model.AiSuggestion
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

object IdeaApi {
    suspend fun evaluateWithAi(id: String): AiSuggestion {
        val result = JSONObject(ApiClient.request("/ideas/$id/ai-evaluation", "POST"))
        val ids = result.getJSONArray("strategyIds")
        return AiSuggestion(result.getInt("score"), when (result.getString("priority")) {
            "CRITICAL" -> "Crítica"; "HIGH" -> "Alta"; "LOW" -> "Baixa"; else -> "Média"
        }, (0 until ids.length()).map { ids.getString(it) }, result.getString("reason"))
    }

    suspend fun list(mine: Boolean): List<InnovationIdea> {
        val path = if (mine) "/ideas/mine" else "/ideas"
        val array = JSONArray(ApiClient.request(path))
        return (0 until array.length()).map { array.getJSONObject(it).toIdea() }
    }

    suspend fun create(title: String, description: String, category: String, guidelineId: String) {
        val body = JSONObject().put("title", title.trim()).put("description", description.trim())
            .put("category", category.trim()).put("strategicGuidelineId", guidelineId)
        ApiClient.request("/ideas", "POST", body)
    }

    suspend fun prioritize(id: String, priority: String, score: Double) {
        val value = when (priority) {
            "Crítica" -> "CRITICAL"; "Alta" -> "HIGH"; "Baixa" -> "LOW"; else -> "MEDIUM"
        }
        ApiClient.request("/ideas/$id/priority", "PATCH",
            JSONObject().put("priority", value).put("managerScore", score))
    }

    suspend fun decide(id: String, approve: Boolean, comment: String) {
        val action = if (approve) "approve" else "reject"
        ApiClient.request("/ideas/$id/$action", "POST", JSONObject().put("comment", comment.trim()))
    }

    private fun JSONObject.toIdea() = InnovationIdea(
        id = getString("id"), title = getString("title"), description = getString("description"),
        author = optString("authorId"), category = getString("category"),
        strategicGuidelineId = getString("strategicGuidelineId"),
        status = when (getString("status")) {
            "APPROVED" -> "Aprovado"; "REJECTED" -> "Recusado"
            "UNDER_REVIEW" -> "Em análise"; else -> "Pendente"
        },
        priority = when (optString("priority")) {
            "HIGH" -> "Alta"; "CRITICAL" -> "Crítica"; "LOW" -> "Baixa"; else -> "Média"
        },
        managerScore = if (isNull("managerScore")) null else optDouble("managerScore"),
        managerComment = optString("managerComment").takeIf { it.isNotBlank() && it != "null" },
        createdAt = optString("createdAt").takeIf { it.isNotBlank() && it != "null" }
            ?.let { Instant.parse(it).toEpochMilli() } ?: 0L,
        approvedAt = optString("evaluatedAt").takeIf { it.isNotBlank() && it != "null" }
            ?.let { Instant.parse(it).toEpochMilli() }
    )
}
