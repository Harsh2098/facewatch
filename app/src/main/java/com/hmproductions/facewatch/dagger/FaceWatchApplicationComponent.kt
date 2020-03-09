package com.hmproductions.facewatch.dagger

import com.hmproductions.facewatch.fragment.HomeFragment
import dagger.Component

@FaceWatchApplicationScope
@Component(modules = [ContextModule::class, ClientModule::class, PreferencesModule::class])
interface FaceWatchApplicationComponent {

    fun inject(homeFragment: HomeFragment)
}
