package com.parkable.app.locale

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore único de la app para preferencias ligeras (idioma, tema). */
val Context.dataStore by preferencesDataStore(name = "parkable_prefs")

class PreferencesRepository(private val context: Context) {

    private val keyLanguage = stringPreferencesKey("language")
    private val keyTheme = stringPreferencesKey("theme")
    private val keyOnboarded = stringPreferencesKey("onboarded")

    /** Códigos ISO 639-1 soportados. Si null -> aún no se ha elegido idioma. */
    val languageFlow: Flow<String?> =
        context.dataStore.data.map { it[keyLanguage] }

    val themeFlow: Flow<String> =
        context.dataStore.data.map { it[keyTheme] ?: "system" }

    val onboardedFlow: Flow<Boolean> =
        context.dataStore.data.map { it[keyOnboarded] == "true" }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[keyLanguage] = code }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[keyTheme] = theme }
    }

    suspend fun setOnboarded(done: Boolean) {
        context.dataStore.edit { it[keyOnboarded] = if (done) "true" else "false" }
    }
}
