package com.example.points

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost  
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.points.auth.ForgotPasswordScreen
import com.example.points.auth.LoginScreen
import com.example.points.auth.RegisterScreen
import com.example.points.components.MainLayout
import com.example.points.components.AdminMainLayout
import com.example.points.models.TipoUsuario
import com.example.points.profile.ProfileScreen
import com.example.points.profile.EditProfileScreen
import com.example.points.screens.AdminHomeScreen
import com.example.points.screens.ClientHomeScreen
import com.example.points.screens.HomeScreen
import com.example.points.viewmodel.IncidentViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "login") {

        // Pantallas de autenticación (sin header ni bottom bar)
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userType ->
                    Toast.makeText(navController.context, "Login exitoso ✅", Toast.LENGTH_SHORT).show()
                    when (userType) {
                        TipoUsuario.ADMINISTRADOR -> {
                            navController.navigate("admin_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        TipoUsuario.MODERADOR -> {
                            navController.navigate("admin_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate("client_home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
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
                    navController.navigate("client_home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("forgot_passworAdminIncidentsScreend") {
            ForgotPasswordScreen(
                onPasswordReset = {
                    Toast.makeText(navController.context, "Correo de recuperación enviado 📧", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Pantalla de administrador
        composable("admin_home") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                val viewModel: IncidentViewModel = viewModel()
                AdminHomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        // Pantalla de cliente/ciudadano
        composable("client_home") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                val viewModel: IncidentViewModel = viewModel()
                ClientHomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        // Pantalla principal (legacy - mantener para compatibilidad)
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
                com.example.points.screens.POISearchScreen(
                    navController = navController
                )
            }
        }
        
        composable("poi_map") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                com.example.points.screens.POIMapScreen(
                    navController = navController
                )
            }
        }
        
        composable("poi_map/{lat}/{lon}") { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                com.example.points.screens.POIMapScreen(
                    navController = navController,
                    targetLat = lat,
                    targetLon = lon
                )
            }
        }
        
        composable("poi_detail/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId") ?: ""
            com.example.points.screens.POIDetailScreen(
                navController = navController,
                poiId = poiId
            )
        }
        
        composable("submit_poi") {
            com.example.points.screens.POISubmissionScreen(
                navController = navController
            )
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
                    },
                    onEditProfile = {
                        navController.navigate("edit_profile")
                    }
                )
            }
        }

        composable("edit_profile") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                EditProfileScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Pantallas de administración
        composable("admin_incidents") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                AdminIncidentsScreen(
                    onBackClick = { navController.popBackStack() },
                    onIncidentDetailClick = { incidentId ->
                        navController.navigate("incident_detail/$incidentId")
                    }
                )
            }
        }

        composable("admin_users") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                AdminUsersScreen()
            }
        }

        composable("admin_analytics") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                AdminAnalyticsScreen()
            }
        }

        composable("admin_settings") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                AdminSettingsScreen()
            }
        }
        
        // Gestión de POIs para administradores
        composable("admin_pois") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                com.example.points.screens.AdminPOIManagementScreen(
                    navController = navController
                )
            }
        }

        composable("admin_profile") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate("login") {
                            popUpTo("admin_profile") { inclusive = true }
                        }
                    },
                    onEditProfile = {
                        navController.navigate("admin_edit_profile")
                    }
                )
            }
        }

        composable("admin_edit_profile") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("admin_profile") }
            ) {
                EditProfileScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Pantallas específicas de cliente
        composable("my_reports") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                MyReportsScreen()
            }
        }

        composable("notifications") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate("profile") }
            ) {
                NotificationsScreen()
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

// Pantallas de administración
@Composable
fun AdminIncidentsScreen(
    onBackClick: () -> Unit,
    onIncidentDetailClick: (String) -> Unit
) {
    com.example.points.screens.AdminIncidentsScreen(
        onBackClick = onBackClick,
        onIncidentDetailClick = onIncidentDetailClick
    )
}

@Composable
fun AdminUsersScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "👥 Gestión de Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Panel de administración para gestionar usuarios",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AdminAnalyticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "📊 Analíticas del Sistema",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Dashboard con estadísticas y reportes del sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AdminSettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "⚙️ Configuración del Sistema",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Configuración avanzada del sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

// Pantallas específicas de cliente (placeholders)
@Composable
fun MyReportsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "📋 Mis Reportes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Historial de tus reportes y su estado",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NotificationsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "🔔 Notificaciones",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Próximamente: Centro de notificaciones y alertas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
