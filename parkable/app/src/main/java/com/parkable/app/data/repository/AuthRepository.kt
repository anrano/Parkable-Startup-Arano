package com.parkable.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.parkable.app.data.firebase.Collections
import com.parkable.app.data.firebase.FirebaseModule
import com.parkable.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de autenticación. Aísla la API de Firebase Auth detrás de funciones
 * suspend para que los ViewModels solo dependan de coroutines.
 */
class AuthRepository {
    private val auth = FirebaseModule.auth
    private val db = FirebaseModule.firestore

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Stream del usuario actual. Útil para que la navegación reaccione al login/logout. */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener {
            trySend(it.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        val res = auth.signInWithEmailAndPassword(email, password).await()
        res.user ?: error("user null")
    }

    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val res = auth.createUserWithEmailAndPassword(email, password).await()
            val user = res.user ?: error("user null")
            // Crea el documento de usuario en Firestore con saldo inicial de puntos = 0
            val profile = User(
                uid = user.uid,
                name = name,
                email = email,
                points = 0,
                createdAt = Timestamp.now()
            )
            db.collection(Collections.USERS).document(user.uid).set(profile).await()
            user
        }

    fun signOut() = auth.signOut()
}
