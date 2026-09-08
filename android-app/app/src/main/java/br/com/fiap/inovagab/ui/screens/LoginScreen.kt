package br.com.fiap.inovagab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onNavigate: (String) -> Unit) {
    // Guarda o texto digitado nos campos de e-mail e senha
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Permite rolar a tela quando o conteúdo ultrapassa a altura disponível
    val scrollState = rememberScrollState()

    // Obtem uma instância do Firebase
    val autentica = FirebaseAuth.getInstance()

    // Cria uma variável de estado para
    // controlar a exibição de um indicador de progresso
    var estaCarregando by remember {
        mutableStateOf(false)
    }

    // Verifica se o usuário já está autenticado
    if (autentica.currentUser != null) {
        when (autentica.currentUser?.email) {
            "operador@aguiabranca.com.br" -> onNavigate("OPERADOR")
            "gestor@aguiabranca.com.br" -> onNavigate("GESTOR")
            "lider@aguiabranca.com.br" -> onNavigate("LIDER")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título do aplicativo
        Text(
            text = "INOVAGAB",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F2C59),
            textAlign = TextAlign.Center
        )
        // Subtítulo descritivo
        Text(
            text = "Plataforma de Inovação Corporativa",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Campo para digitar o e-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            label = { Text("E-mail corporativo") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo para digitar a senha
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        // Exibe avisos se a senha estiver vazia ou incorreta
        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão para validar os dados e entrar no sistema
        Button(
            onClick = {
                if (password.isBlank()) {
                    errorMessage = "Digite uma senha"
                } else if (email.isBlank()) {
                    errorMessage = "Digite uma email válido"
                } else{
                    autentica.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { tarefa ->
                            estaCarregando = false
                            if (tarefa.isSuccessful){
                                when (email.trim().lowercase()) {
                                    "operador@aguiabranca.com.br" -> onNavigate("OPERADOR")
                                    "gestor@aguiabranca.com.br" -> onNavigate("GESTOR")
                                    "lider@aguiabranca.com.br" -> onNavigate("LIDER")
                                }
                            } else {
                                errorMessage = "Usuário ou senha incorretos."
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2C59)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (estaCarregando){
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(48.dp))

        // Painel de acesso rápido para testes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE9ECEF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Acesso Rápido para Avaliação",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF495057),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Abre a tela de Operador
                ElevatedButton(
                    onClick = { onNavigate("OPERADOR") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Operador", fontSize = 15.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(12.dp))

                // Abre a tela de Gestor
                ElevatedButton(
                    onClick = { onNavigate("GESTOR") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Gestor", fontSize = 15.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(12.dp))

                // Abre a tela de Lider
                ElevatedButton(
                    onClick = { onNavigate("LIDER") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("Lider", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}