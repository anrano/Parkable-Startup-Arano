package com.parkable.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.parkable.app.data.repository.AlertRepository
import com.parkable.app.data.repository.AuthRepository
import com.parkable.app.data.repository.ListingRepository
import com.parkable.app.data.repository.UserRepository
import com.parkable.app.locale.PreferencesRepository

class AppViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {

    private val authRepo by lazy { AuthRepository() }
    private val userRepo by lazy { UserRepository() }
    private val listingRepo by lazy { ListingRepository(appContext) }
    private val alertRepo by lazy { AlertRepository() }
    private val prefsRepo by lazy { PreferencesRepository(appContext) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(authRepo, userRepo) as T
            modelClass.isAssignableFrom(ListingViewModel::class.java) ->
                ListingViewModel(listingRepo, authRepo) as T
            modelClass.isAssignableFrom(AlertViewModel::class.java) ->
                AlertViewModel(alertRepo, userRepo, authRepo) as T
            modelClass.isAssignableFrom(PointsViewModel::class.java) ->
                PointsViewModel(userRepo, authRepo) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(prefsRepo) as T
            else -> error("ViewModel no soportado: ${modelClass.name}")
        }
    }
}