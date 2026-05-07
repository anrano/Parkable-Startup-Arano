package com.parkable.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkable.app.data.model.User
import com.parkable.app.data.repository.AuthRepository
import com.parkable.app.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    /** True si hay usuario logueado en Firebase. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val authedUser: StateFlow<User?> = authRepo.authStateFlow()
        .flatMapLatest { fbUser ->
            if (fbUser == null) flowOf(null) else userRepo.observeUser(fbUser.uid)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isLoggedIn: StateFlow<Boolean?> = MutableStateFlow<Boolean?>(null).also { flow ->
        viewModelScope.launch {
            authRepo.authStateFlow().collect { flow.value = it != null }
        }
    }.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _ui.value = AuthUiState(error = "empty")
            return
        }
        _ui.value = AuthUiState(loading = true)
        viewModelScope.launch {
            authRepo.signIn(email.trim(), password)
                .onSuccess { _ui.value = AuthUiState() }
                .onFailure { _ui.value = AuthUiState(error = it.message ?: "error") }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _ui.value = AuthUiState(error = "empty")
            return
        }
        _ui.value = AuthUiState(loading = true)
        viewModelScope.launch {
            authRepo.signUp(name.trim(), email.trim(), password)
                .onSuccess { _ui.value = AuthUiState() }
                .onFailure { _ui.value = AuthUiState(error = it.message ?: "error") }
        }
    }

    fun logout() = authRepo.signOut()

    fun clearError() { _ui.value = _ui.value.copy(error = null) }
}
