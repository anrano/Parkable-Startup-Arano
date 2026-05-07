package com.parkable.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Puntos de acceso únicos a los servicios Firebase.
 * Se usan como dependencias inyectadas manualmente desde los repositorios
 * (sin Hilt para mantener el proyecto sencillo y didáctico para el TFG).
 */
object FirebaseModule {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
}

/** Nombres de las colecciones de Firestore, centralizados para evitar typos. */
object Collections {
    const val USERS = "users"
    const val LISTINGS = "listings"
    const val BOOKINGS = "bookings"
    const val ALERTS = "parking_alerts"
    const val POINTS_TX = "points_transactions"
}
