package br.com.fiap.inovagab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.inovagab.data.FirebaseProvider
import br.com.fiap.inovagab.data.model.CorporateProject
import br.com.fiap.inovagab.data.model.InnovationIdea
import br.com.fiap.inovagab.data.model.StrategicGuideline
import br.com.fiap.inovagab.data.repository.CorporateProjectRepository
import br.com.fiap.inovagab.data.repository.InnovationIdeaRepository
import br.com.fiap.inovagab.data.repository.StrategicGuidelineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.database.ServerValue

class InnovationViewModel(
    private val ideaRepository: InnovationIdeaRepository = FirebaseProvider.innovationIdeaRepository,
    private val guidelineRepository: StrategicGuidelineRepository = FirebaseProvider.strategicGuidelineRepository,
    private val projectRepository: CorporateProjectRepository = FirebaseProvider.corporateProjectRepository
) : ViewModel() {

    // O stateIn converte o Flow frio do Firebase em um StateFlow quente para a UI do Android
    // O Firebase atualiza os flows automaticamente sempre que um dado mudar na nuvem.
    val ideas: StateFlow<List<InnovationIdea>> = ideaRepository.getIdeas()
        .stateIn(
            scope = viewModelScope,

            // Economiza a internet do usuário. Se ele fechar o app ou a tela ficar em segundo plano
            // por mais de 5 segundos, o app cancela a escuta do Firebase automaticamente.
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val guidelines: StateFlow<List<StrategicGuideline>> = guidelineRepository.getGuidelines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val projects: StateFlow<List<CorporateProject>> = projectRepository.getProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Registra uma nova ideia no banco de dados com status Pendente
    fun sendNewIdea(title: String, desc: String, author: String, category: String) {
        viewModelScope.launch {
            val idea = InnovationIdea(
                id = UUID.randomUUID().toString(), // Gera chaves alfa-numéricas seguras para nuvem
                title = title,
                description = desc,
                author = author,
                status = "Pendente",
                category = category,
                // O Firebase reconhece essa chave e injeta o milisegundo exato no servidor
                createdAt = ServerValue.TIMESTAMP
            )
            ideaRepository.saveIdea(idea)
        }
    }

    // Altera o status da ideia para Aprovado e cria um novo projeto corporativo
    fun approveIdea(idea: InnovationIdea) {
        viewModelScope.launch {
            // Atualiza o nó da ideia modificando apenas o status e a data de aprovação
            val updatedIdea = idea.copy(
                status = "Aprovado",
                approvedAt = ServerValue.TIMESTAMP // Captura o milisegundo do momento da aprovação
            )

            ideaRepository.saveIdea(updatedIdea)

            val project = CorporateProject(
                id = UUID.randomUUID().toString(),
                title = idea.title,
                description = idea.description,
                status = "Planejamento",
                investment = 0.0,
                financialReturn = 0.0,
                executionDeadline = "A definir",
                productivityGain = 0
            )
            projectRepository.saveProject(project)
        }
    }

    // Altera o status da ideia para Recusado
    fun rejectIdea(idea: InnovationIdea) {
        viewModelScope.launch {
            val updatedIdea = idea.copy(status = "Recusado")
            ideaRepository.saveIdea(updatedIdea)
        }
    }

    // Grava as novas métricas de progresso e finanças do projeto
    fun updateProjectValues(id: String, investment: Double, finReturn: Double, prodGain: Int, status: String) {
        viewModelScope.launch {
            // Localiza o projeto atual para preservar o título e descrição originais
            val currentProject = projects.value.find { it.id == id }
            if (currentProject != null) {
                val updatedProject = currentProject.copy(
                    investment = investment,
                    financialReturn = finReturn,
                    productivityGain = prodGain,
                    status = status
                )
                projectRepository.saveProject(updatedProject)
            }
        }
    }

    // Insere uma nova diretriz estratégica
    fun publishGuideline(title: String, desc: String) {
        viewModelScope.launch {
            val newGuideline = StrategicGuideline(
                id = UUID.randomUUID().toString(),
                title = title,
                description = desc
            )
            guidelineRepository.saveGuideline(newGuideline)
        }
    }

    // Exclui uma diretriz estratégica pelo seu ID
    fun removeGuideline(id: String) {
        viewModelScope.launch {
            guidelineRepository.removeGuideline(id)
        }
    }
}
