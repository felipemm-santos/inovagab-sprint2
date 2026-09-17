package br.com.fiap.inovagab.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import br.com.fiap.inovagab.data.model.StrategicGuideline
import br.com.fiap.inovagab.data.model.toBrazilianCurrency
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiderDashboardScreen(
    viewModel: InnovationViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    // Busca os dados de projetos e diretrizes na ViewModel
    val projects by viewModel.projects.collectAsState()
    val guidelines by viewModel.guidelines.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val projectLoading by viewModel.projectLoading.collectAsState()
    val projectMessage by viewModel.projectMessage.collectAsState()
    if (projectLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    projectMessage?.let { message ->
        AlertDialog(onDismissRequest = viewModel::clearProjectMessage,
            title = { Text("Projetos") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearProjectMessage) { Text("OK") } })
    }
    val guidelineLoading by viewModel.guidelineLoading.collectAsState()
    val guidelineMessage by viewModel.guidelineMessage.collectAsState()
    var showGuidelineDialog by remember { mutableStateOf(false) }
    var selectedGuidelineForEdit by remember { mutableStateOf<StrategicGuideline?>(null) }

    // Soma os valores financeiros para o dashboard
    val totalInvestment = summary?.optDouble("totalInvestment") ?: 0.0
    val totalReturn = summary?.optDouble("totalFinancialReturn") ?: 0.0
    val avgProductivity = summary?.optDouble("averageProductivityGain")?.toInt() ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Título alinhado à direita
                    Text(
                        text = "Dashboard Executivo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    // Botão para voltar ao login
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
            // Título da seção de métricas financeiras
            item {
                Text(
                    text = "Métricas Consolidadas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Exibe capital alocado e retorno lado a lado com tamanhos iguais
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Capital Alocado", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "R$ ${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        totalInvestment
                                    )
                                }", fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Retorno Capturado", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "R$ ${String.format(Locale.getDefault(), "%.2f", totalReturn)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Mostra o percentual de produtividade ao centro
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ganho Médio de Produtividade", fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(16.dp)) {
                        Text(
                            text = "+$avgProductivity%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Título da seção de diretrizes
            item {
                Text(
                    text = "Diretrizes Estratégicas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botão para criar uma nova meta da empresa
            item {
                Button(
                    onClick = { showGuidelineDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2C59)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Adicionar nova diretriz",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Lista as metas e diretrizes com mais espaço visual
            if (guidelines.isEmpty()) {
                item { Text("Nenhuma diretriz publicada.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (guidelineLoading) item { CircularProgressIndicator() }
            guidelineMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }
            items(guidelines, key = { it.id }) { gl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(gl.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F2C59))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(gl.description, fontSize = 13.sp, color = Color.DarkGray)
                        Text("${gl.category} · ${gl.campaign} · ${gl.status} · versão ${gl.version}", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = { selectedGuidelineForEdit = gl },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Editar", color = Color(0xFF0F2C59), fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { viewModel.removeGuideline(gl.id) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Excluir", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                // Espaço maior para separar o portfólio da seção acima
                Spacer(modifier = Modifier.height(16.dp))

                // Título do portfólio de projetos
                Text(
                    text = "Portfólio de Auditoria de Projetos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista os detalhes financeiros de cada projeto para auditoria
            if (projects.isEmpty()) {
                item { Text("Nenhum projeto no portfólio.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(projects, key = { it.id }) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(p.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F2C59))
                        Text(p.description, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Status: ${p.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Investimento: ${p.investment.toBrazilianCurrency()}", fontSize = 12.sp)
                        Text("Retorno: ${p.financialReturn.toBrazilianCurrency()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
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

    // Formulário popup para inserir título e descrição da nova diretriz
    if (showGuidelineDialog) {
        var gTitle by remember { mutableStateOf("") }
        var gDesc by remember { mutableStateOf("") }
        var gCategory by remember { mutableStateOf("") }
        var gCampaign by remember { mutableStateOf("") }
        var gStatus by remember { mutableStateOf("ACTIVE") }
        var gValidFrom by remember { mutableStateOf("") }
        var gValidUntil by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showGuidelineDialog = false },
            title = { Text("Nova Diretriz") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = gTitle, onValueChange = { gTitle = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = gDesc, onValueChange = { gDesc = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gCategory, onValueChange = { gCategory = it }, label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gCampaign, onValueChange = { gCampaign = it }, label = { Text("Campanha") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gValidFrom, onValueChange = { gValidFrom = it }, label = { Text("Início ISO 8601 (opcional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gValidUntil, onValueChange = { gValidUntil = it }, label = { Text("Fim ISO 8601 (opcional)") }, modifier = Modifier.fillMaxWidth())
                    listOf("DRAFT", "ACTIVE").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gStatus == option, onClick = { gStatus = option })
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (gTitle.isNotBlank() && gDesc.isNotBlank() && gCategory.isNotBlank() && gCampaign.isNotBlank()) {
                        viewModel.publishGuideline(StrategicGuideline(title = gTitle, description = gDesc,
                            category = gCategory, campaign = gCampaign, status = gStatus,
                            validFrom = gValidFrom.ifBlank { null }, validUntil = gValidUntil.ifBlank { null }))
                        showGuidelineDialog = false
                    }
                }, enabled = !guidelineLoading) { Text("Publicar") }
            },
            dismissButton = {
                TextButton(onClick = { showGuidelineDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (selectedGuidelineForEdit != null) {
        val guideline = selectedGuidelineForEdit!!
        var title by remember(guideline.id) { mutableStateOf(guideline.title) }
        var description by remember(guideline.id) { mutableStateOf(guideline.description) }
        var category by remember(guideline.id) { mutableStateOf(guideline.category) }
        var campaign by remember(guideline.id) { mutableStateOf(guideline.campaign) }
        var status by remember(guideline.id) { mutableStateOf(guideline.status) }
        var validFrom by remember(guideline.id) { mutableStateOf(guideline.validFrom.orEmpty()) }
        var validUntil by remember(guideline.id) { mutableStateOf(guideline.validUntil.orEmpty()) }

        AlertDialog(
            onDismissRequest = { selectedGuidelineForEdit = null },
            title = { Text("Editar Diretriz") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = campaign, onValueChange = { campaign = it }, label = { Text("Campanha") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = validFrom, onValueChange = { validFrom = it }, label = { Text("Início ISO 8601 (opcional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = validUntil, onValueChange = { validUntil = it }, label = { Text("Fim ISO 8601 (opcional)") }, modifier = Modifier.fillMaxWidth())
                    listOf("DRAFT", "ACTIVE", "ARCHIVED").forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = status == option, onClick = { status = option })
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && category.isNotBlank() && campaign.isNotBlank()) {
                        viewModel.updateGuideline(guideline.copy(title = title, description = description,
                            category = category, campaign = campaign, status = status,
                            validFrom = validFrom.ifBlank { null }, validUntil = validUntil.ifBlank { null }))
                        selectedGuidelineForEdit = null
                    }
                }, enabled = !guidelineLoading) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { selectedGuidelineForEdit = null }) { Text("Cancelar") }
            }
        )
    }
}

// Calcula a média de inteiros
fun List<Int>.zeroIfEmptyAvg(): Int = if (this.isEmpty()) 0 else this.average().toInt()
