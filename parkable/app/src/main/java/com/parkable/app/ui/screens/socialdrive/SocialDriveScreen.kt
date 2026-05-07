package com.parkable.app.ui.screens.socialdrive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.data.model.AlertStatus
import com.parkable.app.data.model.ParkingAlert
import com.parkable.app.ui.theme.ParkableGradients
import com.parkable.app.viewmodel.AlertViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialDriveScreen(
    vm: AlertViewModel,
    onNewAlert: () -> Unit,
    onAlertClick: (ParkingAlert) -> Unit
) {
    val activeAlerts by vm.alerts.collectAsState()
    val completedAlerts by vm.completedAlerts.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        stringResource(R.string.social_tab_active),
        stringResource(R.string.social_tab_completed)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(stringResource(R.string.social_title))
                    Text(
                        stringResource(R.string.social_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            })
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNewAlert,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.social_new_alert)) }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Banner de puntos
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(ParkableGradients.brandHorizontal)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PointsLegend("+100", stringResource(R.string.points_earn_provide))
                    Spacer(Modifier.width(16.dp))
                    PointsLegend("+25", stringResource(R.string.points_earn_park))
                }
            }

            // Pestañas
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Contenido según pestaña seleccionada
            val displayList = if (selectedTab == 0) activeAlerts else completedAlerts
            val emptyText = if (selectedTab == 0)
                stringResource(R.string.social_empty)
            else
                stringResource(R.string.social_empty_completed)

            if (displayList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(emptyText)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayList, key = { it.id }) { alert ->
                        AlertCard(alert) { onAlertClick(alert) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsLegend(amount: String, label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(50)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(amount, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AlertCard(alert: ParkingAlert, onClick: () -> Unit) {
    val statusColor = when (alert.status) {
        AlertStatus.OPEN -> MaterialTheme.colorScheme.secondary
        AlertStatus.CLAIMED -> MaterialTheme.colorScheme.tertiary
        AlertStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        AlertStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(statusColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.social_offered_by, alert.offererName),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(alert.addressHint.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${alert.leavingInMinutes} ${stringResource(R.string.social_minutes_short)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (alert.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    alert.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}