package com.parkable.app.ui.screens.socialdrive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.parkable.app.R
import com.parkable.app.data.firebase.FirebaseModule
import com.parkable.app.data.model.AlertStatus
import com.parkable.app.data.model.ParkingAlert
import com.parkable.app.ui.components.ParkablePrimaryButton
import com.parkable.app.ui.components.ParkableSecondaryButton
import com.parkable.app.viewmodel.AlertViewModel

/**
 * Detalle del aviso. Muestra acciones distintas según el rol del usuario actual:
 *  - Visitante: puede reclamar (si OPEN).
 *  - Reclamante: puede confirmar "he aparcado".
 *  - Ofreciente: puede confirmar la entrega.
 *  Cuando ambos han confirmado, el ViewModel reparte puntos automáticamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(
    alert: ParkingAlert,
    vm: AlertViewModel,
    onBack: () -> Unit
) {
    // Re-observamos la lista para que esta pantalla refleje cambios en tiempo real
    val live by vm.alerts.collectAsState()
    val current = remember(live, alert.id) {
        live.firstOrNull { it.id == alert.id } ?: alert
    }
    val currentUid = FirebaseModule.auth.currentUser?.uid
    val isOfferer = currentUid == current.offererId
    val isClaimer = currentUid == current.claimerId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.social_title)) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.social_offered_by, current.offererName),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(current.addressHint.ifBlank { "—" })
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${current.leavingInMinutes} ${stringResource(R.string.social_minutes_short)}")
                    }
                    if (current.notes.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(current.notes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Mapa con la ubicación del aviso
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                val pos = LatLng(current.latitude, current.longitude)
                val cam = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(pos, 16f)
                }
                GoogleMap(cameraPositionState = cam) {
                    Marker(state = MarkerState(pos))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Estado y acciones según rol
            when (current.status) {
                AlertStatus.OPEN -> {
                    if (!isOfferer) {
                        ParkablePrimaryButton(
                            text = stringResource(R.string.social_alert_taken),
                            onClick = { vm.claim(current) }
                        )
                    } else {
                        StatusRow(
                            text = stringResource(R.string.social_alert_pending),
                            icon = Icons.Default.HourglassTop
                        )
                    }
                }
                AlertStatus.CLAIMED -> {
                    when {
                        isClaimer && !current.claimerConfirmed -> {
                            ParkablePrimaryButton(
                                text = stringResource(R.string.social_alert_arrived),
                                onClick = { vm.confirm(current) }
                            )
                        }
                        isOfferer && !current.offererConfirmed -> {
                            ParkablePrimaryButton(
                                text = stringResource(R.string.social_alert_confirm),
                                onClick = { vm.confirm(current) }
                            )
                        }
                        else -> {
                            StatusRow(
                                text = stringResource(R.string.social_alert_pending),
                                icon = Icons.Default.HourglassTop
                            )
                        }
                    }
                }
                AlertStatus.COMPLETED -> {
                    StatusRow(
                        text = stringResource(R.string.social_alert_completed),
                        icon = Icons.Default.CheckCircle
                    )
                }
                AlertStatus.CANCELLED -> {
                    StatusRow(text = "—", icon = Icons.Default.HourglassTop)
                }
            }

            Spacer(Modifier.height(12.dp))
            ParkableSecondaryButton(
                text = stringResource(R.string.back),
                onClick = onBack
            )
        }
    }
}

@Composable
private fun StatusRow(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
