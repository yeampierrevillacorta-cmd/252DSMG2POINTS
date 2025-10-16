package com.example.points.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.models.User
import com.example.points.models.TipoUsuario
import com.example.points.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userStats: Map<String, Int> = emptyMap(),
    val selectedUser: User? = null,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val searchQuery: String = "",
    val selectedRoleFilter: TipoUsuario? = null,
    val showOnlyActive: Boolean = false
)

class UserManagementViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository(
        FirebaseFirestore.getInstance(),
        auth
    )
    
    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadUsers()
        loadUserStats()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = userRepository.getAllUsers()
                result.fold(
                    onSuccess = { users ->
                        _uiState.value = _uiState.value.copy(
                            users = users,
                            filteredUsers = users,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar usuarios: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun loadUserStats() {
        viewModelScope.launch {
            try {
                val result = userRepository.getUserStats()
                result.fold(
                    onSuccess = { stats ->
                        _uiState.value = _uiState.value.copy(userStats = stats)
                    },
                    onFailure = { _ ->
                        // No mostramos error para las estadísticas, es opcional
                    }
                )
            } catch (e: Exception) {
                // Ignorar errores de estadísticas
            }
        }
    }
    
    fun updateUserRole(userId: String, newRole: TipoUsuario) {
        viewModelScope.launch {
            try {
                val result = userRepository.updateUserType(userId, newRole)
                result.fold(
                    onSuccess = {
                        loadUsers() // Recargar la lista
                        loadUserStats() // Actualizar estadísticas
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al actualizar rol: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                val result = userRepository.toggleUserStatus(userId, isActive)
                result.fold(
                    onSuccess = {
                        loadUsers() // Recargar la lista
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cambiar estado: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun updateUserInfo(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val result = userRepository.updateUser(userId, updates)
                result.fold(
                    onSuccess = {
                        loadUsers() // Recargar la lista
                        _uiState.value = _uiState.value.copy(showEditDialog = false)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al actualizar usuario: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun selectUser(user: User) {
        _uiState.value = _uiState.value.copy(selectedUser = user)
    }
    
    fun showEditDialog(user: User) {
        _uiState.value = _uiState.value.copy(
            selectedUser = user,
            showEditDialog = true
        )
    }
    
    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            selectedUser = null
        )
    }
    
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun refresh() {
        loadUsers()
        loadUserStats()
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun updateRoleFilter(role: TipoUsuario?) {
        _uiState.value = _uiState.value.copy(selectedRoleFilter = role)
        applyFilters()
    }
    
    fun toggleActiveFilter() {
        _uiState.value = _uiState.value.copy(showOnlyActive = !_uiState.value.showOnlyActive)
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.users
        
        // Filtrar por búsqueda
        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { user ->
                user.nombre.contains(currentState.searchQuery, ignoreCase = true) ||
                user.email.contains(currentState.searchQuery, ignoreCase = true) ||
                user.telefono.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Filtrar por rol
        if (currentState.selectedRoleFilter != null) {
            filtered = filtered.filter { user ->
                user.tipo == currentState.selectedRoleFilter
            }
        }
        
        // Filtrar por estado activo
        if (currentState.showOnlyActive) {
            filtered = filtered.filter { user ->
                user.notificaciones
            }
        }
        
        _uiState.value = currentState.copy(filteredUsers = filtered)
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedRoleFilter = null,
            showOnlyActive = false,
            filteredUsers = _uiState.value.users
        )
    }
    
    fun showDeleteDialog(user: User) {
        _uiState.value = _uiState.value.copy(
            selectedUser = user,
            showDeleteDialog = true
        )
    }
    
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            selectedUser = null
        )
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                val result = userRepository.deleteUser(userId)
                result.fold(
                    onSuccess = {
                        loadUsers() // Recargar la lista
                        loadUserStats() // Actualizar estadísticas
                        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al eliminar usuario: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }
    
    suspend fun canDeleteUser(userId: String): Boolean {
        return userRepository.canDeleteUser(userId)
    }
}
