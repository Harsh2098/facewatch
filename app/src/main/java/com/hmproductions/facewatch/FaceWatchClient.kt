package com.hmproductions.facewatch

import com.hmproductions.facewatch.data.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface FaceWatchClient {

    @POST("student/login")
    fun login(@Body authenticationDetails: AuthenticationDetails): Call<AuthenticationResponse>

    @POST("student/signup")
    fun signUp(@Body authenticationDetails: AuthenticationDetails): Call<AuthenticationResponse>

    @Multipart
    @POST("upload/image")
    fun uploadImage(
        @Header("Authorization") authorization: String, @Part image: MultipartBody.Part
    ): Call<GenericResponse>

    @Multipart
    @POST("upload/video")
    fun uploadVideo(
        @Header("Authorization") authorization: String, @Part image: MultipartBody.Part
    ): Call<videoResponse>

    @POST("train")
    fun trainModel(@Header("Authorization") authorization: String): Call<GenericResponse>

    @Multipart
    @POST("identify")
    fun identifyFace(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): Call<IdentifyFacesResult>

    @GET("attendence")
    fun getAttendance(@Header("Authorization") authorization: String): Call<AttendanceHistoryResult>

    @POST("attendence")
    fun saveAttendance(@Header("Authorization") authorization: String, @Body attendanceRequest: AttendanceRequest): Call<GenericResponse>
}