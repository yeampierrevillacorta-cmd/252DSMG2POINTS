package com.example.points.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.filled.ExitToApp

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // launcher para seleccionar imagen desde galería
    val launcher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        photoUri = uri
    }

    // cuando profile cambia, actualizamos campos
    LaunchedEffect(profile) {
        name = profile.name
        phone = profile.phone
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))

        if (photoUri != null) {
            Image(painter = rememberAsyncImagePainter(photoUri), contentDescription = "Foto", modifier = Modifier.size(120.dp))
        } else if (profile.photoUrl != null) {
            Image(painter = rememberAsyncImagePainter(profile.photoUrl), contentDescription = "Foto", modifier = Modifier.size(120.dp))
        } else {
            Icon(Icons.Default.Person, contentDescription = "Sin foto", modifier = Modifier.size(120.dp))
        }

        TextButton(onClick = { launcher.launch("image/*") }) { Text("Cambiar foto") }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            viewModel.updateProfile(name, phone, photoUri) {
                Toast.makeText(context, "Perfil actualizado ✅", Toast.LENGTH_SHORT).show()
            }
        }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Guardar cambios")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            viewModel.signOut()
            onSignOut()
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(onClick = {
            viewModel.deleteAccount(onSuccess = {
                Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                onSignOut()
            }, onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            })
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Eliminar cuenta")
        }
    }
}
