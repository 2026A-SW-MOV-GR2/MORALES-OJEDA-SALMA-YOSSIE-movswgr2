package com.example.clonbereal.data

import android.content.Context

/**
 * Maneja el registro, login y sesión del usuario usando SharedPreferences.
 * Fines académicos: NO usar en producción (contraseñas en texto plano).
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Registra un usuario. Devuelve false si el usuario ya existe. */
    fun register(username: String, password: String): Boolean {
        val key = userKey(username)
        if (prefs.contains(key)) return false
        prefs.edit().putString(key, password).apply()
        return true
    }

    /** Valida credenciales contra lo guardado. */
    fun validate(username: String, password: String): Boolean {
        val stored = prefs.getString(userKey(username), null) ?: return false
        return stored == password
    }

    /** Marca la sesión como iniciada y guarda el usuario activo. */
    fun login(username: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER, username)
            .apply()
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).remove(KEY_CURRENT_USER).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun currentUser(): String = prefs.getString(KEY_CURRENT_USER, "Salmynj") ?: "Salmynj"

    private fun userKey(username: String) = "user_${username.lowercase().trim()}"

    /** Guarda la descripción del perfil del usuario actual. */
    fun saveBio(bio: String) {
        prefs.edit().putString(bioKey(), bio).apply()
    }

    fun getBio(): String = prefs.getString(bioKey(), "") ?: ""

    /** Guarda la ruta de la foto de perfil del usuario actual. */
    fun savePhotoPath(path: String) {
        prefs.edit().putString(photoKey(), path).apply()
    }

    fun getPhotoPath(): String? = prefs.getString(photoKey(), null)

    private fun bioKey() = "bio_${currentUser().lowercase()}"
    private fun photoKey() = "photo_${currentUser().lowercase()}"

    companion object {
        private const val PREFS_NAME = "bereal_session"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_CURRENT_USER = "current_user"
    }
}
