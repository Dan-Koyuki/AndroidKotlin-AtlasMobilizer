package com.assignment.mondrodb.myModel

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitDocument {
    @GET("documents")
    fun getDocument(@Query("userId") userId: String): Call<List<String>>
}