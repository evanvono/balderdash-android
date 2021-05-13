package com.evanvonoehsen.balderdash.network

import com.evanvonoehsen.balderdash.data.WordResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WordAPI {
    @GET("v4/words.json/wordOfTheDay")
    fun getWord(@Query("date") date: String, @Query("api_key") key: String) : Call<WordResult>
}