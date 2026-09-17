package br.com.fiap.inovagab.data.api

import br.com.fiap.inovagab.data.model.CorporateProject
import org.json.JSONArray
import org.json.JSONObject

object ProjectApi {
    suspend fun list(): List<CorporateProject> {
        val array = JSONArray(ApiClient.request("/projects"))
        return (0 until array.length()).map { array.getJSONObject(it).toProject() }
    }

    suspend fun update(project: CorporateProject) {
        val body = JSONObject()
            .put("strategicGuidelineId", project.strategicGuidelineId)
            .put("name", project.title.trim())
            .put("description", project.description.trim())
            .put("status", when (project.status) {
                "Em Execução" -> "IN_PROGRESS"; "Pausado" -> "PAUSED"
                "Concluído" -> "COMPLETED"; "Cancelado" -> "CANCELLED"; else -> "PLANNED"
            })
            .put("stage", project.stage)
            .put("startDate", project.startDate)
            .put("expectedEndDate", project.expectedEndDate)
            .put("investment", project.investment)
            .put("financialReturn", project.financialReturn)
            .put("costReduction", project.costReduction)
            .put("productivityGain", project.productivityGain)
        ApiClient.request("/projects/${project.id}", "PUT", body)
    }

    suspend fun dashboardSummary(): JSONObject = JSONObject(ApiClient.request("/dashboard/summary"))

    private fun JSONObject.toProject() = CorporateProject(
        id = getString("id"), title = getString("name"), description = getString("description"),
        status = when (getString("status")) {
            "IN_PROGRESS" -> "Em Execução"; "PAUSED" -> "Pausado"
            "COMPLETED" -> "Concluído"; "CANCELLED" -> "Cancelado"; else -> "Planejamento"
        },
        investment = optDouble("investment"), financialReturn = optDouble("financialReturn"),
        productivityGain = optDouble("productivityGain").toInt(),
        costReduction = optDouble("costReduction"),
        strategicGuidelineId = getString("strategicGuidelineId"),
        stage = optString("stage", "PLANEJAMENTO"),
        startDate = optString("startDate").takeIf { it.isNotBlank() && it != "null" },
        expectedEndDate = optString("expectedEndDate").takeIf { it.isNotBlank() && it != "null" }
    )
}
