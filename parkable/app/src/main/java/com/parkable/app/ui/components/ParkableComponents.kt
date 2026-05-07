package com.parkable.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.parkable.app.ui.theme.ParkableGradients

/**
 * Botón primario con gradiente corporativo. Es el CTA principal de la app y se usa
 * por todas las pantallas para mantener consistencia visual.
 */
@Composable
fun ParkablePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val alpha by animateFloatAsState(if (enabled) 1f else 0.5f, label = "alpha")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .clip(RoundedCornerShape(28.dp))
            .background(ParkableGradients.brandHorizontal)
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                color = Color.White.copy(alpha = alpha),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Botón secundario (outline) con el color primario. Para acciones no destructivas. */
@Composable
fun ParkableSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * TextField con estilo Parkable: bordes redondeados, color primario y soporte
 * para alternar visibilidad de contraseña.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        isError = isError,
        keyboardOptions = keyboardOptions,
        visualTransformation = if (isPassword && !visible)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

/** Card "glass" con bordes redondeados y sombra suave. Estilo moderno solicitado. */
@Composable
fun ParkableCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Brush? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))

    val finalModifier = if (background != null) {
        baseModifier.background(background)
    } else {
        baseModifier.background(MaterialTheme.colorScheme.surface)
    }

    val clickModifier = if (onClick != null) finalModifier.clickable { onClick() } else finalModifier

    Column(
        modifier = clickModifier.padding(contentPadding),
        content = content
    )
}

/** Badge para puntos. Pequeña pastilla con número + sufijo "pts". */
@Composable
fun PointsBadge(points: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ParkableGradients.brandHorizontal)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$points pts",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
