package com.parkable.app.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.ui.components.ParkablePrimaryButton
import com.parkable.app.ui.components.ParkableTextField
import com.parkable.app.ui.theme.ParkableGradients
import kotlinx.coroutines.delay

/**
 * Pasarela de pago **simulada**. En un entorno real se integraría Stripe Elements,
 * Bizum, Apple Pay, Google Pay, etc. Para el TFG se imita el flujo (validación de
 * campos, "procesando…", confirmación) sin enviar datos reales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    total: Double,
    onBack: () -> Unit,
    onPaid: () -> Unit
) {
    var card by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(processing) {
        if (processing) {
            delay(1500)        // simula latencia de la pasarela
            processing = false
            done = true
            delay(1200)
            onPaid()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.payment_title)) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Aviso de simulación (importante para honestidad académica)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.payment_simulation_notice),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Tarjeta de crédito decorativa
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ParkableGradients.brandHorizontal)
                    .padding(20.dp)
            ) {
                Column {
                    Icon(Icons.Default.CreditCard, null, tint = Color.White)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        if (card.isBlank()) "•••• •••• •••• ••••" else card.chunked(4).joinToString(" "),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        holder.ifBlank { "TITULAR" }.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            ParkableTextField(
                value = card,
                onValueChange = { if (it.length <= 16) card = it.filter(Char::isDigit) },
                label = stringResource(R.string.payment_card_number),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(8.dp))
            ParkableTextField(
                value = holder,
                onValueChange = { holder = it },
                label = stringResource(R.string.payment_card_holder)
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Box(Modifier.weight(1f)) {
                    ParkableTextField(
                        value = expiry,
                        onValueChange = { if (it.length <= 5) expiry = it },
                        label = stringResource(R.string.payment_expiry)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    ParkableTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4) cvv = it.filter(Char::isDigit) },
                        label = stringResource(R.string.payment_cvv),
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (done) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.payment_success),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            ParkablePrimaryButton(
                text = stringResource(R.string.payment_pay, "%.2f".format(total)),
                loading = processing,
                enabled = card.length >= 13 && holder.isNotBlank() && expiry.isNotBlank() && cvv.length >= 3 && !done,
                onClick = { processing = true }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
