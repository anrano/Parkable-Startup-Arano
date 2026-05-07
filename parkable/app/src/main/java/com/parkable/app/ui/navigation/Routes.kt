package com.parkable.app.ui.navigation

/** Rutas (constantes) usadas por NavController. */
object Routes {
    const val LANGUAGE = "language"
    const val LOGIN = "login"

    const val HOME = "home"
    const val MARKETPLACE = "marketplace"
    const val SOCIAL = "social"
    const val POINTS = "points"
    const val PROFILE = "profile"

    const val LISTING_DETAIL = "listing/{id}"
    const val PUBLISH = "publish"
    const val PAYMENT = "payment/{listingId}/{unit}/{qty}/{total}"

    const val NEW_ALERT = "new_alert"
    const val ALERT_DETAIL = "alert/{id}"

    const val SETTINGS = "settings"

    fun listingDetail(id: String) = "listing/$id"
    fun alertDetail(id: String) = "alert/$id"
    fun payment(listingId: String, unit: String, qty: Int, total: Double) =
        "payment/$listingId/$unit/$qty/$total"
}
