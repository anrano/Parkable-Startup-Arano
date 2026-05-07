package com.parkable.app.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.ui.components.ParkableCard
import com.parkable.app.ui.components.ParkablePrimaryButton
import com.parkable.app.ui.theme.ParkableGradients
import com.parkable.app.ui.theme.ParkableGreen

/**
 * Pantalla inicial de selección de idioma. Aparece la primera vez que se abre la app
 * (y también desde Ajustes). El cambio se persiste en DataStore y se aplica en caliente.
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageChosen: (String) -> Unit,
    initialLanguage: String? = null
) {
    var selected by remember { mutableStateOf(initialLanguage ?: "es") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banda decorativa superior con gradiente corporativo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(ParkableGradients.brandVertical)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Parkable",
                    modifier = Modifier.size(96.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Parkable",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                stringResource(R.string.app_tagline),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(48.dp))

            Text(
                stringResource(R.string.language_select_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(24.dp))

            LanguageOption(
                code = "es",
                label = stringResource(R.string.language_spanish),
                flag = "🇪🇸",
                selected = selected == "es",
                onClick = { selected = "es" }
            )
            Spacer(Modifier.height(12.dp))
            LanguageOption(
                code = "en",
                label = stringResource(R.string.language_english),
                flag = "🇬🇧",
                selected = selected == "en",
                onClick = { selected = "en" }
            )

            Spacer(Modifier.weight(1f))

            ParkablePrimaryButton(
                text = stringResource(R.string.continue_action),
                onClick = { onLanguageChosen(selected) }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LanguageOption(
    code: String,
    label: String,
    flag: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ParkableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(flag, style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(
                    code.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) ParkableGreen else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
