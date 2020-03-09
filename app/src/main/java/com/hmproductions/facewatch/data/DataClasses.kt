package com.hmproductions.facewatch.data

data class IdentifyFaceResult(
    val statusCode: Int, val statusMessage: String, val roll_no: String,
    val name: String, val probability: Double
)

data class Person(val name: String, val rollNumber: String)