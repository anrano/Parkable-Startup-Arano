package com.parkable.app.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Aplica un Locale concreto a un contexto. Se invoca tanto desde [Application.attachBaseContext]
 * (carga inicial) como desde la actividad cuando el usuario cambia el idioma en caliente.
 */
object LocaleHelper {

    fun wrap(context: Context, languageCode: String?): Context {
        if (languageCode.isNullOrBlank()) return context
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(android.os.LocaleList(locale))
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
