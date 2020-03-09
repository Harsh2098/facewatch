package com.hmproductions.facewatch

import com.hmproductions.facewatch.data.IdentifyFaceResult
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FaceWatchClient {

    @Multipart
    @POST("identify")
    fun identifyFace(
        @Header("Authorization") authorization: String,
        @Part("photo") image: MultipartBody.Part
    ): Call<IdentifyFaceResult>
}