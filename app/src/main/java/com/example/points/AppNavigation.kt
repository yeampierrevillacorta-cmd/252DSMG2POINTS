package com.example.points

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.example.points.viewmodel.UserManagementViewModel
import com.example.points.viewmodel.UserManagementUiState
import com.example.points.models.User
import com.example.points.models.TipoUsuario
import com.example.points.screens.AdminUsersContent
import com.example.points.screens.EditUserDialog
import com.example.points.screens.DeleteUserDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.compose.NavHost  
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.points.auth.ForgotPasswordScreen
import com.example.points.auth.LoginScreen
import com.example.points.auth.RegisterScreen
import com.example.points.components.MainLayout
import com.example.points.components.AdminMainLayout
import com.example.points.profile.ProfileScreen
import com.example.points.profile.EditProfileScreen
import com.example.points.screens.AdminHomeScreen
import com.example.points.screens.ClientHomeScreen
import com.example.points.screens.HomeScreen
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.constants.AppRoutes
import com.example.points.constants.SuccessMessage

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppRoutes.LOGIN) {

        // Pantallas de autenticación (sin header ni bottom bar)
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { userType ->
                    Toast.makeText(navController.context, SuccessMessage.LOGIN_EXITOSO.value, Toast.LENGTH_SHORT).show()
                    when (userType) {
                        TipoUsuario.ADMINISTRADOR -> {
                            navController.navigate(AppRoutes.ADMIN_HOME) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        }
                        TipoUsuario.MODERADOR -> {
                            navController.navigate(AppRoutes.ADMIN_HOME) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(AppRoutes.CLIENT_HOME) {
                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                },
                onRegisterClick = { navController.navigate(AppRoutes.REGISTER) },
                onForgotPasswordClick = { navController.navigate(AppRoutes.FORGOT_PASSWORD) }
            )
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    Toast.makeText(navController.context, SuccessMessage.USUARIO_REGISTRADO.value, Toast.LENGTH_SHORT).show()
                    navController.navigate(AppRoutes.CLIENT_HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onPasswordReset = {
                    Toast.makeText(navController.context, SuccessMessage.PASSWORD_RESET_ENVIADO.value, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Pantalla de administrador
        composable(AppRoutes.ADMIN_HOME) {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                val viewModel: IncidentViewModel = viewModel()
                AdminHomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        // Pantalla de cliente/ciudadano
        composable(AppRoutes.CLIENT_HOME) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                val viewModel: IncidentViewModel = viewModel()
                ClientHomeScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        // Pantalla principal (legacy - mantener para compatibilidad)
        composable(AppRoutes.HOME) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
                ) {
                    val viewModel: IncidentViewModel = viewModel()
                    HomeScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
        }

        composable(AppRoutes.INCIDENTS) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.IncidentsScreen(
                    navController = navController
                )
            }
        }
        
        composable(AppRoutes.INCIDENTS_MAP) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.IncidentsMapScreen(
                    onCreateIncidentClick = { navController.navigate(AppRoutes.CREATE_INCIDENT) },
                    onIncidentDetailClick = { incidentId -> 
                        navController.navigate("${AppRoutes.INCIDENT_DETAIL}/$incidentId")
                    }
                )
            }
        }
        
        composable(AppRoutes.CREATE_INCIDENT) {
            com.example.points.screens.CreateIncidentScreen(
                onBackClick = { navController.popBackStack() },
                onIncidentCreated = { 
                    navController.popBackStack()
                }
            )
        }
        
        composable("${AppRoutes.INCIDENT_DETAIL}/{incidentId}") { backStackEntry ->
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

        composable(AppRoutes.POI_SEARCH) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.POISearchScreen(
                    navController = navController
                )
            }
        }
        
        // Lista de POIs
        composable(AppRoutes.POI_LIST) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.POIScreen(
                    navController = navController
                )
            }
        }
        
        // Mapa de POIs (sin parámetros - muestra todos los POIs)
        composable(AppRoutes.POI_MAP) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.POIMapScreen(
                    navController = navController,
                    targetLat = null,
                    targetLon = null
                )
            }
        }
        
        // Mapa de POIs con coordenadas específicas
        composable("${AppRoutes.POI_MAP}/{lat}/{lon}") { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.POIMapScreen(
                    navController = navController,
                    targetLat = lat,
                    targetLon = lon
                )
            }
        }
        
        composable("${AppRoutes.POI_DETAIL}/{poiId}") { backStackEntry ->
            val poiId = backStackEntry.arguments?.getString("poiId") ?: ""
            com.example.points.screens.POIDetailScreen(
                navController = navController,
                poiId = poiId
            )
        }
        
        composable(AppRoutes.POI_SUBMISSION) {
            com.example.points.screens.POISubmissionScreen(
                navController = navController
            )
        }

        composable(AppRoutes.EVENTS) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.EventsScreen(
                    navController = navController
                )
            }
        }
        
        composable(AppRoutes.EVENT_SCHEDULE) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                com.example.points.screens.EventScheduleScreen(
                    navController = navController
                )
            }
        }
        
        composable(AppRoutes.ADMIN_EVENTS) {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                com.example.points.screens.AdminEventsScreen(
                    navController = navController
                )
            }
        }

        composable("alerts") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                AlertsScreen()
            }
        }

        composable(AppRoutes.PROFILE) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.PROFILE) { inclusive = true }
                        }
                    },
                    onEditProfile = {
                        navController.navigate(AppRoutes.EDIT_PROFILE)
                    }
                )
            }
        }

        composable(AppRoutes.EDIT_PROFILE) {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
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
        composable(AppRoutes.ADMIN_INCIDENTS) {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                AdminIncidentsScreen(
                    onBackClick = { navController.popBackStack() },
                    onIncidentDetailClick = { incidentId ->
                        navController.navigate("${AppRoutes.INCIDENT_DETAIL}/$incidentId")
                    }
                )
            }
        }

        composable(AppRoutes.ADMIN_USER_MANAGEMENT) {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                AdminUsersScreen()
            }
        }

        composable("admin_analytics") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                AdminAnalyticsScreen()
            }
        }

        composable("admin_settings") {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                AdminSettingsScreen()
            }
        }
        
        // Gestión de POIs para administradores
        composable(AppRoutes.ADMIN_POI_MANAGEMENT) {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                com.example.points.screens.AdminPOIManagementScreen(
                    navController = navController
                )
            }
        }

        composable(AppRoutes.ADMIN_PROFILE) {
            AdminMainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
            ) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.ADMIN_PROFILE) { inclusive = true }
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
                onProfileClick = { navController.navigate(AppRoutes.ADMIN_PROFILE) }
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
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                MyReportsScreen()
            }
        }

        composable("notifications") {
            MainLayout(
                navController = navController,
                onProfileClick = { navController.navigate(AppRoutes.PROFILE) }
            ) {
                NotificationsScreen()
            }
        }
    }
}

// Pantalla temporal (placeholder)
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
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var userRole by remember { mutableStateOf<TipoUsuario?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Verificar el rol del usuario actual
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val tipo = userDoc.getString("tipo")
                    userRole = when (tipo) {
                        "ADMINISTRADOR" -> TipoUsuario.ADMINISTRADOR
                        "MODERADOR" -> TipoUsuario.MODERADOR
                        "CIUDADANO" -> TipoUsuario.CIUDADANO
                        else -> null
                    }
                }
            } catch (e: Exception) {
                // Error al obtener el rol
            }
        }
        isLoading = false
    }
    
    when {
        isLoading -> {
            // Mostrar loading
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Verificando permisos...")
            }
        }
        userRole == TipoUsuario.ADMINISTRADOR -> {
            // Solo administradores pueden ver la gestión de usuarios
            val viewModel: UserManagementViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            
            AdminUsersContent(
                uiState = uiState,
                onUpdateUserRole = viewModel::updateUserRole,
                onToggleUserStatus = viewModel::toggleUserStatus,
                onShowEditDialog = viewModel::showEditDialog,
                onShowDeleteDialog = viewModel::showDeleteDialog,
                onClearError = viewModel::clearError,
                onUpdateSearchQuery = viewModel::updateSearchQuery,
                onUpdateRoleFilter = viewModel::updateRoleFilter,
                onToggleActiveFilter = viewModel::toggleActiveFilter,
                onClearFilters = viewModel::clearFilters
            )
            
            // Diálogos
            if (uiState.showEditDialog && uiState.selectedUser != null) {
                EditUserDialog(
                    user = uiState.selectedUser!!,
                    onDismiss = viewModel::hideEditDialog,
                    onUpdateUser = viewModel::updateUserInfo
                )
            }
            
            if (uiState.showDeleteDialog && uiState.selectedUser != null) {
                DeleteUserDialog(
                    user = uiState.selectedUser!!,
                    onDismiss = viewModel::hideDeleteDialog,
                    onConfirmDelete = viewModel::deleteUser
                )
            }
        }
        else -> {
            // Usuarios no autorizados (moderadores, ciudadanos, etc.)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Acceso Denegado",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Solo los administradores pueden acceder a la gestión de usuarios",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AdminAnalyticsScreen() {
    // Dashboard con estadísticas de incidentes por tipo (tiene su propio scroll)
    com.example.points.ui.screens.DashboardScreen()
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
