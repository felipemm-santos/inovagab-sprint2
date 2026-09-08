package br.com.fiap.inovagab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.inovagab.ui.navigation.InovaGabNavGraph
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Surface é o container principal que define o fundo do app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inicializa o ViewModel central que distribui os dados mockados
                    val viewModel: InnovationViewModel = viewModel()

                    // Dispara o grafo de navegação começando pela tela de Login
                    InovaGabNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}