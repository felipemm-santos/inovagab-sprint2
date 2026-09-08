package br.com.fiap.inovagab.data.model

import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Os três perfis de acesso obrigatórios determinados pelo desafio do Grupo Águia Branca
enum class UserRole { OPERADOR, GESTOR, LIDER }

// Representa a captura de dores e oportunidades enviadas pela ponta operacional
data class InnovationIdea(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val status: String = "Pendente", // "Pendente" ou "Aprovado"
    val category: String = "Passageiros", // "Passageiros", "Comércio" ou "Logística"

    // Campos de data para rastrear o tempo de resposta do funil
    val createdAt: Any = ServerValue.TIMESTAMP,         // Data em que o operador registrou a ocorrência
    val approvedAt: Any? = null // Data de aprovação (começa nula e o gestor preenche)
)

// Mapeia as diretrizes e macro-objetivos publicados pela liderança executiva
data class StrategicGuideline(
    val id: String = "",
    val title: String = "",
    val description: String = ""
)

// Mapeia os projetos reais criados a partir das ideias aprovadas na curadoria tática
data class CorporateProject(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "Planejamento",          // "Planejamento", "Em Execução" ou "Concluído"
    val investment: Double = 0.0,      // Capital alocado para a iniciativa
    val financialReturn: Double = 0.0, // Retorno financeiro real (usado para o cálculo do ROI global)
    val executionDeadline: String = "", // Prazo estimado de entrega
    val productivityGain: Int = 0    // Ganho médio de eficiência/produtividade em %
)

// Função de extensão para formatar data

fun Any?.getFormattedDate(): String {
    val timestamp = this as? Long ?:return ""
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(date)
}