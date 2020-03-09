package com.hmproductions.facewatch.data

import androidx.lifecycle.ViewModel
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.room.FaceWatchRepository
import okhttp3.MultipartBody

class FaceWatchViewModel : ViewModel() {

    private val repository: FaceWatchRepository = FaceWatchRepository()
    var token: String? = null

    fun identifyFace(client: FaceWatchClient, image: MultipartBody.Part): Person? {
        val tempToken = token
        return if (tempToken == null) {
            null
        } else {
            repository.identifyFace(client, tempToken, image)
        }
    }
}