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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "INICIO", Icons.Filled.Home)
    object Incidents : BottomNavItem("incidents", "INCIDENTES", Icons.Filled.Report)
    object Places : BottomNavItem("places", "LUGARES", Icons.Filled.LocationOn)
    object Events : BottomNavItem("events", "EVENTOS", Icons.Filled.Event)
    object Alerts : BottomNavItem("alerts", "ALERTAS", Icons.Filled.NotificationImportant)
    object Profile : BottomNavItem("profile", "PERFIL", Icons.Filled.NotificationImportant)
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
