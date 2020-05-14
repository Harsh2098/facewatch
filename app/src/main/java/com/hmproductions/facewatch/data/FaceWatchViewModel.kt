package com.hmproductions.facewatch.data

import androidx.lifecycle.ViewModel
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.room.FaceWatchRepository
import okhttp3.MultipartBody

class FaceWatchViewModel : ViewModel() {

    private val repository: FaceWatchRepository = FaceWatchRepository()
    var token: String? = null
    var currentPhotosCount = 0
    var email: String? = null

    fun identifyFace(client: FaceWatchClient, image: MultipartBody.Part): MutableList<Person> {
        val tempToken = token
        return if (tempToken != null)
            repository.identifyFace(client, tempToken, image)
        else
            mutableListOf()
    }

    fun uploadImage(client: FaceWatchClient, image: MultipartBody.Part): GenericResponse {
        val tempToken = token
        return if (tempToken != null)
            repository.uploadImage(client, tempToken, image) ?: GenericResponse()
        else
            GenericResponse()
    }

    fun login(client: FaceWatchClient, email: String, password: String): AuthenticationResponse {
        val response = repository.login(client, AuthenticationDetails(email, password, "", "", false))
        return response ?: AuthenticationResponse(500, "Internal server error")
    }

    fun signUp(
        client: FaceWatchClient, email: String, name: String,
        roll_no: String, password: String, isAdmin: Boolean
    ): AuthenticationResponse {
        val response = repository.signUp(client, AuthenticationDetails(email, password, roll_no, name, isAdmin))
        return response ?: AuthenticationResponse(500, "Internal server error")
    }

    fun trainModel(client: FaceWatchClient): GenericResponse {
        val tempToken = token
        return if (tempToken != null)
            repository.trainModel(client, tempToken) ?: GenericResponse()
        else
            GenericResponse()
    }

    fun saveAttendance(client: FaceWatchClient, studentList: MutableList<Student>): GenericResponse {
        val tempToken = token
        return if (tempToken != null)
            repository.saveAttendance(client, tempToken, studentList) ?: GenericResponse()
        else
            GenericResponse()
    }

    fun getAttendance(client: FaceWatchClient): List<Student> {
        val tempToken = token
        return if (tempToken != null)
            repository.getAttendance(client, tempToken)
        else
            mutableListOf()
    }
}