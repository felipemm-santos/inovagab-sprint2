package br.com.fiap.inovagab.ui.screens

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
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth


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
    var showGuidelineDialog by remember { mutableStateOf(false) }

    // Soma os valores financeiros para o dashboard
    val totalInvestment = projects.sumOf { it.investment }
    val totalReturn = projects.sumOf { it.financialReturn }
    val avgProductivity = if (projects.isNotEmpty()) projects.map { it.productivityGain }.zeroIfEmptyAvg() else 0

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
            items(guidelines) { gl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(gl.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F2C59))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(gl.description, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Botão para remover a diretriz
                        TextButton(onClick = { viewModel.removeGuideline(gl.id) }, contentPadding = PaddingValues(0.dp)) {
                            Text("Excluir", color = Color.Red, fontWeight = FontWeight.Bold)
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
            items(projects) { p ->
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
                        Text("Investimento: R$ ${p.investment}", fontSize = 12.sp)
                        Text("Retorno: R$ ${p.financialReturn}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
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

    // Formulário popup para inserir título e descrição da nova diretriz
    if (showGuidelineDialog) {
        var gTitle by remember { mutableStateOf("") }
        var gDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showGuidelineDialog = false },
            title = { Text("Nova Diretriz") },
            text = {
                Column {
                    OutlinedTextField(value = gTitle, onValueChange = { gTitle = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = gDesc, onValueChange = { gDesc = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (gTitle.isNotBlank()) {
                        viewModel.publishGuideline(gTitle, gDesc)
                        showGuidelineDialog = false
                    }
                }) { Text("Publicar") }
            },
            dismissButton = {
                TextButton(onClick = { showGuidelineDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// Calcula a média de inteiros
fun List<Int>.zeroIfEmptyAvg(): Int = if (this.isEmpty()) 0 else this.average().toInt()