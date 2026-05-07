package com.parkable.app.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.parkable.app.viewmodel.ListingViewModel

/**
 * Marketplace: alterna entre vista de lista (anuncios estilo Wallapop) y mapa
 * (todos los anuncios geoposicionados con Google Maps Compose).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    vm: ListingViewModel,
    onListingClick: (Listing) -> Unit,
    onPublishClick: () -> Unit
) {
    val listings by vm.available.collectAsState()
    var mapView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.marketplace_title)) },
                actions = {
                    IconButton(onClick = { mapView = !mapView }) {
                        Icon(
                            imageVector = if (mapView) Icons.Default.ViewList else Icons.Default.Map,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPublishClick,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.marketplace_publish_fab)) }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (mapView) {
                MarketplaceMap(listings, onListingClick)
            } else {
                if (listings.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.marketplace_empty))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listings, key = { it.id }) { ListingCard(it) { onListingClick(it) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceMap(
    listings: List<Listing>,
    onListingClick: (Listing) -> Unit
) {
    // Centrado por defecto en Sevilla (sede de Parkable)
    val sevilla = LatLng(37.3886, -5.9823)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sevilla, 12f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState
    ) {
        listings.forEach { l ->
            Marker(
                state = MarkerState(LatLng(l.latitude, l.longitude)),
                title = l.title,
                snippet = l.address,
                onInfoWindowClick = { onListingClick(l) }
            )
        }
    }
}

@Composable
private fun ListingCard(listing: Listing, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(170.dp)) {
                if (listing.photos.isNotEmpty()) {
                    val url = listing.photos.first()
                    AsyncImage(
                        model = remember(url) {
                            if (url.startsWith("data:image")) {
                                val base64 = url.substringAfter("base64,")
                                val bytes = android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else url
                        },
                        contentDescription = listing.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                // Pastilla con precio "desde X"
                val cheapest = listOfNotNull(
                    listing.pricePerHour?.let { "$it €/h" },
                    listing.pricePerDay?.let { "$it €/día" }
                ).firstOrNull()
                if (cheapest != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            cheapest,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Column(Modifier.padding(14.dp)) {
                Text(listing.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        listing.address.ifBlank { "—" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
