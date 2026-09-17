package br.com.fiap.inovagab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.inovagab.data.model.CorporateProject
import br.com.fiap.inovagab.data.model.InnovationIdea
import br.com.fiap.inovagab.data.model.toBrazilianCurrency
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestorDashboardScreen(
    viewModel: InnovationViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    // Carrega estados reativos da ViewModel
    val ideas by viewModel.ideas.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val guidelines by viewModel.guidelines.collectAsState()
    val guidelineLoading by viewModel.guidelineLoading.collectAsState()
    val guidelineMessage by viewModel.guidelineMessage.collectAsState()
    val ideaLoading by viewModel.ideaLoading.collectAsState()
    val ideaMessage by viewModel.ideaMessage.collectAsState()
    val projectLoading by viewModel.projectLoading.collectAsState()
    val projectMessage by viewModel.projectMessage.collectAsState()
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    if (projectLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    projectMessage?.let { message ->
        AlertDialog(onDismissRequest = viewModel::clearProjectMessage,
            title = { Text("Projetos") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearProjectMessage) { Text("OK") } })
    }
    if (ideaLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    ideaMessage?.let { message ->
        AlertDialog(onDismissRequest = viewModel::clearIdeaMessage,
            title = { Text("Ideias") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearIdeaMessage) { Text("OK") } })
    }
    if (guidelineLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    guidelineMessage?.let { message ->
        AlertDialog(onDismissRequest = viewModel::clearGuidelineMessage,
            title = { Text("Diretrizes") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearGuidelineMessage) { Text("OK") } })
    }

    // Controla qual projeto está ativo no modal de edição
    var selectedProjectForEdit by remember { mutableStateOf<CorporateProject?>(null) }
    var selectedIdeaForPriority by remember { mutableStateOf<InnovationIdea?>(null) }
    var selectedIdeaForDecision by remember { mutableStateOf<InnovationIdea?>(null) }
    var selectedIdeaForAi by remember { mutableStateOf<InnovationIdea?>(null) }
    var approveDecision by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Título da tela
                    Text(
                        text = "Painel Tático",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                        modifier = Modifier.padding(start = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Voltar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F2C59))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){

            item {
                Text(
                    text = "Triagem de Ideias Recebidas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista de ideias com status Pendente
            val pendingIdeas = ideas.filter { it.status == "Pendente" || it.status == "Em análise" }
                .sortedBy { priorityRank(it.priority) }
            if (pendingIdeas.isEmpty()) {
                item { Text("Não há ideias pendentes de avaliação.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp)) }
            }
            items(
                pendingIdeas,
                key = { it.id }
            ) { idea ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(idea.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F2C59))
                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Divisão: ${idea.category}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(idea.description, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { selectedIdeaForPriority = idea },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Prioridade: ${idea.priority}",
                                color = Color(0xFF0F2C59),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        idea.managerScore?.let { Text("Pontuação do Gestor: $it / 100", fontSize = 12.sp) }
                        OutlinedButton(onClick = {
                            selectedIdeaForAi = idea
                            viewModel.evaluateIdeaWithAi(idea.id)
                        }, enabled = !aiLoading) { Text("Solicitar sugestão da IA") }

                        // Botões de aprovação e recusa da ideia
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { selectedIdeaForDecision = idea; approveDecision = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Recusar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { selectedIdeaForDecision = idea; approveDecision = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Aprovar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Diretrizes Estratégicas Vigentes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(guidelines, key = { it.id }) { guideline ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            guideline.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F2C59)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(guideline.description, fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Projetos & Iniciativas Ativas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(projects, key = { it.id }) { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(project.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F2C59))
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(project.description, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Eficiência Mapeada: ${project.productivityGain}%", fontSize = 12.sp, color = Color.Gray)
                        Text("Capital investido: ${project.investment.toBrazilianCurrency()}", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Status: ${project.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F2C59))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botão para abrir o modal de atualização do projeto
                        Button(
                            onClick = { selectedProjectForEdit = project },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2C59)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Atualizar Métricas", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Botão de Logout
            item {
                Button(
                    onClick = {
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Sair da conta",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal para gerenciar informações do projeto selecionado
    if (selectedProjectForEdit != null) {
        val proj = selectedProjectForEdit!!
        var investStr by remember { mutableStateOf(proj.investment.toString()) }
        var returnStr by remember { mutableStateOf(proj.financialReturn.toString()) }
        var gainStr by remember { mutableStateOf(proj.productivityGain.toString()) }
        var statusState by remember { mutableStateOf(proj.status) }
        var startDate by remember(proj.id) { mutableStateOf(proj.startDate.orEmpty()) }
        var endDate by remember(proj.id) { mutableStateOf(proj.expectedEndDate.orEmpty()) }
        var stage by remember(proj.id) { mutableStateOf(proj.stage) }
        var costReduction by remember(proj.id) { mutableStateOf(proj.costReduction.toString()) }

        AlertDialog(
            onDismissRequest = { selectedProjectForEdit = null },
            title = { Text("Atualizar Progresso Corporativo") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = investStr, onValueChange = { investStr = it }, label = { Text("Investimento (R$)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = returnStr, onValueChange = { returnStr = it }, label = { Text("Retorno Real (R$)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = gainStr, onValueChange = { gainStr = it }, label = { Text("Eficiência (%)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = costReduction, onValueChange = { costReduction = it }, label = { Text("Redução de custo (R$)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = stage, onValueChange = { stage = it }, label = { Text("Etapa") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Início (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fim previsto (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Status Atual:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F3F5), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        listOf("Planejamento", "Em Execução", "Concluído").forEach { st ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (statusState == st), onClick = { statusState = st })
                                Text(st, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateProjectValues(proj.copy(
                        investment = investStr.toDouble(), financialReturn = returnStr.toDouble(),
                        productivityGain = gainStr.toInt(), costReduction = costReduction.toDouble(),
                        status = statusState, stage = stage, startDate = startDate,
                        expectedEndDate = endDate))
                    selectedProjectForEdit = null
                }, enabled = !projectLoading && investStr.toDoubleOrNull()?.let { it >= 0 } == true &&
                    returnStr.toDoubleOrNull()?.let { it >= 0 } == true &&
                    gainStr.toIntOrNull()?.let { it >= 0 } == true &&
                    costReduction.toDoubleOrNull()?.let { it >= 0 } == true &&
                    stage.isNotBlank() && runCatching { java.time.LocalDate.parse(startDate) }.isSuccess &&
                    runCatching { java.time.LocalDate.parse(endDate) }.isSuccess &&
                    endDate >= startDate) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { selectedProjectForEdit = null }) { Text("Voltar") }
            }
        )
    }

    if (selectedIdeaForPriority != null) {
        val idea = selectedIdeaForPriority!!
        var priority by remember(idea.id) { mutableStateOf(idea.priority) }
        var score by remember(idea.id) { mutableStateOf(idea.managerScore?.toString().orEmpty()) }

        AlertDialog(
            onDismissRequest = { selectedIdeaForPriority = null },
            title = { Text("Definir Prioridade") },
            text = {
                Column {
                    Text("Selecione a prioridade da ideia", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Alta", "Média", "Baixa").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = priority == option,
                                onClick = { priority = option }
                            )
                            Text(option, fontSize = 14.sp)
                        }
                    }
                    OutlinedTextField(value = score, onValueChange = { score = it },
                        label = { Text("Pontuação (0 a 100)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateIdeaPriority(idea, priority, score.toDouble())
                    selectedIdeaForPriority = null
                }, enabled = !ideaLoading && score.toDoubleOrNull()?.let { it in 0.0..100.0 } == true) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { selectedIdeaForPriority = null }) { Text("Cancelar") }
            }
        )
    }
    selectedIdeaForDecision?.let { idea ->
        var comment by remember(idea.id, approveDecision) { mutableStateOf("") }
        AlertDialog(onDismissRequest = { selectedIdeaForDecision = null },
            title = { Text(if (approveDecision) "Aprovar ideia" else "Recusar ideia") },
            text = { OutlinedTextField(value = comment, onValueChange = { comment = it },
                label = { Text("Justificativa") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { Button(onClick = {
                if (approveDecision) viewModel.approveIdea(idea, comment)
                else viewModel.rejectIdea(idea, comment)
                selectedIdeaForDecision = null
            }, enabled = comment.isNotBlank() && !ideaLoading) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { selectedIdeaForDecision = null }) { Text("Cancelar") } })
    }
    selectedIdeaForAi?.let { idea ->
        AlertDialog(onDismissRequest = {
            selectedIdeaForAi = null
            viewModel.clearAiSuggestion()
        }, title = { Text("Sugestão gerada por IA") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(idea.title, fontWeight = FontWeight.Bold)
                    if (aiLoading) CircularProgressIndicator()
                    aiError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    aiSuggestion?.let { suggestion ->
                        Text("Pontuação sugerida: ${suggestion.score} / 100")
                        Text("Prioridade sugerida: ${suggestion.priority}")
                        Text("Diretrizes relacionadas:", fontWeight = FontWeight.Bold)
                        suggestion.strategyIds.forEach { id ->
                            Text("• ${guidelines.find { it.id == id }?.title ?: id}")
                        }
                        Text("Justificativa:", fontWeight = FontWeight.Bold)
                        Text(suggestion.reason)
                    }
                    Text("A avaliação final é do Gestor.", style = MaterialTheme.typography.bodySmall)
                }
            }, confirmButton = { TextButton(onClick = {
                selectedIdeaForAi = null
                viewModel.clearAiSuggestion()
            }) { Text("Fechar") } })
    }
}

private fun priorityRank(priority: String): Int = when (priority) {
    "Alta" -> 0
    "Média" -> 1
    else -> 2
}
