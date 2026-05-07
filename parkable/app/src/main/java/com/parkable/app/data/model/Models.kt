package com.parkable.app.data.model

import com.google.firebase.Timestamp

/**
 * Usuario registrado en Parkable.
 * El [points] es el saldo de puntos canjeables (sistema de gamificación).
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val points: Int = 0,
    val createdAt: Timestamp? = null
)

/** Modalidades de alquiler que admite la plataforma. */
enum class RentalUnit { HOUR, DAY, WEEK, MONTH }

/**
 * Anuncio del marketplace (sector 1: estilo Wallapop/MilAnuncios para garajes).
 * Las fotos se almacenan en Firebase Storage; aquí guardamos las URLs.
 */
data class Listing(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val title: String = "",
    val description: String = "",
    val photos: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val pricePerHour: Double? = null,
    val pricePerDay: Double? = null,
    val pricePerWeek: Double? = null,
    val pricePerMonth: Double? = null,
    val available: Boolean = true,
    val createdAt: Timestamp? = null
)

/** Reserva confirmada de una plaza. */
data class Booking(
    val id: String = "",
    val listingId: String = "",
    val renterId: String = "",
    val renterName: String = "",
    val ownerId: String = "",
    val unit: RentalUnit = RentalUnit.HOUR,
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val status: String = "CONFIRMED",
    val createdAt: Timestamp? = null
)

/** Estado de un aviso del foro SocialDrive. */
enum class AlertStatus { OPEN, CLAIMED, COMPLETED, CANCELLED }

/**
 * Aviso del sector 2 (foro estilo SocialDrive).
 * Cuando el conductor que sale lo crea, otro lo "reclama" y se confirma la transacción
 * de forma bilateral; ambos suman puntos (100 al que ofrece, 25 al que aparca).
 */
data class ParkingAlert(
    val id: String = "",
    val offererId: String = "",
    val offererName: String = "",
    val claimerId: String? = null,
    val claimerName: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val addressHint: String = "",
    val leavingInMinutes: Int = 5,
    val notes: String = "",
    val status: AlertStatus = AlertStatus.OPEN,
    val offererConfirmed: Boolean = false,
    val claimerConfirmed: Boolean = false,
    val createdAt: Timestamp? = null
)

/** Recompensa canjeable del catálogo de puntos. */
data class Reward(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val cost: Int,
    val iconName: String
)

/** Movimiento del historial de puntos (entrada/salida). */
data class PointsTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val reason: String = "",
    val createdAt: Timestamp? = null
)
