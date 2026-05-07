package com.parkable.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.ui.components.ParkableCard
import com.parkable.app.ui.components.PointsBadge
import com.parkable.app.ui.theme.ParkableBluePrimary
import com.parkable.app.ui.theme.ParkableGreen
import com.parkable.app.ui.theme.ParkableTeal
import com.parkable.app.viewmodel.AuthViewModel

/**
 * Home: tarjetas grandes hacia las dos funcionalidades principales (sector 1 y 2)
 * + saldo de puntos prominente. Pensada como entry point claro en cada sesión.
 */
@Composable
fun HomeScreen(
    authVm: AuthViewModel,
    onGoMarketplace: () -> Unit,
    onGoSocialDrive: () -> Unit,
    onGoPublish: () -> Unit
) {
    val user by authVm.authedUser.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_greeting, user?.name?.takeIf { it.isNotBlank() } ?: "👋"),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PointsBadge(points = user?.points ?: 0)
        }

        Spacer(Modifier.height(28.dp))

        // Tarjeta destacada: marketplace (alquilar plaza)
        BigActionCard(
            title = stringResource(R.string.home_card_marketplace_title),
            description = stringResource(R.string.home_card_marketplace_desc),
            icon = Icons.Default.LocalParking,
            background = Brush.linearGradient(listOf(ParkableBluePrimary, ParkableTeal)),
            onClick = onGoMarketplace
        )
        Spacer(Modifier.height(16.dp))

        // Tarjeta SocialDrive (foro de avisos)
        BigActionCard(
            title = stringResource(R.string.home_card_socialdrive_title),
            description = stringResource(R.string.home_card_socialdrive_desc),
            icon = Icons.Default.DirectionsCar,
            background = Brush.linearGradient(listOf(ParkableTeal, ParkableGreen)),
            onClick = onGoSocialDrive
        )
        Spacer(Modifier.height(16.dp))

        // Tarjeta publicar
        BigActionCard(
            title = stringResource(R.string.home_card_publish_title),
            description = stringResource(R.string.home_card_publish_desc),
            icon = Icons.Default.AddBusiness,
            background = Brush.linearGradient(listOf(ParkableGreen, ParkableTeal)),
            onClick = onGoPublish
        )

        Spacer(Modifier.height(28.dp))
        ParkableCard {
            Text(
                stringResource(R.string.home_points_balance),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${user?.points ?: 0}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.home_points_unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BigActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    background: Brush,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(
                    description,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
