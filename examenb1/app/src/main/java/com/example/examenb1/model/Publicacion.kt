package com.example.examenb1.model

// SOLUCIÓN AL MAPEO: Importar la directiva para emparejar variables de internet
import com.google.gson.annotations.SerializedName

data class Publicacion(
    val id: String, // Se mantiene idéntico para consistencia con tus llaves SQLite/NoSQL

    @SerializedName("title") // <- Enlaza "title" de JSONPlaceholder con tu variable "titulo"
    val titulo: String,

    @SerializedName("body")  // <- Enlaza "body" de JSONPlaceholder con tu variable "contenido"
    val contenido: String
)