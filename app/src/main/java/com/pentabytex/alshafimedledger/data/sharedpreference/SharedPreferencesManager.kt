package com.pentabytex.alshafimedledger.data.sharedpreference

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SharedPreferencesManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun saveString(key: String, value: String) =
        sharedPreferences.edit { putString(key, value) }

    fun getString(key: String, defaultValue: String = ""): String =
        sharedPreferences.getString(key, defaultValue).orEmpty()

    fun saveInt(key: String, value: Int) =
        sharedPreferences.edit { putInt(key, value) }

    fun getInt(key: String, defaultValue: Int = 0): Int =
        sharedPreferences.getInt(key, defaultValue)

    fun saveLong(key: String, value: Long) =
        sharedPreferences.edit { putLong(key, value) }

    fun getLong(key: String, defaultValue: Long = 0L): Long =
        sharedPreferences.getLong(key, defaultValue)

    fun saveFloat(key: String, value: Float) =
        sharedPreferences.edit { putFloat(key, value) }

    fun getFloat(key: String, defaultValue: Float = 0.0f): Float =
        sharedPreferences.getFloat(key, defaultValue)

    fun saveBoolean(key: String, value: Boolean) =
        sharedPreferences.edit { putBoolean(key, value) }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    fun remove(key: String) = sharedPreferences.edit { remove(key) }

    fun clear() = sharedPreferences.edit { clear() }
}
