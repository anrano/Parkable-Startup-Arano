package com.parkable.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.data.model.Listing
import com.parkable.app.data.model.ParkingAlert
import com.parkable.app.ui.components.ParkableSecondaryButton
import com.parkable.app.ui.components.PointsBadge
import com.parkable.app.ui.theme.ParkableGradients
import com.parkable.app.viewmodel.AuthViewModel
import com.parkable.app.viewmodel.ListingViewModel
import com.parkable.app.viewmodel.AlertViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authVm: AuthViewModel,
    listingVm: ListingViewModel,
    alertVm: AlertViewModel,
    onSettings: () -> Unit
) {
    val user by authVm.authedUser.collectAsState()
    val listings by listingVm.available.collectAsState()
    val alerts by alertVm.alerts.collectAsState()

    val myListings = listings.filter { it.ownerId == user?.uid }
    val myAlerts = alerts.filter { it.offererId == user?.uid }

    var expanded by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                actions = {
                    IconButton(onSettings) { Icon(Icons.Default.Settings, null) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp)
        ) {
            // Tarjeta perfil
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ParkableGradients.brandHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.name?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user?.name ?: "—", style = MaterialTheme.typography.titleLarge)
                        Text(
                            user?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        user?.createdAt?.toDate()?.let {
                            val fmt = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it)
                            Text(
                                stringResource(R.string.profile_member_since, fmt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    PointsBadge(points = user?.points ?: 0)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mis plazas publicadas
            ExpandableSection(
                title = stringResource(R.string.profile_my_listings),
                isExpanded = expanded == "listings",
                onToggle = { expanded = if (expanded == "listings") null else "listings" }
            ) {
                if (myListings.isEmpty()) {
                    Text(
                        "No tienes plazas publicadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    myListings.forEach { listing ->
                        ListingRow(listing)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mis reservas
            ExpandableSection(
                title = stringResource(R.string.profile_my_bookings),
                isExpanded = expanded == "bookings",
                onToggle = { expanded = if (expanded == "bookings") null else "bookings" }
            ) {
                Text(
                    "Reservas activas próximamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Mis avisos
            ExpandableSection(
                title = stringResource(R.string.profile_my_alerts),
                isExpanded = expanded == "alerts",
                onToggle = { expanded = if (expanded == "alerts") null else "alerts" }
            ) {
                if (myAlerts.isEmpty()) {
                    Text(
                        "No tienes avisos publicados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    myAlerts.forEach { alert ->
                        AlertRow(alert)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            ParkableSecondaryButton(
                text = stringResource(R.string.logout),
                onClick = { authVm.logout() }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onToggle) {
                    Text(if (isExpanded) "▲ Cerrar" else "▼ Ver")
                }
            }
            if (isExpanded) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ListingRow(listing: Listing) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(listing.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    listing.address.ifBlank { "Sin dirección" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            listing.pricePerHour?.let {
                Text("$it €/h", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AlertRow(alert: ParkingAlert) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    alert.addressHint.ifBlank { "Sin dirección" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${alert.leavingInMinutes} min · ${alert.status.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}