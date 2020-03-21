package com.hmproductions.facewatch.dagger

import com.hmproductions.facewatch.TrainModelWorker
import com.hmproductions.facewatch.fragment.AdminFragment
import com.hmproductions.facewatch.fragment.HomeFragment
import com.hmproductions.facewatch.fragment.LoginFragment
import dagger.Component

@FaceWatchApplicationScope
@Component(modules = [ContextModule::class, ClientModule::class, PreferencesModule::class])
interface FaceWatchApplicationComponent {

    fun inject(loginFragment: LoginFragment)
    fun inject(adminFragment: AdminFragment)
    fun inject(homeFragment: HomeFragment)
    fun inject(worker: TrainModelWorker)
}
