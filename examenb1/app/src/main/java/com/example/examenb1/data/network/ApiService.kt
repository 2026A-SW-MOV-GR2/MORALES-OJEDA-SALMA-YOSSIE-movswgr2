package com.example.examenb1.data.network

import com.example.examenb1.model.Publicacion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("posts")
    suspend fun getAllElementsRemote(): Response<List<Publicacion>>

    @POST("posts")
    suspend fun createElementRemote(@Body elemento: Publicacion): Response<Publicacion>
}