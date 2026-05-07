package com.parkable.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import androidx.lifecycle.lifecycleScope
import com.parkable.app.locale.LocaleHelper
import com.parkable.app.locale.PreferencesRepository
import com.parkable.app.ui.navigation.ParkableNavHost
import com.parkable.app.ui.theme.ParkableTheme
import com.parkable.app.viewmodel.AppViewModelFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Única Activity de la app. Estrategia:
 *  - attachBaseContext aplica el locale guardado para que los recursos string ya nazcan
 *    en el idioma correcto.
 *  - Si el idioma cambia más tarde mientras la app está abierta, se llama a recreate()
 *    para que la nueva Activity vuelva a leer la configuración y todo cambie en caliente.
 */
class MainActivity : ComponentActivity() {

    private lateinit var prefsRepo: PreferencesRepository

    override fun attachBaseContext(newBase: Context) {
        val saved = runBlocking {
            runCatching { PreferencesRepository(newBase).languageFlow.first() }.getOrNull()
        }
        super.attachBaseContext(LocaleHelper.wrap(newBase, saved))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        prefsRepo = PreferencesRepository(applicationContext)

        // Observa cambios de idioma para recrear la Activity y aplicar locale en caliente.
        lifecycleScope.launch {
            prefsRepo.languageFlow
                .distinctUntilChanged()
                .drop(1)              // ignoramos el valor inicial (ya aplicado)
                .collect { recreate() }
        }

        val factory = AppViewModelFactory(applicationContext)

        setContent {
            // Tema reactivo a la preferencia
            val themePref by prefsRepo.themeFlow
                .collectAsState(initial = "system")
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themePref) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            val isLoggedIn = com.parkable.app.data.firebase.FirebaseModule.auth.currentUser != null

            ParkableTheme(darkTheme = darkTheme) {
                @OptIn(ExperimentalPermissionsApi::class)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifPermission = rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    LaunchedEffect(Unit) { notifPermission.launchPermissionRequest() }
                }
                ParkableNavHost(
                    factory = factory,
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
}
