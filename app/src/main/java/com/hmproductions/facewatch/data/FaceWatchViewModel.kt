package com.hmproductions.facewatch.data

import androidx.lifecycle.ViewModel
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.room.FaceWatchRepository
import okhttp3.MultipartBody

class FaceWatchViewModel : ViewModel() {

    private val repository: FaceWatchRepository = FaceWatchRepository()
    var token: String? = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImhhcnNobWFoYWphbjkyN0BnbWFpbC5jb20iLCJ1c2VySWQiOiI1ZTY1MzljZjQ2NGQ5NjFiYTNkMmNiOTkiLCJhZG1pbiI6dHJ1ZSwiaWF0IjoxNTgzODE5MzEzLCJleHAiOjE1ODM4MjI5MTN9.DRVK_E4P6UIXsBRwMr1aGe2wl3TrJrwz3skFO9Sk6eQ"

    fun identifyFace(client: FaceWatchClient, image: MultipartBody.Part): MutableList<Person> {
        val tempToken = token
        return if (tempToken != null)
            repository.identifyFace(client, tempToken, image)
        else
            mutableListOf()
    }
}