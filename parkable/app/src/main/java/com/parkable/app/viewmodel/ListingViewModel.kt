package com.parkable.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkable.app.data.model.Booking
import com.parkable.app.data.model.Listing
import com.parkable.app.data.model.RentalUnit
import com.parkable.app.data.repository.AuthRepository
import com.parkable.app.data.repository.ListingRepository
import com.parkable.app.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PublishUiState(
    val photos: List<Uri> = emptyList(),
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pricePerHour: String = "",
    val pricePerDay: String = "",
    val pricePerWeek: String = "",
    val pricePerMonth: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class ListingViewModel(
    private val repo: ListingRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    val available: StateFlow<List<Listing>> = repo.observeAvailableListings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _publish = MutableStateFlow(PublishUiState())
    val publish: StateFlow<PublishUiState> = _publish.asStateFlow()

    fun addPhoto(uri: Uri) {
        _publish.value = _publish.value.copy(photos = _publish.value.photos + uri)
    }

    fun removePhoto(uri: Uri) {
        _publish.value = _publish.value.copy(photos = _publish.value.photos - uri)
    }

    fun updateField(transform: (PublishUiState) -> PublishUiState) {
        _publish.value = transform(_publish.value)
    }

    fun resetPublish() { _publish.value = PublishUiState() }

    fun publishListing() {
        val state = _publish.value
        val owner = authRepo.currentUser ?: return
        if (state.photos.isEmpty()) {
            _publish.value = state.copy(error = "min_photos"); return
        }
        if (state.title.isBlank() || state.latitude == null || state.longitude == null) {
            _publish.value = state.copy(error = "missing_fields"); return
        }
        _publish.value = state.copy(saving = true, error = null)
        viewModelScope.launch {
            runCatching {
                val urls = repo.uploadPhotos(owner.uid, state.photos)
                val listing = Listing(
                    ownerId = owner.uid,
                    ownerName = owner.displayName ?: owner.email ?: "Anónimo",
                    title = state.title,
                    description = state.description,
                    photos = urls,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    address = state.address,
                    pricePerHour = state.pricePerHour.toDoubleOrNull(),
                    pricePerDay = state.pricePerDay.toDoubleOrNull(),
                    pricePerWeek = state.pricePerWeek.toDoubleOrNull(),
                    pricePerMonth = state.pricePerMonth.toDoubleOrNull()
                )
                repo.createListing(listing)
            }
                .onSuccess { _publish.value = _publish.value.copy(saving = false, saved = true) }
                .onFailure { _publish.value = _publish.value.copy(saving = false, error = it.message) }
        }
    }

    fun book(
        listing: Listing,
        unit: RentalUnit,
        quantity: Int,
        total: Double,
        onDone: () -> Unit,
        onError: () -> Unit = {}
    ) {
        val renter = authRepo.currentUser ?: return
        viewModelScope.launch {
            // Comprueba que la plaza sigue disponible en Firestore antes de crear la reserva
            val current = repo.getListing(listing.id)
            if (current == null || !current.available) {
                onError()
                return@launch
            }
            val booking = Booking(
                listingId = listing.id,
                renterId = renter.uid,
                renterName = renter.displayName ?: renter.email ?: "Usuario",
                ownerId = listing.ownerId,
                unit = unit,
                quantity = quantity,
                totalPrice = total,
                status = "CONFIRMED"
            )
            runCatching {
                repo.createBooking(booking)
                repo.setAvailability(listing.id, false)
            }
                .onSuccess {
                    NotificationHelper.showBookingConfirmed(listing.title, listing.address)
                    onDone()
                }
                .onFailure { onError() }
        }
    }
}