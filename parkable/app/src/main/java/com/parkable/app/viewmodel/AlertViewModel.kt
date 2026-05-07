package com.parkable.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkable.app.data.model.AlertStatus
import com.parkable.app.data.model.ParkingAlert
import com.parkable.app.data.repository.AlertRepository
import com.parkable.app.data.repository.AuthRepository
import com.parkable.app.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del foro estilo SocialDrive (sector 2).
 * Encapsula también la entrega de puntos cuando una transacción se completa
 * (100 al ofreciente, 25 al receptor).
 */
class AlertViewModel(
    private val repo: AlertRepository,
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    val alerts: StateFlow<List<ParkingAlert>> = repo.observeOpenAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedAlerts: StateFlow<List<ParkingAlert>> = repo.observeCompletedAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun create(latitude: Double, longitude: Double, address: String, minutes: Int, notes: String) {
        val u = authRepo.currentUser ?: return
        viewModelScope.launch {
            repo.createAlert(
                ParkingAlert(
                    offererId = u.uid,
                    offererName = u.displayName ?: u.email ?: "Conductor",
                    latitude = latitude,
                    longitude = longitude,
                    addressHint = address,
                    leavingInMinutes = minutes,
                    notes = notes
                )
            )
        }
    }

    fun claim(alert: ParkingAlert) {
        val u = authRepo.currentUser ?: return
        if (alert.offererId == u.uid) return  // no se puede reclamar uno propio
        viewModelScope.launch {
            repo.claimAlert(alert.id, u.uid, u.displayName ?: u.email ?: "Conductor")
        }
    }

    /**
     * Confirma la transacción. Si tras la confirmación queda completada (ambos lados),
     * se reparten los puntos automáticamente (100/25).
     */
    fun confirm(alert: ParkingAlert) {
        val u = authRepo.currentUser ?: return
        viewModelScope.launch {
            val completed = when (u.uid) {
                alert.offererId -> repo.confirmAsOfferer(alert.id)
                alert.claimerId -> repo.confirmAsClaimer(alert.id)
                else -> false
            }
            if (completed) {
                userRepo.addPoints(alert.offererId, 100, "alert_offered:${alert.id}")
                alert.claimerId?.let { userRepo.addPoints(it, 25, "alert_claimed:${alert.id}") }
            }
        }
    }
}
