package com.parkable.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.parkable.app.data.firebase.Collections
import com.parkable.app.data.firebase.FirebaseModule
import com.parkable.app.data.model.PointsTransaction
import com.parkable.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de usuario y sistema de puntos.
 * Separa la lógica de saldo (atómica vía FieldValue.increment) del historial transaccional.
 */
class UserRepository {
    private val db = FirebaseModule.firestore

    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val reg = db.collection(Collections.USERS).document(uid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(User::class.java))
            }
        awaitClose { reg.remove() }
    }

    suspend fun getUserOnce(uid: String): User? =
        db.collection(Collections.USERS).document(uid).get().await()
            .toObject(User::class.java)

    /**
     * Suma o resta puntos de forma atómica. Registra también una entrada en el
     * historial para que el usuario pueda auditar todos los movimientos.
     */
    suspend fun addPoints(uid: String, amount: Int, reason: String) {
        db.collection(Collections.USERS).document(uid)
            .update("points", FieldValue.increment(amount.toLong()))
            .await()
        val tx = PointsTransaction(
            userId = uid,
            amount = amount,
            reason = reason,
            createdAt = Timestamp.now()
        )
        db.collection(Collections.POINTS_TX).add(tx).await()
    }

    fun observePointsHistory(uid: String): Flow<List<PointsTransaction>> = callbackFlow {
        val reg = db.collection(Collections.POINTS_TX)
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(PointsTransaction::class.java)?.copy(id = it.id)
                }?.sortedByDescending { it.createdAt?.seconds } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
