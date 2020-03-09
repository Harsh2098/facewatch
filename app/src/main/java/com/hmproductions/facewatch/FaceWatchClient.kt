package com.hmproductions.facewatch

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

interface FaceWatchClient {
    @Headers("AccountKey:EPEcmrGzRWeN4824xfuvoQ==")
    @GET("CarParkAvailabilityv2")
    fun getCarParkAvailabilityDetails(): Call<LiveCarParkResult>
}