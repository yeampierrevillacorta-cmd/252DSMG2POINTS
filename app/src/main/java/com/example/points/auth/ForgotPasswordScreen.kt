package com.example.points.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    onPasswordReset: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Recuperar Contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            loading = true
            FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                .addOnSuccessListener {
                    loading = false
                    message = "Correo de recuperación enviado"
                    onPasswordReset()
                }
                .addOnFailureListener {
                    loading = false
                    message = it.localizedMessage ?: "Error al enviar correo"
                }
        }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Enviar correo de recuperación")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("⬅ Volver al Login")
        }

        message?.let { msg ->
            Spacer(Modifier.height(12.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
