package com.hmproductions.facewatch.data

import androidx.lifecycle.ViewModel
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.room.FaceWatchRepository
import okhttp3.MultipartBody

class FaceWatchViewModel : ViewModel() {

    private val repository: FaceWatchRepository = FaceWatchRepository()
    var token: String? = null

    fun identifyFace(client: FaceWatchClient, image: MultipartBody.Part): MutableList<Person> {
        val tempToken = token
        return if (tempToken != null)
            repository.identifyFace(client, tempToken, image)
        else
            mutableListOf()
    }

    fun login(client: FaceWatchClient, email: String, password: String): GenericResponse {
        val response = repository.login(client, AuthenticationDetails(email, password, "", ""))
        return response ?: GenericResponse(500, "Internal server error", "")
    }
}