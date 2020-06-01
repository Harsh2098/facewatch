package com.hmproductions.facewatch.data

data class IdentifyFacesResult(
    val statusCode: Int, val statusMessage: String, val numberOfFaces: Int,
    val students: List<FaceItem>, val probabilities: List<Double>
)

data class FaceItem(val name: String, val roll_no: String, val email: String)

data class Person(var name: String, var rollNumber: String, val probability: Double)

data class AuthenticationResponse(val statusCode: Int, val statusMessage: String, val token: String = "",
                                  val isAdmin: Boolean = false, val currentPhotosCount: Int = 0)

data class AuthenticationDetails(
    val email: String, val password: String, val roll_no: String, val name: String, val isAdmin: Boolean
)

data class GenericResponse(val statusCode: Int = 500, val statusMessage: String = "")

data class videoResponse(val statusCode: Int = 500, val statusMessage: String = "", val numberOfSavedImages: Int = 0)

data class Student(val roll_no: String, var date: String, val course_code: String)

data class AttendanceRequest(val studentList: MutableList<Student>)

data class AttendanceHistoryResult(val statusCode: Int, val statusMessage: String, val result: List<Student>)