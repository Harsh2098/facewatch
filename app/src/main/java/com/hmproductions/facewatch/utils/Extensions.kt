package com.hmproductions.facewatch.utils

fun Int.isSuccessful(): Boolean {
    return this in 200..299
}