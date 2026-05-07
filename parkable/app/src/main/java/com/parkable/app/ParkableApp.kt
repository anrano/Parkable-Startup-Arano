package com.parkable.app

import android.app.Application
import android.content.Context
import com.parkable.app.locale.LocaleHelper
import com.parkable.app.locale.PreferencesRepository
import com.parkable.app.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Application raíz. Aplica el locale guardado en DataStore antes de que se carguen
 * los recursos de las actividades, garantizando que la app abre directamente en el
 * idioma elegido por el usuario.
 */
class ParkableApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.init(this)
    }

    override fun attachBaseContext(base: Context) {
        // Lee el idioma guardado de forma síncrona (única excepción a "no runBlocking")
        // porque ocurre antes de que cualquier coroutine pueda ejecutarse.
        val saved = runBlocking {
            runCatching { PreferencesRepository(base).languageFlow.first() }.getOrNull()
        }
        super.attachBaseContext(LocaleHelper.wrap(base, saved))
    }
}
