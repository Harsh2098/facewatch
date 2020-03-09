package com.hmproductions.facewatch.dagger

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val context: Context) {

    @Provides
    @FaceWatchApplicationScope
    fun context() = context
}