package com.example.examenb1.repository

import android.content.Context
import android.util.Log
import com.example.examenb1.model.Publicacion
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class NoSqlRepository(context: Context) : PublicacionRepository {
    private val file = File(context.filesDir, "nosql_store.json")

    init {
        if (!file.exists()) {
            file.writeText("[]")
            Log.i("AUDITORIA_NOSQL", "Almacenamiento NoSQL JSON inicializado en memoria local.")
        }
    }

    private fun getJsonArray(): JSONArray {
        return JSONArray(file.readText())
    }

    private fun saveJsonArray(jsonArray: JSONArray) {
        file.writeText(jsonArray.toString())
    }

    override fun create(publicacion: Publicacion) {
        try {
            val array = getJsonArray()
            val obj = JSONObject().apply {
                put("id", publicacion.id)
                put("titulo", publicacion.titulo)
                put("contenido", publicacion.contenido)
                put("tipo_dinamico", "documento_nosql") // Esquema flexible extensible
            }
            array.put(obj)
            saveJsonArray(array)
            Log.d("DEBUG_NOSQL", "Documento JSON insertado dinámicamente: ID ${publicacion.id}")
        } catch (e: Exception) {
            Log.e("ERROR_NOSQL", "Error al escribir documento NoSQL: ${e.message}")
        }
    }

    override fun readAll(): List<Publicacion> {
        val lista = mutableListOf<Publicacion>()
        val array = getJsonArray()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            lista.add(Publicacion(
                obj.getString("id"),
                obj.getString("titulo"),
                obj.getString("contenido")
            ))
        }
        Log.i("INFO_NOSQL", "Lectura NoSQL finalizada. Documentos: ${lista.size}")
        return lista
    }

    override fun update(publicacion: Publicacion) {
        val array = getJsonArray()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            if (obj.getString("id") == publicacion.id) {
                obj.put("titulo", publicacion.titulo)
                obj.put("contenido", publicacion.contenido)
                break
            }
        }
        saveJsonArray(array)
        Log.d("DEBUG_NOSQL", "Documento actualizado en NoSQL: ${publicacion.id}")
    }

    override fun delete(id: String) {
        val array = getJsonArray()
        val newArray = JSONArray()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            if (obj.getString("id") != id) {
                newArray.put(obj)
            }
        }
        saveJsonArray(newArray)
        Log.d("DEBUG_NOSQL", "Documento eliminado de NoSQL: $id")
    }
}