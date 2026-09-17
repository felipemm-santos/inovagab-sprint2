package br.com.fiap.inovagab.data.repository

import br.com.fiap.inovagab.data.api.GuidelineApi
import br.com.fiap.inovagab.data.api.IdeaApi
import br.com.fiap.inovagab.data.api.ProjectApi
import br.com.fiap.inovagab.data.model.CorporateProject
import br.com.fiap.inovagab.data.model.AiSuggestion
import br.com.fiap.inovagab.data.model.InnovationIdea
import br.com.fiap.inovagab.data.model.StrategicGuideline
import org.json.JSONObject

/** Operações de negócio consumidas pelo ViewModel. Os API clients cuidam do transporte JSON. */
object InnovationBackendRepository {
    suspend fun guidelines(): List<StrategicGuideline> = GuidelineApi.list()
    suspend fun saveGuideline(value: StrategicGuideline): StrategicGuideline = GuidelineApi.save(value)
    suspend fun deleteGuideline(id: String) = GuidelineApi.delete(id)

    suspend fun ideas(mine: Boolean): List<InnovationIdea> = IdeaApi.list(mine)
    suspend fun createIdea(title: String, description: String, category: String, guidelineId: String) =
        IdeaApi.create(title, description, category, guidelineId)
    suspend fun prioritizeIdea(id: String, priority: String, score: Double) =
        IdeaApi.prioritize(id, priority, score)
    suspend fun decideIdea(id: String, approve: Boolean, comment: String) =
        IdeaApi.decide(id, approve, comment)
    suspend fun evaluateIdea(id: String): AiSuggestion = IdeaApi.evaluateWithAi(id)

    suspend fun projects(): List<CorporateProject> = ProjectApi.list()
    suspend fun updateProject(value: CorporateProject) = ProjectApi.update(value)
    suspend fun dashboardSummary(): JSONObject = ProjectApi.dashboardSummary()
}
