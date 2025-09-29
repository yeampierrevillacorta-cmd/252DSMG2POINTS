package com.example.points

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.points.auth.ForgotPasswordScreen
import com.example.points.auth.LoginScreen
import com.example.points.auth.RegisterScreen
import com.example.points.profile.ProfileScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    Toast.makeText(navController.context, "Login exitoso ✅", Toast.LENGTH_SHORT).show()
                    navController.navigate("profile") {
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
                    navController.navigate("profile") {
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

        composable("profile") {
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
