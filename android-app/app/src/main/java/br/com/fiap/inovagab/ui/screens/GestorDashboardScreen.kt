package br.com.fiap.inovagab.ui.screens

import androidx.compose.foundation.background
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
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel
import com.google.firebase.auth.FirebaseAuth


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

    // Controla qual projeto está ativo no modal de edição
    var selectedProjectForEdit by remember { mutableStateOf<CorporateProject?>(null) }

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
            items(ideas.filter { it.status == "Pendente" }) { idea ->
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

                        // Botões de aprovação e recusa da ideia
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.rejectIdea(idea) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Recusar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = { viewModel.approveIdea(idea) },
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
                    text = "Projetos & Iniciativas Ativas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista de projetos e iniciativas ativas
            items(projects) { project ->
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
                        Text("Capital Investido: R$ ${project.investment}", fontSize = 12.sp, color = Color.Gray)

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
                        FirebaseAuth.getInstance().signOut()
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

        AlertDialog(
            onDismissRequest = { selectedProjectForEdit = null },
            title = { Text("Atualizar Progresso Corporativo") },
            text = {
                Column {
                    OutlinedTextField(value = investStr, onValueChange = { investStr = it }, label = { Text("Investimento (R$)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = returnStr, onValueChange = { returnStr = it }, label = { Text("Retorno Real (R$)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = gainStr, onValueChange = { gainStr = it }, label = { Text("Eficiência (%)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
                    viewModel.updateProjectValues(
                        id = proj.id,
                        investment = investStr.toDoubleOrNull() ?: 0.0,
                        finReturn = returnStr.toDoubleOrNull() ?: 0.0,
                        prodGain = gainStr.toIntOrNull() ?: 0,
                        status = statusState
                    )
                    selectedProjectForEdit = null
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { selectedProjectForEdit = null }) { Text("Voltar") }
            }
        )
    }
}