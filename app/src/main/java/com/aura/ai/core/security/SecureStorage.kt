package com.aura.ai.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "t1000_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSecret(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun getSecret(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    fun deleteSecret(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    fun hasSecret(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }

    fun clearAllSecrets() {
        encryptedPrefs.edit().clear().apply()
    }
}
