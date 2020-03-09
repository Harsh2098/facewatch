package com.hmproductions.facewatch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hmproductions.facewatch.data.FaceWatchViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var model: FaceWatchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProvider(this).get(FaceWatchViewModel::class.java)
    }
}
