package com.example.points.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.points.constants.AppRoutes

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    // Elementos para ciudadanos
    object Home : BottomNavItem(AppRoutes.HOME, "INICIO", Icons.Filled.Home)
    object Incidents : BottomNavItem(AppRoutes.INCIDENTS, "INCIDENTES", Icons.Filled.Report)
    object Places : BottomNavItem(AppRoutes.POI_LIST, "LUGARES", Icons.Filled.LocationOn)
    object Events : BottomNavItem(AppRoutes.EVENTS, "EVENTOS", Icons.Filled.Event)
    object Alerts : BottomNavItem("alerts", "ALERTAS", Icons.Filled.NotificationImportant)
    object Profile : BottomNavItem(AppRoutes.PROFILE, "PERFIL", Icons.Filled.NotificationImportant)
    
    // Elementos para administradores
    object AdminHome : BottomNavItem(AppRoutes.ADMIN_HOME, "INICIO", Icons.Filled.AdminPanelSettings)
    object AdminIncidents : BottomNavItem(AppRoutes.ADMIN_INCIDENTS, "INCIDENTES", Icons.Filled.Report)
    object AdminPOIs : BottomNavItem(AppRoutes.ADMIN_POI_MANAGEMENT, "LUGARES", Icons.Filled.LocationOn)
    object AdminUsers : BottomNavItem(AppRoutes.ADMIN_USER_MANAGEMENT, "USUARIOS", Icons.Filled.People)
    object AdminAnalytics : BottomNavItem("admin_analytics", "ANALÍTICAS", Icons.Filled.Analytics)
    object AdminSettings : BottomNavItem("admin_settings", "CONFIG", Icons.Filled.Settings)
    object AdminProfile : BottomNavItem(AppRoutes.ADMIN_PROFILE, "PERFIL", Icons.Filled.Person)
}

@Composable
fun MainLayout(
    navController: NavHostController,
    onProfileClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Incidents,
        BottomNavItem.Places,
        BottomNavItem.Events,
        BottomNavItem.Alerts
    )

    BackToHomeOnBack(navController = navController)

    Scaffold(
        topBar = {
            AppHeader(onProfileClick = onProfileClick)
        },
        bottomBar = {
            BottomBar(navController = navController, items = items)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
fun AdminMainLayout(
    navController: NavHostController,
    onProfileClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var userRole by remember { mutableStateOf<com.example.points.models.TipoUsuario?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Verificar el rol del usuario actual
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            try {
                val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val tipo = userDoc.getString("tipo")
                    userRole = when (tipo) {
                        "ADMINISTRADOR" -> com.example.points.models.TipoUsuario.ADMINISTRADOR
                        "MODERADOR" -> com.example.points.models.TipoUsuario.MODERADOR
                        "CIUDADANO" -> com.example.points.models.TipoUsuario.CIUDADANO
                        else -> null
                    }
                }
            } catch (e: Exception) {
                // Error al obtener el rol
            }
        }
        isLoading = false
    }
    
    // Determinar qué elementos mostrar según el rol
    val items = if (isLoading) {
        // Mostrar elementos básicos mientras carga
        listOf(
            BottomNavItem.AdminHome,
            BottomNavItem.AdminIncidents,
            BottomNavItem.AdminPOIs
        )
    } else {
        when (userRole) {
            com.example.points.models.TipoUsuario.ADMINISTRADOR -> {
                // Administradores ven todas las opciones
                listOf(
                    BottomNavItem.AdminHome,
                    BottomNavItem.AdminIncidents,
                    BottomNavItem.AdminPOIs,
                    BottomNavItem.AdminUsers,
                    BottomNavItem.AdminAnalytics,
                    BottomNavItem.AdminSettings
                )
            }
            com.example.points.models.TipoUsuario.MODERADOR -> {
                // Moderadores no ven gestión de usuarios
                listOf(
                    BottomNavItem.AdminHome,
                    BottomNavItem.AdminIncidents,
                    BottomNavItem.AdminPOIs,
                    BottomNavItem.AdminAnalytics,
                    BottomNavItem.AdminSettings
                )
            }
            else -> {
                // Fallback para otros roles
                listOf(
                    BottomNavItem.AdminHome,
                    BottomNavItem.AdminIncidents,
                    BottomNavItem.AdminPOIs
                )
            }
        }
    }

    BackToAdminHomeOnBack(navController = navController)

    Scaffold(
        topBar = {
            AdminAppHeader(onProfileClick = onProfileClick)
        },
        bottomBar = {
            BottomBar(navController = navController, items = items)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, items: List<BottomNavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isRouteSelected(item.route),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}

@Composable
private fun BackToHomeOnBack(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BackHandler(enabled = currentRoute != BottomNavItem.Home.route) {
        navController.navigate(BottomNavItem.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
private fun BackToAdminHomeOnBack(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BackHandler(enabled = currentRoute != BottomNavItem.AdminHome.route) {
        navController.navigate(BottomNavItem.AdminHome.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
