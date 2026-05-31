package com.example.examenb1.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys // <- CAMBIADO: Lleva una 's' al final en esta versión

class SecurityStorageManager(context: Context) {

    // SOLUCIÓN AL UNRESOLVED: Usar el generador clásico compatible con la versión estable
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Inicializar las SharedPreferences Encriptadas a nivel de hardware nativo
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_tokens_store",
        masterKeyAlias, // Pasamos el alias de la llave maestra
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Almacenar un secreto de forma segura
    fun saveSecretToken(key: String, token: String) {
        sharedPreferences.edit().putString(key, token).apply()
    }

    // Recuperar el secreto descifrado
    fun getSecretToken(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    // Limpiar credenciales
    fun clearSecret(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}