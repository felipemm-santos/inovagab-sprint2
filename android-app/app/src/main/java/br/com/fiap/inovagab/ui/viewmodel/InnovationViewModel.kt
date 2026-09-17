package br.com.fiap.inovagab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.inovagab.data.api.ApiSession
import br.com.fiap.inovagab.data.repository.InnovationBackendRepository
import br.com.fiap.inovagab.data.model.AiSuggestion
import br.com.fiap.inovagab.data.model.CorporateProject
import br.com.fiap.inovagab.data.model.InnovationIdea
import br.com.fiap.inovagab.data.model.StrategicGuideline
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class InnovationViewModel : ViewModel() {
    private val repository = InnovationBackendRepository
    private val _ideas = MutableStateFlow<List<InnovationIdea>>(emptyList())
    val ideas: StateFlow<List<InnovationIdea>> = _ideas
    private val _ideaLoading = MutableStateFlow(false)
    val ideaLoading: StateFlow<Boolean> = _ideaLoading
    private val _ideaMessage = MutableStateFlow<String?>(null)
    val ideaMessage: StateFlow<String?> = _ideaMessage
    private val _aiSuggestion = MutableStateFlow<AiSuggestion?>(null)
    val aiSuggestion: StateFlow<AiSuggestion?> = _aiSuggestion
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading
    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError

    fun evaluateIdeaWithAi(id: String) {
        if (ApiSession.role != "GESTOR") return
        viewModelScope.launch {
            _aiSuggestion.value = null
            _aiError.value = null
            _aiLoading.value = true
            try { _aiSuggestion.value = repository.evaluateIdea(id) }
            catch (error: Exception) { _aiError.value = "Sugestão indisponível: ${error.message}" }
            finally { _aiLoading.value = false }
        }
    }

    fun clearAiSuggestion() {
        _aiSuggestion.value = null
        _aiError.value = null
    }

    fun refreshIdeas() {
        val role = ApiSession.role
        if (role != "OPERADOR" && role != "GESTOR") return
        viewModelScope.launch {
            _ideaLoading.value = true
            try { _ideas.value = repository.ideas(role == "OPERADOR") }
            catch (error: Exception) { _ideaMessage.value = "Falha ao carregar ideias: ${error.message}" }
            finally { _ideaLoading.value = false }
        }
    }

    fun clearIdeaMessage() { _ideaMessage.value = null }
    fun clearIdeas() { _ideas.value = emptyList(); _ideaMessage.value = null; clearAiSuggestion() }

    private val _guidelines = MutableStateFlow<List<StrategicGuideline>>(emptyList())
    val guidelines: StateFlow<List<StrategicGuideline>> = _guidelines
    private val _guidelineLoading = MutableStateFlow(false)
    val guidelineLoading: StateFlow<Boolean> = _guidelineLoading
    private val _guidelineMessage = MutableStateFlow<String?>(null)
    val guidelineMessage: StateFlow<String?> = _guidelineMessage

    fun refreshGuidelines() {
        if (ApiSession.token == null) return
        viewModelScope.launch {
            _guidelineLoading.value = true
            try { _guidelines.value = repository.guidelines() }
            catch (error: Exception) { _guidelineMessage.value = "Falha ao carregar diretrizes: ${error.message}" }
            finally { _guidelineLoading.value = false }
        }
    }

    fun clearGuidelineMessage() { _guidelineMessage.value = null }
    fun clearGuidelines() { _guidelines.value = emptyList(); _guidelineMessage.value = null }

    private val _projects = MutableStateFlow<List<CorporateProject>>(emptyList())
    val projects: StateFlow<List<CorporateProject>> = _projects
    private val _projectLoading = MutableStateFlow(false)
    val projectLoading: StateFlow<Boolean> = _projectLoading
    private val _projectMessage = MutableStateFlow<String?>(null)
    val projectMessage: StateFlow<String?> = _projectMessage
    private val _dashboardSummary = MutableStateFlow<org.json.JSONObject?>(null)
    val dashboardSummary: StateFlow<org.json.JSONObject?> = _dashboardSummary

    fun refreshProjects() {
        val role = ApiSession.role
        if (role != "GESTOR" && role != "LIDER") return
        viewModelScope.launch {
            _projectLoading.value = true
            try {
                _projects.value = repository.projects()
                if (role == "LIDER") _dashboardSummary.value = repository.dashboardSummary()
            } catch (error: Exception) { _projectMessage.value = "Falha ao carregar projetos: ${error.message}" }
            finally { _projectLoading.value = false }
        }
    }

    fun clearProjectMessage() { _projectMessage.value = null }
    fun clearProjects() { _projects.value = emptyList(); _dashboardSummary.value = null; _projectMessage.value = null }

    // Registra uma nova ideia no banco de dados com status Pendente
    fun sendNewIdea(title: String, desc: String, category: String, guidelineId: String) {
        if (ApiSession.role != "OPERADOR") return
        viewModelScope.launch {
            _ideaLoading.value = true
            try {
                repository.createIdea(title, desc, category, guidelineId)
                _ideas.value = repository.ideas(true)
                _ideaMessage.value = "Ideia enviada com sucesso."
            } catch (error: Exception) {
                _ideaMessage.value = "Falha ao enviar ideia: ${error.message}"
            } finally { _ideaLoading.value = false }
        }
    }

    // Altera o status da ideia para Aprovado e cria um novo projeto corporativo
    fun approveIdea(idea: InnovationIdea, comment: String) {
        if (ApiSession.role != "GESTOR") return
        viewModelScope.launch {
            _ideaLoading.value = true
            try {
                repository.decideIdea(idea.id, true, comment)
                _ideas.value = repository.ideas(false)
                refreshProjects()
                _ideaMessage.value = "Ideia aprovada."
            } catch (error: Exception) { _ideaMessage.value = "Falha ao aprovar: ${error.message}" }
            finally { _ideaLoading.value = false }
        }
    }

    // Altera o status da ideia para Recusado
    fun rejectIdea(idea: InnovationIdea, comment: String) {
        if (ApiSession.role != "GESTOR") return
        viewModelScope.launch {
            _ideaLoading.value = true
            try {
                repository.decideIdea(idea.id, false, comment)
                _ideas.value = repository.ideas(false)
                _ideaMessage.value = "Ideia recusada."
            } catch (error: Exception) { _ideaMessage.value = "Falha ao recusar: ${error.message}" }
            finally { _ideaLoading.value = false }
        }
    }

    // Atualiza a prioridade definida pelo gestor para a ideia selecionada
    fun updateIdeaPriority(idea: InnovationIdea, priority: String, score: Double) {
        if (ApiSession.role != "GESTOR") return
        viewModelScope.launch {
            _ideaLoading.value = true
            try {
                repository.prioritizeIdea(idea.id, priority, score)
                _ideas.value = repository.ideas(false)
                _ideaMessage.value = "Prioridade salva."
            } catch (error: Exception) { _ideaMessage.value = "Falha ao priorizar: ${error.message}" }
            finally { _ideaLoading.value = false }
        }
    }

    // Grava as novas métricas de progresso e finanças do projeto
    fun updateProjectValues(project: CorporateProject) {
        if (ApiSession.role != "GESTOR") return
        viewModelScope.launch {
            _projectLoading.value = true
            try {
                repository.updateProject(project)
                _projects.value = repository.projects()
                _projectMessage.value = "Projeto atualizado."
            } catch (error: Exception) { _projectMessage.value = "Falha ao atualizar projeto: ${error.message}" }
            finally { _projectLoading.value = false }
        }
    }

    // Insere uma nova diretriz estratégica
    fun publishGuideline(guideline: StrategicGuideline) = saveGuideline(guideline)

    // Atualiza uma diretriz preservando sua identificação no backend.
    fun updateGuideline(guideline: StrategicGuideline) = saveGuideline(guideline)

    private fun saveGuideline(guideline: StrategicGuideline) {
        if (ApiSession.role != "LIDER") return
        viewModelScope.launch {
            _guidelineLoading.value = true
            try {
                repository.saveGuideline(guideline)
                _guidelines.value = repository.guidelines()
                _guidelineMessage.value = "Diretriz salva com sucesso."
            } catch (error: Exception) {
                _guidelineMessage.value = "Falha ao salvar diretriz: ${error.message}"
            } finally { _guidelineLoading.value = false }
        }
    }

    // Exclui uma diretriz estratégica pelo seu ID
    fun removeGuideline(id: String) {
        if (ApiSession.role != "LIDER") return
        viewModelScope.launch {
            _guidelineLoading.value = true
            try {
                repository.deleteGuideline(id)
                _guidelines.value = repository.guidelines()
                _guidelineMessage.value = "Diretriz excluída."
            } catch (error: Exception) {
                _guidelineMessage.value = "Falha ao excluir diretriz: ${error.message}"
            } finally { _guidelineLoading.value = false }
        }
    }
}
