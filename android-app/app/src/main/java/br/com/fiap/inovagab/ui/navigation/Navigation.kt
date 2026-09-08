package br.com.fiap.inovagab.ui.navigation

// Esse arquivo funciona como o GPS do nosso aplicativo
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Operador : Screen("operador_dashboard")
    object Gestor : Screen("gestor_dashboard")
    object Lider : Screen("lider_dashboard")
}