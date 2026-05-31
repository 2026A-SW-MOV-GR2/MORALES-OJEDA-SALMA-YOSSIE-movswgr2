package com.example.examenb1.repository

import com.example.examenb1.model.Publicacion

interface PublicacionRepository {
    fun create(publicacion: Publicacion)
    fun readAll(): List<Publicacion>
    fun update(publicacion: Publicacion)
    fun delete(id: String)
}