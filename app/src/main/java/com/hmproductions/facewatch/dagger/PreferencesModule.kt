package com.hmproductions.facewatch.dagger

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides

@Module
class PreferencesModule {

    @Provides
    @FaceWatchApplicationScope
    fun getSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
}
