package com.example.examenb1.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.examenb1.model.Publicacion

class SqlRepository(context: Context) : SQLiteOpenHelper(context, "EpnDatabase.db", null, 1), PublicacionRepository {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE publicaciones (id TEXT PRIMARY KEY, titulo TEXT, contenido TEXT)")
        Log.i("AUDITORIA_SQL", "Esquema fijo SQL inicializado correctamente.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    override fun create(publicacion: Publicacion) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id", publicacion.id)
            put("titulo", publicacion.titulo)
            put("contenido", publicacion.contenido)
        }
        val result = db.insert("publicaciones", null, values)
        if (result != -1L) {
            Log.d("DEBUG_SQL", "Inserción exitosa: ID ${publicacion.id}")
        } else {
            Log.e("ERROR_SQL", "Falló la inserción del ID ${publicacion.id}")
        }
    }

    override fun readAll(): List<Publicacion> {
        val lista = mutableListOf<Publicacion>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM publicaciones", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Publicacion(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.i("INFO_SQL", "Lectura completada. Registros encontrados: ${lista.size}")
        return lista
    }

    override fun update(publicacion: Publicacion) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("titulo", publicacion.titulo)
            put("contenido", publicacion.contenido)
        }
        db.update("publicaciones", values, "id = ?", arrayOf(publicacion.id))
        Log.d("DEBUG_SQL", "Registro actualizado en SQLite: ${publicacion.id}")
    }

    override fun delete(id: String) {
        val db = this.writableDatabase
        db.delete("publicaciones", "id = ?", arrayOf(id))
        Log.d("DEBUG_SQL", "Registro eliminado de SQLite: $id")
    }
}