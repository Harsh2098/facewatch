package com.hmproductions.facewatch.data

data class IdentifyFacesResult(
    val statusCode: Int, val statusMessage: String, val numberOfFaces: Int,
    val students: List<FaceItem>, val probabilities: List<Double>
)

data class FaceItem(val name: String, val roll_no: String, val email: String)

data class Person(val name: String, val rollNumber: String, val probability: Double)

data class GenericResponse(val statusCode: Int, val statusMessage: String, val token: String)

data class AuthenticationDetails(val email: String, val password: String, val roll_no: String, val name: String)
