package com.parkable.app.data.repository

import com.google.firebase.Timestamp
import com.parkable.app.data.firebase.Collections
import com.parkable.app.data.firebase.FirebaseModule
import com.parkable.app.data.model.AlertStatus
import com.parkable.app.data.model.ParkingAlert
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio del sector 2 (foro estilo SocialDrive).
 * Gestiona avisos de plazas en la calle y la doble confirmación bilateral.
 *
 * Flujo:
 *   1. Conductor A publica aviso (status=OPEN, +0 puntos aún).
 *   2. Conductor B reclama el aviso (status=CLAIMED).
 *   3. Cuando ambos confirman (offererConfirmed && claimerConfirmed) -> COMPLETED
 *      y la capa de ViewModel dispara la entrega de puntos.
 */
class AlertRepository {
    private val db = FirebaseModule.firestore

    fun observeOpenAlerts(): Flow<List<ParkingAlert>> = callbackFlow {
        val reg = db.collection(Collections.ALERTS)
            .whereIn("status", listOf(AlertStatus.OPEN.name, AlertStatus.CLAIMED.name))
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(ParkingAlert::class.java)?.copy(id = it.id)
                }?.sortedByDescending { it.createdAt?.seconds } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeCompletedAlerts(): Flow<List<ParkingAlert>> = callbackFlow {
        val reg = db.collection(Collections.ALERTS)
            .whereIn("status", listOf(AlertStatus.COMPLETED.name, AlertStatus.CANCELLED.name))
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(ParkingAlert::class.java)?.copy(id = it.id)
                }?.sortedByDescending { it.createdAt?.seconds } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun createAlert(alert: ParkingAlert): String {
        val ref = db.collection(Collections.ALERTS).document()
        ref.set(alert.copy(id = ref.id, createdAt = Timestamp.now())).await()
        return ref.id
    }

    suspend fun claimAlert(alertId: String, claimerId: String, claimerName: String) {
        db.collection(Collections.ALERTS).document(alertId).update(
            mapOf(
                "claimerId" to claimerId,
                "claimerName" to claimerName,
                "status" to AlertStatus.CLAIMED.name
            )
        ).await()
    }

    /**
     * Marca la confirmación del usuario en su rol (offerer o claimer).
     * Devuelve true si tras el update el aviso ha quedado completado (ambas confirmaciones).
     */
    suspend fun confirmAsOfferer(alertId: String): Boolean {
        val ref = db.collection(Collections.ALERTS).document(alertId)
        ref.update("offererConfirmed", true).await()
        val updated = ref.get().await().toObject(ParkingAlert::class.java) ?: return false
        if (updated.offererConfirmed && updated.claimerConfirmed) {
            ref.update("status", AlertStatus.COMPLETED.name).await()
            return true
        }
        return false
    }

    suspend fun confirmAsClaimer(alertId: String): Boolean {
        val ref = db.collection(Collections.ALERTS).document(alertId)
        ref.update("claimerConfirmed", true).await()
        val updated = ref.get().await().toObject(ParkingAlert::class.java) ?: return false
        if (updated.offererConfirmed && updated.claimerConfirmed) {
            ref.update("status", AlertStatus.COMPLETED.name).await()
            return true
        }
        return false
    }
}
