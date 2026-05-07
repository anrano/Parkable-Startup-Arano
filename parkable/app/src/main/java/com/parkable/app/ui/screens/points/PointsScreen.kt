package com.parkable.app.ui.screens.points

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.data.model.PointsTransaction
import com.parkable.app.data.model.Reward
import com.parkable.app.ui.theme.ParkableGradients
import com.parkable.app.util.RewardsCatalog
import com.parkable.app.viewmodel.AuthViewModel
import com.parkable.app.viewmodel.PointsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen(
    pointsVm: PointsViewModel,
    authVm: AuthViewModel
) {
    val user by authVm.authedUser.collectAsState()
    val balance = user?.points ?: 0
    val history by pointsVm.history.collectAsState()
    val feedback by pointsVm.redeemFeedback.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val msgInsufficient = stringResource(R.string.points_insufficient)
    val msgRedeemedOk = stringResource(R.string.points_redeemed_ok)

    // Estado del diálogo de canje
    var redeemingReward by remember { mutableStateOf<Reward?>(null) }
    var dialogEmail by remember { mutableStateOf("") }
    var lastEmail by remember { mutableStateOf("") }

    LaunchedEffect(feedback) {
        when (feedback) {
            "ok" -> {
                snackbar.showSnackbar(
                    if (lastEmail.isNotBlank()) "¡Cupón enviado a $lastEmail!"
                    else msgRedeemedOk
                )
                pointsVm.consumeFeedback()
            }
            "insufficient" -> {
                snackbar.showSnackbar(msgInsufficient)
                pointsVm.consumeFeedback()
            }
        }
    }

    // Diálogo de email para canjear
    redeemingReward?.let { reward ->
        AlertDialog(
            onDismissRequest = { redeemingReward = null; dialogEmail = "" },
            title = { Text(stringResource(R.string.points_redeem_dialog_title)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.points_redeem_email_label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dialogEmail,
                        onValueChange = { dialogEmail = it },
                        label = { Text(stringResource(R.string.email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        lastEmail = dialogEmail
                        pointsVm.redeem(reward, balance)
                        redeemingReward = null
                        dialogEmail = ""
                    },
                    enabled = dialogEmail.contains("@")
                ) {
                    Text(stringResource(R.string.points_redeem_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { redeemingReward = null; dialogEmail = "" }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.points_title)) }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero saldo
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ParkableGradients.brandHorizontal)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            stringResource(R.string.points_balance_label),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "$balance",
                                color = Color.White,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.home_points_unit),
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            // Catálogo de recompensas
            item {
                Text(stringResource(R.string.points_rewards), style = MaterialTheme.typography.titleLarge)
            }
            items(RewardsCatalog.all.chunked(2)) { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { reward ->
                        Box(Modifier.weight(1f)) {
                            RewardCard(reward, balance) { redeemingReward = reward }
                        }
                    }
                    if (rowItems.size == 1) Box(Modifier.weight(1f))
                }
            }

            // Historial
            item {
                Text(stringResource(R.string.points_history), style = MaterialTheme.typography.titleLarge)
            }
            items(history, key = { it.id }) { tx -> HistoryRow(tx) }
            if (history.isEmpty()) {
                item {
                    Text(
                        "—",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardCard(reward: Reward, balance: Int, onRedeem: () -> Unit) {
    val icon: ImageVector = when (reward.iconName) {
        "LocalCarWash" -> Icons.Default.LocalCarWash
        "LocalGasStation" -> Icons.Default.LocalGasStation
        "WorkspacePremium" -> Icons.Default.WorkspacePremium
        "Coffee" -> Icons.Default.Coffee
        else -> Icons.Default.WorkspacePremium
    }
    val canRedeem = balance >= reward.cost
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(reward.titleRes), style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(reward.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${reward.cost} pts",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                FilledTonalButton(
                    onClick = onRedeem,
                    enabled = canRedeem,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.points_redeem), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(tx: PointsTransaction) {
    val date = tx.createdAt?.toDate()?.let {
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(it)
    } ?: ""
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tx.reason, style = MaterialTheme.typography.bodyMedium)
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                (if (tx.amount >= 0) "+" else "") + "${tx.amount}",
                color = if (tx.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}