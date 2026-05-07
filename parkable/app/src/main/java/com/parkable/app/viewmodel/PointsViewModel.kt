package com.parkable.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkable.app.data.model.PointsTransaction
import com.parkable.app.data.model.Reward
import com.parkable.app.data.repository.AuthRepository
import com.parkable.app.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PointsViewModel(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<PointsTransaction>> = authRepo.authStateFlow()
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList()) else userRepo.observePointsHistory(user.uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _redeemFeedback = MutableStateFlow<String?>(null)
    val redeemFeedback: StateFlow<String?> = _redeemFeedback

    /**
     * Canjea una recompensa. Si el saldo es insuficiente lanza un mensaje;
     * si es suficiente, descuenta los puntos y registra la transacción.
     */
    fun redeem(reward: Reward, currentBalance: Int) {
        val u = authRepo.currentUser ?: return
        if (currentBalance < reward.cost) {
            _redeemFeedback.value = "insufficient"
            return
        }
        viewModelScope.launch {
            userRepo.addPoints(u.uid, -reward.cost, "redeem:${reward.id}")
            _redeemFeedback.value = "ok"
        }
    }

    fun consumeFeedback() { _redeemFeedback.value = null }
}
