package com.example.examenb1.data

import android.content.Context
import com.example.examenb1.data.network.RetrofitClient
import com.example.examenb1.model.Publicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersistenceManager(context: Context) {

    private val apiService = RetrofitClient.instance

    // SOLUCIÓN AL ROJO: Cambiado de List<String> a List<Publicacion> para acoplarse al ApiService
    suspend fun fetchRemoteData(): List<Publicacion>? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllElementsRemote()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // SOLUCIÓN AL ROJO: Cambiado de String a Publicacion para cumplir el envío asíncrono
    suspend fun saveElementRemote(elemento: Publicacion): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createElementRemote(elemento)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}