package br.com.fiap.inovagab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import br.com.fiap.inovagab.ui.screens.GestorDashboardScreen
import br.com.fiap.inovagab.ui.screens.LiderDashboardScreen
import br.com.fiap.inovagab.ui.screens.LoginScreen
import br.com.fiap.inovagab.ui.screens.OperadorDashboardScreen
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel

@Composable
fun InovaGabNavGraph(viewModel: InnovationViewModel) {
    // Inicializa o controlador de telas do Compose
    val navController = rememberNavController()

    // Define a árvore de navegação do aplicativo
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // O app sempre começa na tela de Login
    ) {
        // Rota da Tela de Login
        composable(Screen.Login.route) {
            LoginScreen(onNavigate = { role ->
                when (role) {
                    "OPERADOR" -> navController.navigate(Screen.Operador.route)
                    "GESTOR" -> navController.navigate(Screen.Gestor.route)
                    "LIDER" -> navController.navigate(Screen.Lider.route)
                }
            })
        }

        // Rota do painel do Operador
        composable(Screen.Operador.route) {
            OperadorDashboardScreen(
                viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Rota do painel do Gestor
        composable(Screen.Gestor.route) {
            GestorDashboardScreen(
                viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Rota do painel do Líder
        composable(Screen.Lider.route) {
            LiderDashboardScreen(
                viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}