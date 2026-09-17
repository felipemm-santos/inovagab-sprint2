package br.com.fiap.inovagab.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.fiap.inovagab.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onNavigate: (String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("INOVAGAB", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Plataforma de inovação corporativa", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        val invalidEmail = email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        OutlinedTextField(value = email, onValueChange = { email = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("E-mail corporativo") }, singleLine = true, isError = invalidEmail, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), supportingText = { if (invalidEmail) Text("Informe um e-mail válido.") })
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Senha") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done))
        error?.let { Text(it, Modifier.fillMaxWidth().padding(top = 8.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            val normalized = email.trim().lowercase()
            error = when {
                !Patterns.EMAIL_ADDRESS.matcher(normalized).matches() -> "Informe um e-mail corporativo válido."
                password.isBlank() -> "Informe sua senha."
                else -> null
            }
            if (error == null) {
                loading = true
                scope.launch {
                    try {
                        onNavigate(AuthRepository.login(normalized, password))
                    } catch (exception: Exception) {
                        error = if (exception is br.com.fiap.inovagab.data.api.ApiException && exception.status == 401)
                            "E-mail ou senha inválidos."
                        else "Não foi possível entrar: ${exception.message}"
                    } finally {
                        loading = false
                    }
                }
            }
        }, enabled = !loading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("Entrar")
        }
        Spacer(Modifier.height(32.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Acesso rápido para avaliação", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("Os atalhos abrem as telas para testes visuais. Entre com uma conta para carregar dados e executar ações.",
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { AuthRepository.logout(); onNavigate("OPERADOR") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir visão do Operador")
                }
                OutlinedButton(onClick = { AuthRepository.logout(); onNavigate("GESTOR") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir visão do Gestor")
                }
                OutlinedButton(onClick = { AuthRepository.logout(); onNavigate("LIDER") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir visão do Líder")
                }
            }
        }
    }
}
