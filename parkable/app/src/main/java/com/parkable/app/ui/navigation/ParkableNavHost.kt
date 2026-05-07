package com.parkable.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.parkable.app.R
import com.parkable.app.data.model.Listing
import com.parkable.app.data.model.RentalUnit
import com.parkable.app.ui.screens.auth.LoginScreen
import com.parkable.app.ui.screens.home.HomeScreen
import com.parkable.app.ui.screens.marketplace.ListingDetailScreen
import com.parkable.app.ui.screens.marketplace.MarketplaceScreen
import com.parkable.app.ui.screens.marketplace.PaymentScreen
import com.parkable.app.ui.screens.marketplace.PublishListingScreen
import com.parkable.app.ui.screens.points.PointsScreen
import com.parkable.app.ui.screens.profile.ProfileScreen
import com.parkable.app.ui.screens.settings.LanguageSelectionScreen
import com.parkable.app.ui.screens.settings.SettingsScreen
import com.parkable.app.ui.screens.socialdrive.AlertDetailScreen
import com.parkable.app.ui.screens.socialdrive.NewAlertScreen
import com.parkable.app.ui.screens.socialdrive.SocialDriveScreen
import com.parkable.app.viewmodel.AlertViewModel
import com.parkable.app.viewmodel.AppViewModelFactory
import com.parkable.app.viewmodel.AuthViewModel
import com.parkable.app.viewmodel.ListingViewModel
import com.parkable.app.viewmodel.PointsViewModel
import com.parkable.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * NavHost raíz. Controla:
 *  - Selector de idioma (sólo primera vez si no se ha hecho onboarding).
 *  - Login si no hay usuario autenticado.
 *  - Bottom bar con las 5 secciones cuando hay usuario.
 */
@Composable
fun ParkableNavHost(
    factory: AppViewModelFactory,
    isLoggedIn: Boolean
) {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = factory)
    val listingVm: ListingViewModel = viewModel(factory = factory)
    val alertVm: AlertViewModel = viewModel(factory = factory)
    val pointsVm: PointsViewModel = viewModel(factory = factory)
    val settingsVm: SettingsViewModel = viewModel(factory = factory)

    val authedUser by authVm.authedUser.collectAsState()

    val start = when {
        !isLoggedIn -> Routes.LANGUAGE
        else -> Routes.HOME
    }

    // Reacción al logout: si nos quedamos sin usuario, volvemos al selector de idioma
    androidx.compose.runtime.LaunchedEffect(authedUser) {
        if (authedUser == null) {
            val current = nav.currentBackStackEntry?.destination?.route
            if (current != null && current != Routes.LOGIN && current != Routes.LANGUAGE) {
                nav.navigate(Routes.LANGUAGE) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            val navBackStack by nav.currentBackStackEntryAsState()
            val currentRoute = navBackStack?.destination?.route
            val showBottomBar = currentRoute in setOf(
                Routes.HOME, Routes.MARKETPLACE, Routes.SOCIAL,
                Routes.POINTS, Routes.PROFILE
            )
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    nav.navigate(item.route) {
                                        popUpTo(nav.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, null) },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LANGUAGE) {
                LanguageSelectionScreen(
                    onLanguageChosen = { code ->
                        settingsVm.setLanguage(code)
                        nav.navigate(if (authedUser == null) Routes.LOGIN else Routes.HOME) {
                            popUpTo(Routes.LANGUAGE) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    vm = authVm,
                    onAuthenticated = {
                        nav.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    authVm = authVm,
                    onGoMarketplace = { nav.navigate(Routes.MARKETPLACE) },
                    onGoSocialDrive = { nav.navigate(Routes.SOCIAL) },
                    onGoPublish = { nav.navigate(Routes.PUBLISH) }
                )
            }

            composable(Routes.MARKETPLACE) {
                MarketplaceScreen(
                    vm = listingVm,
                    onListingClick = { l -> nav.navigate(Routes.listingDetail(l.id)) },
                    onPublishClick = { nav.navigate(Routes.PUBLISH) }
                )
            }

            composable(
                Routes.LISTING_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                val available = listingVm.available.collectAsState().value
                val listing: Listing? = remember(available, id) { available.firstOrNull { it.id == id } }
                if (listing != null) {
                    ListingDetailScreen(
                        listing = listing,
                        onBack = { nav.popBackStack() },
                        onProceedToPayment = { unit, qty, total ->
                            nav.navigate(Routes.payment(listing.id, unit.name, qty, total))
                        }
                    )
                }
            }

            composable(Routes.PUBLISH) {
                PublishListingScreen(
                    vm = listingVm,
                    onBack = { nav.popBackStack() },
                    onPublished = { nav.popBackStack() }
                )
            }

            composable(
                Routes.PAYMENT,
                arguments = listOf(
                    navArgument("listingId") { type = NavType.StringType },
                    navArgument("unit") { type = NavType.StringType },
                    navArgument("qty") { type = NavType.IntType },
                    navArgument("total") { type = NavType.FloatType }
                )
            ) { entry ->
                val listingId = entry.arguments?.getString("listingId") ?: return@composable
                val unit = RentalUnit.valueOf(entry.arguments?.getString("unit") ?: "HOUR")
                val qty = entry.arguments?.getInt("qty") ?: 1
                val total = entry.arguments?.getFloat("total")?.toDouble() ?: 0.0
                val listing = listingVm.available.collectAsState().value.firstOrNull { it.id == listingId }
                val scope = rememberCoroutineScope()
                PaymentScreen(
                    total = total,
                    onBack = { nav.popBackStack() },
                    onPaid = {
                        scope.launch {
                            val navigate = {
                                nav.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                }
                            }
                            if (listing != null) {
                                listingVm.book(
                                    listing = listing,
                                    unit = unit,
                                    quantity = qty,
                                    total = total,
                                    onDone = navigate,
                                    onError = navigate
                                )
                            } else {
                                navigate()
                            }
                        }
                    }
                )
            }

            composable(Routes.SOCIAL) {
                SocialDriveScreen(
                    vm = alertVm,
                    onNewAlert = { nav.navigate(Routes.NEW_ALERT) },
                    onAlertClick = { a -> nav.navigate(Routes.alertDetail(a.id)) }
                )
            }

            composable(Routes.NEW_ALERT) {
                NewAlertScreen(
                    vm = alertVm,
                    onBack = { nav.popBackStack() },
                    onCreated = { nav.popBackStack() }
                )
            }

            composable(
                Routes.ALERT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                val alerts = alertVm.alerts.collectAsState().value
                val alert = remember(alerts, id) { alerts.firstOrNull { it.id == id } }
                if (alert != null) {
                    AlertDetailScreen(
                        alert = alert,
                        vm = alertVm,
                        onBack = { nav.popBackStack() }
                    )
                }
            }

            composable(Routes.POINTS) {
                PointsScreen(pointsVm = pointsVm, authVm = authVm)
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    authVm = authVm,
                    listingVm = listingVm,
                    alertVm = alertVm,
                    onSettings = { nav.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    vm = settingsVm,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

private data class BottomItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, R.string.nav_home, Icons.Default.Home),
    BottomItem(Routes.MARKETPLACE, R.string.nav_marketplace, Icons.Default.LocalParking),
    BottomItem(Routes.SOCIAL, R.string.nav_socialdrive, Icons.Default.DirectionsCar),
    BottomItem(Routes.POINTS, R.string.nav_points, Icons.Default.Stars),
    BottomItem(Routes.PROFILE, R.string.nav_profile, Icons.Default.Person)
)
