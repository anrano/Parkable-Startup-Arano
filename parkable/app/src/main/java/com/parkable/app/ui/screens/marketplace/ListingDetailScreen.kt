package com.parkable.app.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import androidx.compose.runtime.remember
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.parkable.app.R
import com.parkable.app.data.model.Listing
import com.parkable.app.data.model.RentalUnit
import com.parkable.app.ui.components.ParkablePrimaryButton
import com.parkable.app.ui.components.ParkableSecondaryButton

/**
 * Detalle del anuncio. Muestra galería horizontal, descripción, mapa con la ubicación
 * exacta y selector de modalidad/cantidad antes de pasar a la pasarela de pago.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listing: Listing,
    onBack: () -> Unit,
    onProceedToPayment: (RentalUnit, Int, Double) -> Unit
) {
    var unit by remember { mutableStateOf(RentalUnit.HOUR) }
    var qty by remember { mutableStateOf(1) }

    // Calcula precio en función de la modalidad elegida y unidades
    val unitPrice: Double? = when (unit) {
        RentalUnit.HOUR -> listing.pricePerHour
        RentalUnit.DAY -> listing.pricePerDay
        RentalUnit.WEEK -> listing.pricePerWeek
        RentalUnit.MONTH -> listing.pricePerMonth
    }
    val total = (unitPrice ?: 0.0) * qty

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listing.title) },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Galería de fotos
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listing.photos) { url ->
                    AsyncImage(
                        model = remember(url) {
                            if (url.startsWith("data:image")) {
                                val base64 = url.substringAfter("base64,")
                                val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else url
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(220.dp)
                            .width(280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${stringResource(R.string.detail_owner)}: ${listing.ownerName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null)
                    Spacer(Modifier.width(8.dp))
                    Text(listing.address, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.detail_description),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(listing.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(16.dp))
                // Mapa con la ubicación
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val pos = LatLng(listing.latitude, listing.longitude)
                    val camera = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(pos, 16f)
                    }
                    GoogleMap(cameraPositionState = camera) {
                        Marker(state = MarkerState(pos), title = listing.title)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.detail_select_dates),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                // Chips de modalidad
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RentalChip(R.string.marketplace_filter_hour, unit == RentalUnit.HOUR,
                        listing.pricePerHour != null) { unit = RentalUnit.HOUR; qty = 1 }
                    RentalChip(R.string.marketplace_filter_day, unit == RentalUnit.DAY,
                        listing.pricePerDay != null) { unit = RentalUnit.DAY; qty = 1 }
                    RentalChip(R.string.marketplace_filter_week, unit == RentalUnit.WEEK,
                        listing.pricePerWeek != null) { unit = RentalUnit.WEEK; qty = 1 }
                    RentalChip(R.string.marketplace_filter_month, unit == RentalUnit.MONTH,
                        listing.pricePerMonth != null) { unit = RentalUnit.MONTH; qty = 1 }
                }

                Spacer(Modifier.height(16.dp))
                // Selector de cantidad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad: ")
                    IconButton(onClick = { if (qty > 1) qty-- }) { Text("−") }
                    Text("$qty", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { qty++ }) { Text("+") }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${stringResource(R.string.detail_total)}: %.2f €".format(total),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.height(20.dp))
                ParkablePrimaryButton(
                    text = stringResource(R.string.detail_book_now),
                    enabled = unitPrice != null && qty > 0,
                    onClick = { onProceedToPayment(unit, qty, total) }
                )
                Spacer(Modifier.height(12.dp))
                ParkableSecondaryButton(
                    text = stringResource(R.string.back),
                    onClick = onBack
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RentalChip(
    @androidx.annotation.StringRes labelRes: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        label = { Text(stringResource(labelRes)) }
    )
}
