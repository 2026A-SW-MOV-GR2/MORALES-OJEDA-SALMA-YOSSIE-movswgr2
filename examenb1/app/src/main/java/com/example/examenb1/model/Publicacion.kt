package com.example.examenb1.model

data class Publicacion(
    val id: String, // Usaremos timestamps como strings para consistencia con tu CRUD anterior
    val titulo: String,
    val contenido: String
)
