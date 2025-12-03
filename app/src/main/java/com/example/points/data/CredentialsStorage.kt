package com.example.points.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Maneja el almacenamiento seguro de credenciales y preferencias
 * relacionadas con la sesión del usuario.
 */
class CredentialsStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var rememberCredentials: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_CREDENTIALS, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_CREDENTIALS, value).apply()

    var keepSessionActive: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SESSION, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SESSION, value).apply()

    fun getSavedEmail(): String =
        prefs.getString(KEY_EMAIL, "") ?: ""

    fun getSavedPassword(): String =
        prefs.getString(KEY_PASSWORD, "") ?: ""

    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "points_credentials"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_REMEMBER_CREDENTIALS = "remember_credentials"
        private const val KEY_KEEP_SESSION = "keep_session_active"
    }
}

