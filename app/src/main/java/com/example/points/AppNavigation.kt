package com.example.points

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.points.auth.ForgotPasswordScreen
import com.example.points.auth.LoginScreen
import com.example.points.auth.RegisterScreen
import com.example.points.profile.ProfileScreen
import com.example.points.components.MainLayout
import com.example.points.screens.HomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.viewmodel.IncidentViewModel

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
                val viewModel: IncidentViewModel = viewModel()
                HomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        composable("incidents") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                com.example.points.screens.IncidentsMapScreen(
                    onCreateIncidentClick = { navController.navigate("create_incident") },
                    onIncidentDetailClick = { incidentId -> 
                        navController.navigate("incident_detail/$incidentId")
                    }
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
        
        composable("incident_detail/{incidentId}") { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
            com.example.points.screens.IncidentDetailScreen(
                incidentId = incidentId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { 
                    // TODO: Implementar edición de incidente
                    navController.navigate("edit_incident/$incidentId")
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

// Pantallas temporales (placeholders)
@Composable
fun PlacesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "🏢 Lugares de Interés",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Restaurantes, cultura, entretenimiento y áreas verdes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EventsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Event,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "🎉 Eventos Urbanos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Actividades, festivales y eventos comunitarios",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AlertsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NotificationImportant,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "🔔 Alertas y Notificaciones",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Notificaciones en tiempo real sobre eventos cercanos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
