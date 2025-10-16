package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.models.User
import com.example.points.models.TipoUsuario
import com.example.points.viewmodel.UserManagementUiState
//el panel moraddo dentro de la gestion de usuarios esta ocupando mucho espacio, modifica para que se adapte y no obstaculice a las demas opciones dentro de la gestion de usuarios
@Composable
fun AdminUsersContent(
    uiState: UserManagementUiState,
    onUpdateUserRole: (String, TipoUsuario) -> Unit,
    onToggleUserStatus: (String, Boolean) -> Unit,
    onShowEditDialog: (User) -> Unit,
    onShowDeleteDialog: (User) -> Unit,
    onClearError: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onUpdateRoleFilter: (TipoUsuario?) -> Unit,
    onToggleActiveFilter: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con estadísticas
        UserManagementHeader(
            userStats = uiState.userStats
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Barra de búsqueda y filtros
        SearchAndFilterBar(
            searchQuery = uiState.searchQuery,
            selectedRoleFilter = uiState.selectedRoleFilter,
            showOnlyActive = uiState.showOnlyActive,
            onSearchQueryChange = onUpdateSearchQuery,
            onRoleFilterChange = onUpdateRoleFilter,
            onToggleActiveFilter = onToggleActiveFilter,
            onClearFilters = onClearFilters
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de usuarios
        if (uiState.isLoading && uiState.users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredUsers.isEmpty() && uiState.users.isNotEmpty()) {
            // No hay resultados de búsqueda
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No se encontraron usuarios",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Intenta con otros filtros de búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onUpdateRole = { newRole -> onUpdateUserRole(user.id, newRole) },
                        onToggleStatus = { isActive -> onToggleUserStatus(user.id, isActive) },
                        onEditUser = { onShowEditDialog(user) },
                        onDeleteUser = { onShowDeleteDialog(user) }
                    )
                }
            }
        }
    }
    
    // Mostrar error si existe
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            onClearError()
        }
    }
}

@Composable
fun UserManagementHeader(
    userStats: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título más grande
            Text(
                text = "Gestión de Usuarios",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estadísticas en cuadrantes compactos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactStatCard(
                    title = "Total",
                    value = userStats.values.sum().toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                CompactStatCard(
                    title = "Admins",
                    value = userStats["ADMINISTRADOR"]?.toString() ?: "0",
                    color = Color(0xFF4CAF50)
                )
                CompactStatCard(
                    title = "Mods",
                    value = userStats["MODERADOR"]?.toString() ?: "0",
                    color = Color(0xFF2196F3)
                )
                CompactStatCard(
                    title = "Ciudadanos",
                    value = userStats["CIUDADANO"]?.toString() ?: "0",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun CompactStatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .padding(horizontal = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onUpdateRole: (TipoUsuario) -> Unit,
    onToggleStatus: (Boolean) -> Unit,
    onEditUser: () -> Unit,
    onDeleteUser: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Información del usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar simplificado
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                when (user.tipo) {
                                    TipoUsuario.ADMINISTRADOR -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    TipoUsuario.MODERADOR -> Color(0xFF2196F3).copy(alpha = 0.1f)
                                    TipoUsuario.CIUDADANO -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.nombre.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = when (user.tipo) {
                                TipoUsuario.ADMINISTRADOR -> Color(0xFF4CAF50)
                                TipoUsuario.MODERADOR -> Color(0xFF2196F3)
                                TipoUsuario.CIUDADANO -> Color(0xFFFF9800)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Badge de rol
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (user.tipo) {
                                    TipoUsuario.ADMINISTRADOR -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    TipoUsuario.MODERADOR -> Color(0xFF2196F3).copy(alpha = 0.1f)
                                    TipoUsuario.CIUDADANO -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                }
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = user.tipo.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = when (user.tipo) {
                                    TipoUsuario.ADMINISTRADOR -> Color(0xFF4CAF50)
                                    TipoUsuario.MODERADOR -> Color(0xFF2196F3)
                                    TipoUsuario.CIUDADANO -> Color(0xFFFF9800)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.telefono.isNotEmpty()) {
                            Text(
                                text = user.telefono,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Menú de acciones
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Acciones",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = "Editar Usuario",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                onEditUser()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = "Eliminar Usuario",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFF44336)
                                )
                            },
                            onClick = {
                                onDeleteUser()
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Controles de rol y estado mejorados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de rol mejorado
                RoleSelector(
                    currentRole = user.tipo,
                    onRoleChange = onUpdateRole
                )
                
                // Toggle de estado mejorado
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (user.notificaciones) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (user.notificaciones) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = user.notificaciones,
                        onCheckedChange = onToggleStatus,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color(0xFFF44336),
                            uncheckedTrackColor = Color(0xFFF44336).copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSelector(
    currentRole: TipoUsuario,
    onRoleChange: (TipoUsuario) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = "Rol",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "▼",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TipoUsuario.values().forEach { role ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = role.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onRoleChange(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onUpdateUser: (String, Map<String, Any>) -> Unit
) {
    var nombre by remember { mutableStateOf(user.nombre) }
    var telefono by remember { mutableStateOf(user.telefono) }
    var email by remember { mutableStateOf(user.email) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false // El email no se puede cambiar
                )
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updates = mapOf(
                        "nombre" to nombre,
                        "telefono" to telefono
                    )
                    onUpdateUser(user.id, updates)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    selectedRoleFilter: TipoUsuario?,
    showOnlyActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onRoleFilterChange: (TipoUsuario?) -> Unit,
    onToggleActiveFilter: () -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Barra de búsqueda compacta
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { 
                    Text(
                        text = "Buscar usuarios...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filtro por rol
                var expanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(
                            text = selectedRoleFilter?.displayName ?: "Todos los roles",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = "Todos los roles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                onRoleFilterChange(null)
                                expanded = false
                            }
                        )
                        TipoUsuario.values().forEach { role ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = role.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    onRoleFilterChange(role)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Filtro por estado activo
                FilterChip(
                    onClick = onToggleActiveFilter,
                    label = { 
                        Text(
                            text = "Solo activos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = showOnlyActive,
                    modifier = Modifier.height(40.dp)
                )
                
                // Botón limpiar filtros
                if (searchQuery.isNotEmpty() || selectedRoleFilter != null || showOnlyActive) {
                    TextButton(
                        onClick = onClearFilters,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Limpiar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirmDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Eliminar Usuario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )
        },
        text = {
            Column {
                Text(
                    text = "¿Estás seguro de que deseas eliminar permanentemente este usuario?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Usuario a eliminar:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nombre: ${user.nombre}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Email: ${user.email}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Rol: ${user.tipo.displayName}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Esta acción no se puede deshacer. Se eliminarán todos los datos del usuario.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDelete(user.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text(
                    text = "Eliminar Usuario",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
