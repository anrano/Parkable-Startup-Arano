package com.parkable.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.parkable.app.R
import com.parkable.app.ui.components.ParkablePrimaryButton
import com.parkable.app.ui.components.ParkableTextField
import com.parkable.app.ui.theme.ParkableGradients
import com.parkable.app.viewmodel.AuthViewModel

/**
 * Pantalla de autenticación con dos modos (login/registro) controlados por el toggle inferior.
 * Mantenerlo en una sola pantalla simplifica la navegación y reduce la fricción de uso.
 */
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val ui by vm.ui.collectAsState()
    val authedUser by vm.authedUser.collectAsState()

    LaunchedEffect(authedUser) { if (authedUser != null) onAuthenticated() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(ParkableGradients.brandVertical)
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Parkable",
                    modifier = Modifier.size(72.dp)
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                if (isRegister) stringResource(R.string.register_title)
                else stringResource(R.string.login_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                if (isRegister) stringResource(R.string.register_subtitle)
                else stringResource(R.string.login_subtitle),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(40.dp))

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    if (isRegister) {
                        ParkableTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = stringResource(R.string.full_name),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    ParkableTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = { Icon(Icons.Default.Email, null) }
                    )
                    Spacer(Modifier.height(12.dp))
                    ParkableTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password),
                        isPassword = true,
                        leadingIcon = { Icon(Icons.Default.Lock, null) }
                    )
                    Spacer(Modifier.height(20.dp))

                    ui.error?.let {
                        val msg = if (it == "empty") stringResource(R.string.error_empty_fields) else it
                        Text(
                            msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    ParkablePrimaryButton(
                        text = if (isRegister) stringResource(R.string.register_action)
                        else stringResource(R.string.login_action),
                        loading = ui.loading,
                        onClick = {
                            if (isRegister) vm.register(name, email, password)
                            else vm.login(email, password)
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = { isRegister = !isRegister; vm.clearError() }) {
                Text(
                    if (isRegister) stringResource(R.string.have_account)
                    else stringResource(R.string.no_account)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
