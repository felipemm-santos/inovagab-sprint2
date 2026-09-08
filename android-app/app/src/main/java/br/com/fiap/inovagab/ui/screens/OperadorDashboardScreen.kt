package br.com.fiap.inovagab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import br.com.fiap.inovagab.data.model.getFormattedDate
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperadorDashboardScreen(
    viewModel: InnovationViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val ideas by viewModel.ideas.collectAsState()
    val guidelines by viewModel.guidelines.collectAsState()

    var selectedFilter by remember { mutableStateOf("Todas") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Posiciona o título no lado direito da barra superior
                    Text(
                        text = "Área Operacional",
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFF0F2C59)) {
                Text("+", color = Color.White, fontSize = 24.sp)
            }
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
                    text = "Diretrizes Estratégicas Vigentes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(guidelines) { item ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(item.title, fontWeight = FontWeight.Bold, color = Color(0xFF0F2C59), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Minhas Ideias & Dores",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf("Todas", "Passageiros", "Logística", "Comércio")
                    items(filterOptions) { filter ->
                        FilterChip(
                            selected = (selectedFilter == filter),
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            val filteredIdeas = if (selectedFilter == "Todas") {
                ideas
            } else {
                ideas.filter { it.category.equals(selectedFilter, ignoreCase = true) }
            }

            items(filteredIdeas) { idea ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    // Estrutura alinhada à esquerda com espaçamentos proporcionais
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
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Criado em: ${idea.getFormattedDate()}", fontSize = 12.sp, color = Color.Gray)

                        if (idea.approvedAt != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Atualizado em: ${idea.approvedAt.getFormattedDate()}", fontSize = 12.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Badge de status encapsulado em um Surface para destaque visual limpo
                        val statusColor = when (idea.status) {
                            "Aprovado" -> Color(0xFFE8F5E9)
                            "Recusado" -> Color(0xFFFFEBEE)
                            else -> Color(0xFFFFF3E0)
                        }
                        val textColor = when (idea.status) {
                            "Aprovado" -> Color(0xFF2E7D32)
                            "Recusado" -> Color(0xFFC62828)
                            else -> Color(0xFFEF6C00)
                        }

                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = idea.status,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
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

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Logística") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Registrar Ideia / Dor Operacional") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da ocorrência") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Descrição detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Divisão afetada:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F3F5), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        listOf("Passageiros", "Comércio", "Logística").forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (category == cat), onClick = { category = cat })
                                Text(cat, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.sendNewIdea(title, desc, "Colaborador de Bordo", category)
                        showDialog = false
                    }
                }) { Text("Submeter") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}