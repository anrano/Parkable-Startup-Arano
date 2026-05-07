package com.parkable.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.Timestamp
import com.parkable.app.data.firebase.Collections
import com.parkable.app.data.firebase.FirebaseModule
import com.parkable.app.data.model.Booking
import com.parkable.app.data.model.Listing
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ListingRepository(private val context: Context) {
    private val db = FirebaseModule.firestore

    fun observeAvailableListings(): Flow<List<Listing>> = callbackFlow {
        val reg = db.collection(Collections.LISTINGS)
            .whereEqualTo("available", true)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Listing::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun observeUserListings(ownerId: String): Flow<List<Listing>> = callbackFlow {
        val reg = db.collection(Collections.LISTINGS)
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Listing::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun getListing(id: String): Listing? =
        db.collection(Collections.LISTINGS).document(id).get().await()
            .toObject(Listing::class.java)?.copy(id = id)

    suspend fun uploadPhotos(ownerId: String, photos: List<Uri>): List<String> {
        return photos.map { uri -> "data:image/jpeg;base64,${uriToBase64(uri)}" }
    }

    private fun uriToBase64(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val maxSize = 400
        val ratio = minOf(
            maxSize.toFloat() / original.width,
            maxSize.toFloat() / original.height
        )
        val scaled = if (ratio < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * ratio).toInt(),
                (original.height * ratio).toInt(),
                true
            )
        } else original

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 60, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun setAvailability(listingId: String, available: Boolean) {
        db.collection(Collections.LISTINGS).document(listingId)
            .update("available", available).await()
    }

    suspend fun createListing(listing: Listing): String {
        val ref = db.collection(Collections.LISTINGS).document()
        ref.set(listing.copy(id = ref.id, createdAt = Timestamp.now())).await()
        return ref.id
    }

    suspend fun createBooking(booking: Booking): String {
        val ref = db.collection(Collections.BOOKINGS).document()
        ref.set(booking.copy(id = ref.id, createdAt = Timestamp.now())).await()
        return ref.id
    }

    fun observeUserBookings(renterId: String): Flow<List<Booking>> = callbackFlow {
        val reg = db.collection(Collections.BOOKINGS)
            .whereEqualTo("renterId", renterId)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Booking::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}