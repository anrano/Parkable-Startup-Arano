package com.parkable.app.util

import com.parkable.app.R
import com.parkable.app.data.model.Reward

/**
 * Catálogo estático de recompensas. Para un TFG es perfectamente válido
 * tenerlo en código; si en producción se quisiera modificar sin re-publicar,
 * se movería a Firestore o Remote Config.
 */
object RewardsCatalog {
    val all: List<Reward> = listOf(
        Reward(
            id = "car_wash",
            titleRes = R.string.reward_car_wash,
            descriptionRes = R.string.reward_car_wash_desc,
            cost = 500,
            iconName = "LocalCarWash"
        ),
        Reward(
            id = "fuel_5",
            titleRes = R.string.reward_fuel_discount,
            descriptionRes = R.string.reward_fuel_discount_desc,
            cost = 750,
            iconName = "LocalGasStation"
        ),
        Reward(
            id = "premium_week",
            titleRes = R.string.reward_premium_week,
            descriptionRes = R.string.reward_premium_week_desc,
            cost = 1500,
            iconName = "WorkspacePremium"
        ),
        Reward(
            id = "coffee",
            titleRes = R.string.reward_coffee,
            descriptionRes = R.string.reward_coffee_desc,
            cost = 200,
            iconName = "Coffee"
        )
    )
}
