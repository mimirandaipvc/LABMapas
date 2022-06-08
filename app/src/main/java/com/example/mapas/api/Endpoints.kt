package com.example.mapas.api

import com.example.mapas.models.User
import retrofit2.http.*
import retrofit2.Call


interface Endpoints {

    @GET("/users/")
    fun getUsers():Call<List<User>>


}