package com.pentabytex.alshafimedledger.utils

object Constants {

    // 🔹 SharedPreferences Keys
    object SharedPrefKeys {
        const val ROLE = "role"
        const val PROFILE = "profile"
        const val TOKEN = "token"
        const val USER_ID = "user_id"
        const val IS_LOGGED_IN = "is_logged_in"
        const val ON_BOARDING = "on_boarding"
        const val IS_DARK_MODE = "is_dark_mode"
        const val APP_NAME = "HOPE_LINK"
        const val LANGUAGE = "language"
    }


    // 🔹 Intent Extras
    object IntentExtras {
        const val USER_ID = "extra_user_id"
        const val USER_NAME = "extra_user_name"
        const val USER_ROLE = "extra_user_role"
        const val USER_EMAIL = "extra_user_email"
        const val TRANSFER_DATA = "transfer_data"
        const val TRANSFER_DATA2 = "transfer_data2"
    }

    // 🔹 Firebase Collections
    object FirebaseCollections {
        const val USERS = "users"
        const val MEDICINES = "medicines"

    }

    // 🔹 Other Static Values
    const val EMAIL_ADDRESS = "jerryaxe24@gmail.com"
    const val STOCK_LIMIT = 10
}
