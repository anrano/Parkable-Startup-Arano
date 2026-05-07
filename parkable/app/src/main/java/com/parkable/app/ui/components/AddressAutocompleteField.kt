package com.parkable.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.parkable.app.data.GeoPlace
import com.parkable.app.data.GeocodingService
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (address: String, lat: Double, lng: Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var suggestions by remember { mutableStateOf<List<GeoPlace>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var skipSearch by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (skipSearch) { skipSearch = false; return@LaunchedEffect }
        if (value.length < 3) { suggestions = emptyList(); expanded = false; return@LaunchedEffect }
        delay(500)
        loading = true
        suggestions = GeocodingService.search(value)
        expanded = suggestions.isNotEmpty()
        loading = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!it) expanded = false },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { v ->
                onValueChange(v)
                if (v.isEmpty()) { suggestions = emptyList(); expanded = false }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            trailingIcon = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        if (expanded && suggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { place ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = place.displayName,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            skipSearch = true
                            onValueChange(place.displayName)
                            onPlaceSelected(
                                place.displayName,
                                place.lat.toDouble(),
                                place.lon.toDouble()
                            )
                            expanded = false
                            suggestions = emptyList()
                        }
                    )
                }
            }
        }
    }
}