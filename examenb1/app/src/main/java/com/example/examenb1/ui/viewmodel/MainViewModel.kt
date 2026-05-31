package com.example.examenb1.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.examenb1.data.PersistenceManager
import com.example.examenb1.data.security.SecurityStorageManager
import com.example.examenb1.model.Publicacion
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val persistenceManager = PersistenceManager(application)
    private val securityStorageManager = SecurityStorageManager(application)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _remoteElements = MutableLiveData<List<Publicacion>?>()
    val remoteElements: LiveData<List<Publicacion>?> get() = _remoteElements

    private val _savedSecret = MutableLiveData<String?>()
    val savedSecret: LiveData<String?> get() = _savedSecret

    // SOLUCIÓN AL ERROR DE HILOS: Usar postValue para comunicar de fondo a la UI
    fun loadDataFromNetwork() {
        viewModelScope.launch {
            // Aseguramos que el estado de carga bloquee la UI de inmediato en el hilo principal
            _isLoading.value = true

            // La consulta de red corre aislada en fondo dentro de PersistenceManager (Dispatchers.IO)
            val result = persistenceManager.fetchRemoteData()

            // El postValue se encarga de empaquetar y enviar el resultado de forma segura
            _remoteElements.postValue(result)

            // Liberamos el bloqueo de la UI
            _isLoading.postValue(false)
        }
    }

    fun persistSecretKey(key: String, value: String) {
        securityStorageManager.saveSecretToken(key, value)
        _savedSecret.value = value
    }

    fun loadSecretKey(key: String) {
        _savedSecret.value = securityStorageManager.getSecretToken(key)
    }



}