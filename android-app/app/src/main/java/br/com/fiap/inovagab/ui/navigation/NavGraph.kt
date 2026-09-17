package br.com.fiap.inovagab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import br.com.fiap.inovagab.data.repository.AuthRepository
import br.com.fiap.inovagab.data.api.ApiSession

import br.com.fiap.inovagab.ui.screens.GestorDashboardScreen
import br.com.fiap.inovagab.ui.screens.LiderDashboardScreen
import br.com.fiap.inovagab.ui.screens.LoginScreen
import br.com.fiap.inovagab.ui.screens.OperadorDashboardScreen
import br.com.fiap.inovagab.ui.viewmodel.InnovationViewModel

@Composable
fun InovaGabNavGraph(viewModel: InnovationViewModel) {
    // Inicializa o controlador de telas do Compose
    val navController = rememberNavController()
    val logout: () -> Unit = {
        AuthRepository.logout()
        viewModel.clearGuidelines()
        viewModel.clearIdeas()
        viewModel.clearProjects()
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Define a árvore de navegação do aplicativo
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // O app sempre começa na tela de Login
    ) {
        // Rota da Tela de Login
        composable(Screen.Login.route) {
            LoginScreen(onNavigate = { role ->
                if (ApiSession.token == null) {
                    viewModel.clearGuidelines()
                    viewModel.clearIdeas()
                    viewModel.clearProjects()
                }
                viewModel.refreshGuidelines()
                viewModel.refreshIdeas()
                viewModel.refreshProjects()
                when (role) {
                    "OPERADOR" -> navController.navigate(Screen.Operador.route) { launchSingleTop = true }
                    "GESTOR" -> navController.navigate(Screen.Gestor.route) { launchSingleTop = true }
                    "LIDER" -> navController.navigate(Screen.Lider.route) { launchSingleTop = true }
                }
            })
        }

        // Rota do painel do Operador
        composable(Screen.Operador.route) {
            OperadorDashboardScreen(
                viewModel,
                onBack = logout,
                onLogout = logout
            )
        }

        // Rota do painel do Gestor
        composable(Screen.Gestor.route) {
            GestorDashboardScreen(
                viewModel,
                onBack = logout,
                onLogout = logout
            )
        }

        // Rota do painel do Líder
        composable(Screen.Lider.route) {
            LiderDashboardScreen(
                viewModel,
                onBack = logout,
                onLogout = logout
            )
        }
    }
}
