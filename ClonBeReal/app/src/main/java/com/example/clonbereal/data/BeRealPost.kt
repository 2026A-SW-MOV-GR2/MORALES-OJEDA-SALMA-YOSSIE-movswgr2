package com.example.clonbereal.data

data class BeRealPost(
    val id: String,
    val userAvatarResId: Int,
    val username: String,
    val timePosted: String,
    val mainImagePath: String,    // ruta del archivo en vez de Bitmap
    val selfieImagePath: String,  // ruta del archivo en vez de Bitmap
    val reactionCount: Int,
    val commentCount: Int
)