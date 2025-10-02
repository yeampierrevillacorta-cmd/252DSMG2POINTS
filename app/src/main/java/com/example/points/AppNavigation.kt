package com.example.points

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.points.auth.ForgotPasswordScreen
import com.example.points.auth.LoginScreen
import com.example.points.auth.RegisterScreen
import com.example.points.profile.ProfileScreen
import com.example.points.components.MainLayout

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "login") {

        // Pantallas de autenticación (sin header ni bottom bar)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    Toast.makeText(navController.context, "Login exitoso ✅", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    Toast.makeText(navController.context, "Registro exitoso ✅", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onPasswordReset = {
                    Toast.makeText(navController.context, "Correo de recuperación enviado 📧", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Pantallas principales (con header y bottom bar)
        composable("home") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                HomeScreen()
            }
        }

        composable("incidents") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                com.example.points.screens.IncidentsMapScreen(
                    onCreateIncidentClick = { navController.navigate("create_incident") }
                )
            }
        }
        
        composable("create_incident") {
            com.example.points.screens.CreateIncidentScreen(
                onBackClick = { navController.popBackStack() },
                onIncidentCreated = { 
                    navController.popBackStack()
                }
            )
        }

        composable("places") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                PlacesScreen()
            }
        }

        composable("events") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                EventsScreen()
            }
        }

        composable("alerts") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                AlertsScreen()
            }
        }

        composable("profile") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// Screens que antes estaban en MainScreen
@Composable
fun HomeScreen() {
    Text(
        text = "Inicio",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun IncidentsScreen() {
    Text(
        text = "Incidentes",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun PlacesScreen() {
    Text(
        text = "Lugares",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun EventsScreen() {
    Text(
        text = "Eventos",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun AlertsScreen() {
    Text(
        text = "Alertas",
        style = MaterialTheme.typography.headlineMedium
    )
}
