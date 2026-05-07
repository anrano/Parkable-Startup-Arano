package com.parkable.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkable.app.locale.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: PreferencesRepository
) : ViewModel() {

    val language: StateFlow<String?> = prefs.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val theme: StateFlow<String> = prefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val onboarded: StateFlow<Boolean> = prefs.onboardedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setLanguage(code: String) = viewModelScope.launch {
        prefs.setLanguage(code)
        prefs.setOnboarded(true)
    }

    fun setTheme(theme: String) = viewModelScope.launch { prefs.setTheme(theme) }
}
