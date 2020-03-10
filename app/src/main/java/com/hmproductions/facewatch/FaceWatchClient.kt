package com.hmproductions.facewatch

import com.hmproductions.facewatch.data.AuthenticationDetails
import com.hmproductions.facewatch.data.GenericResponse
import com.hmproductions.facewatch.data.IdentifyFacesResult
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface FaceWatchClient {

    @POST("student/login")
    fun login(@Body authenticationDetails: AuthenticationDetails): Call<GenericResponse>

    @POST("student/signup")
    fun signUp(@Body authenticationDetails: AuthenticationDetails): Call<GenericResponse>

    @Multipart
    @POST("identify")
    fun identifyFace(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): Call<IdentifyFacesResult>
}