package com.hmproductions.facewatch.room

import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.data.Person
import okhttp3.MultipartBody

class FaceWatchRepository {

    fun identifyFace(client: FaceWatchClient, token: String, image: MultipartBody.Part): Person {
        val result = client.identifyFace(token, image).execute()
        return Person(result.body()?.name ?: "", result.body()?.roll_no ?: "")
    }
}