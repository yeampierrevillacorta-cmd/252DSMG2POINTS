package com.example.points.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.R
import com.example.points.ui.theme.PointsTheme
import com.example.points.ui.components.*

@Composable
fun LoginScreen(
    onLoginSuccess: (com.example.points.models.TipoUsuario) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = rememberLoginViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.keepSessionActive) {
        viewModel.tryRestoreSession(onLoginSuccess)
    }

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo con efectos
        Image(
            painter = painterResource(id = R.drawable.fondoiniciosesion),
            contentDescription = "Fondo de inicio de sesión",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.7f)
                .blur(radius = 3.dp),
            contentScale = ContentScale.Crop
        )
        
        // Gradiente overlay moderno
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Fondo animado sutil
        AnimatedBackground(
            modifier = Modifier.fillMaxSize(),
            particleCount = 15
        )
        
        // Contenido principal con animación
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + 
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo animado
                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "logo_scale"
                )
                
                Column(
                    modifier = Modifier.scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POINTS",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 6.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                shadowElevation = 12f
                            }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Monitoreo Urbano Inteligente",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(56.dp))

                // Card moderna con glassmorphism
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Inicia sesión para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ModernTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@correo.com",
                        leadingIcon = Icons.Default.Email,
                        isError = uiState.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email.trim()).matches(),
                        errorMessage = if (uiState.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email.trim()).matches()) {
                            "Formato de correo inválido"
                        } else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var passwordVisible by remember { mutableStateOf(false) }
                    
                    ModernTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = "Contraseña",
                        placeholder = "Ingresa tu contraseña",
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        onTrailingIconClick = { passwordVisible = !passwordVisible },
                        visualTransformation = if (passwordVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PreferenceOptionRow(
                            text = "Mantener sesión iniciada",
                            checked = uiState.keepSessionActive,
                            onCheckedChange = viewModel::onKeepSessionChange
                        )
                        PreferenceOptionRow(
                            text = "Recordar contraseña en este dispositivo",
                            checked = uiState.rememberCredentials,
                            onCheckedChange = viewModel::onRememberCredentialsChange
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = uiState.isRestoringSession,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    ModernButton(
                        text = "Iniciar Sesión",
                        onClick = { viewModel.loginUser(onLoginSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Login,
                        enabled = !uiState.isLoading && !uiState.isRestoringSession,
                        loading = uiState.isLoading,
                        variant = ButtonVariant.Primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onRegisterClick) {
                            Text(
                                "Crear cuenta",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        uiState.errorMessage?.let { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen")
@Composable
private fun LoginScreenPreview() {
    PointsTheme {
        LoginScreen(
            onLoginSuccess = { },
            onRegisterClick = { },
            onForgotPasswordClick = { }
        )
    }
}

@Composable
private fun PreferenceOptionRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun rememberLoginViewModel(): LoginViewModel {
    val context = LocalContext.current
    return viewModel(
        factory = LoginViewModel.provideFactory(context)
    )
}
