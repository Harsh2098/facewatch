package com.hmproductions.facewatch.room

import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.data.AuthenticationDetails
import com.hmproductions.facewatch.data.AuthenticationResponse
import com.hmproductions.facewatch.data.GenericResponse
import com.hmproductions.facewatch.data.Person
import com.hmproductions.facewatch.utils.extractErrorMessage
import okhttp3.MultipartBody

class FaceWatchRepository {

    fun login(client: FaceWatchClient, authenticationDetails: AuthenticationDetails): AuthenticationResponse? {
        val result = client.login(authenticationDetails).execute()
        return if (result.isSuccessful) result.body()
        else AuthenticationResponse(500, extractErrorMessage(result.errorBody()?.string()))
    }

    fun identifyFace(client: FaceWatchClient, token: String, image: MultipartBody.Part): MutableList<Person> {
        val result = client.identifyFace(token, image).execute()
        val personList = mutableListOf<Person>()

        if (result.isSuccessful) {
            val faceList = result.body()?.students ?: return personList
            val probabilityList = result.body()?.probabilities ?: return personList
            for (index in faceList.indices) {
                val currentFace = faceList[index]
                personList.add(Person(currentFace.name, currentFace.roll_no, probabilityList[index]))
            }
        }

        return personList
    }

    fun uploadImage(client: FaceWatchClient, token: String, image: MultipartBody.Part): GenericResponse? {
        val result = client.uploadImage(token, image).execute()
        return result.body()
    }
}