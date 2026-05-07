package com.parkable.app.ui.screens.socialdrive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
import com.parkable.app.viewmodel.AlertViewModel

/**
 * Crea un aviso de plaza libre. El usuario toca el mapa para fijar la ubicación,
 * indica en cuántos minutos se va y opcionalmente añade notas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAlertScreen(
    vm: AlertViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    var address by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }

    val sevilla = LatLng(37.3886, -5.9823)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sevilla, 13f)
    }
    LaunchedEffect(lat, lng) {
        if (lat != null && lng != null) {
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat!!, lng!!), 16f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.social_new_alert)) },
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
            AddressAutocompleteField(
                value = address,
                onValueChange = { address = it },
                onPlaceSelected = { selectedAddress, selectedLat, selectedLng ->
                    address = selectedAddress
                    lat = selectedLat
                    lng = selectedLng
                },
                label = stringResource(R.string.social_alert_address)
            )
            Spacer(Modifier.height(8.dp))
            ParkableTextField(
                value = minutes,
                onValueChange = { v -> if (v.length <= 3) minutes = v.filter(Char::isDigit) },
                label = stringResource(R.string.social_alert_time),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(8.dp))
            ParkableTextField(
                value = notes,
                onValueChange = { notes = it },
                label = stringResource(R.string.social_alert_notes),
                singleLine = false
            )

            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                GoogleMap(
                    cameraPositionState = cameraState,
                    onMapClick = { p -> lat = p.latitude; lng = p.longitude }
                ) {
                    if (lat != null && lng != null) {
                        Marker(state = MarkerState(LatLng(lat!!, lng!!)))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            ParkablePrimaryButton(
                text = stringResource(R.string.social_alert_publish),
                enabled = lat != null && lng != null && minutes.isNotBlank(),
                onClick = {
                    vm.create(
                        latitude = lat!!,
                        longitude = lng!!,
                        address = address,
                        minutes = minutes.toIntOrNull() ?: 5,
                        notes = notes
                    )
                    onCreated()
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
