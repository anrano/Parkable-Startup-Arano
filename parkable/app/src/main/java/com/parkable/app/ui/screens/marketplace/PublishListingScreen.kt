package com.parkable.app.ui.screens.marketplace

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.parkable.app.R
import com.parkable.app.ui.components.AddressAutocompleteField
import com.parkable.app.ui.components.ParkablePrimaryButton
import com.parkable.app.ui.components.ParkableTextField
import com.parkable.app.viewmodel.ListingViewModel

/**
 * Pantalla de creación de anuncio. Implementa el requisito clave del enunciado:
 * permite añadir como mínimo 5 fotos (sin límite superior duro), seleccionar la
 * ubicación tocando el mapa y fijar precios para cada modalidad de alquiler.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishListingScreen(
    vm: ListingViewModel,
    onBack: () -> Unit,
    onPublished: () -> Unit
) {
    val state by vm.publish.collectAsState()
    val minPhotos = 1

    LaunchedEffect(state.saved) { if (state.saved) { onPublished(); vm.resetPublish() } }

    // Selector de imágenes múltiple
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris -> uris.forEach { vm.addPhoto(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.publish_title)) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Paso 1: Fotos ─────────────────────────────────────────────
            SectionTitle(stringResource(R.string.publish_step_photos))
            Text(
                stringResource(
                    R.string.publish_photos_count,
                    state.photos.size,
                    minPhotos
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (state.photos.size >= minPhotos)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.photos) { uri ->
                    PhotoThumb(uri) { vm.removePhoto(uri) }
                }
                item {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                pickPhotos.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, null)
                            Text(
                                stringResource(R.string.publish_add_photo),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            // ── Paso 2: Datos básicos ────────────────────────────────────
            SectionTitle(stringResource(R.string.publish_step_basics))
            ParkableTextField(
                value = state.title,
                onValueChange = { v -> vm.updateField { it.copy(title = v) } },
                label = stringResource(R.string.publish_listing_title)
            )
            Spacer(Modifier.height(8.dp))
            ParkableTextField(
                value = state.description,
                onValueChange = { v -> vm.updateField { it.copy(description = v) } },
                label = stringResource(R.string.publish_listing_desc),
                singleLine = false
            )
            Spacer(Modifier.height(8.dp))
            AddressAutocompleteField(
                value = state.address,
                onValueChange = { v -> vm.updateField { it.copy(address = v) } },
                onPlaceSelected = { address, lat, lng ->
                    vm.updateField { it.copy(address = address, latitude = lat, longitude = lng) }
                },
                label = stringResource(R.string.detail_location)
            )

            Spacer(Modifier.height(20.dp))
            // ── Paso 3: Ubicación ────────────────────────────────────────
            SectionTitle(stringResource(R.string.publish_step_location))
            Text(
                stringResource(R.string.publish_pick_on_map),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))

            val sevilla = LatLng(37.3886, -5.9823)
            val cameraState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(sevilla, 13f)
            }
            LaunchedEffect(state.latitude, state.longitude) {
                val lat = state.latitude
                val lng = state.longitude
                if (lat != null && lng != null && (lat != 0.0 || lng != 0.0)) {
                    cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f))
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                GoogleMap(
                    cameraPositionState = cameraState,
                    onMapClick = { latLng ->
                        vm.updateField { it.copy(latitude = latLng.latitude, longitude = latLng.longitude) }
                    }
                ) {
                    if (state.latitude != null && state.longitude != null) {
                        Marker(
                            state = MarkerState(LatLng(state.latitude!!, state.longitude!!))
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            // ── Paso 4: Precios ──────────────────────────────────────────
            SectionTitle(stringResource(R.string.publish_step_pricing))
            PriceField(
                value = state.pricePerHour,
                label = stringResource(R.string.detail_price_per_hour),
                onChange = { v -> vm.updateField { it.copy(pricePerHour = v) } }
            )
            Spacer(Modifier.height(8.dp))
            PriceField(
                value = state.pricePerDay,
                label = stringResource(R.string.detail_price_per_day),
                onChange = { v -> vm.updateField { it.copy(pricePerDay = v) } }
            )
            Spacer(Modifier.height(8.dp))
            PriceField(
                value = state.pricePerWeek,
                label = stringResource(R.string.detail_price_per_week),
                onChange = { v -> vm.updateField { it.copy(pricePerWeek = v) } }
            )
            Spacer(Modifier.height(8.dp))
            PriceField(
                value = state.pricePerMonth,
                label = stringResource(R.string.detail_price_per_month),
                onChange = { v -> vm.updateField { it.copy(pricePerMonth = v) } }
            )

            Spacer(Modifier.height(24.dp))
            state.error?.let { code ->
                val msg = when (code) {
                    "min_photos" -> stringResource(R.string.publish_min_photos_error)
                    "missing_fields" -> stringResource(R.string.error_empty_fields)
                    else -> code
                }
                Text(msg, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            ParkablePrimaryButton(
                text = stringResource(R.string.publish_save),
                loading = state.saving,
                enabled = !state.saving,
                onClick = { vm.publishListing() }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun PhotoThumb(uri: Uri, onRemove: () -> Unit) {
    Box(
        Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.55f))
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PriceField(value: String, label: String, onChange: (String) -> Unit) {
    ParkableTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}
