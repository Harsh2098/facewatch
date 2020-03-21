package com.hmproductions.facewatch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.utils.Constants.TOKEN_KEY
import javax.inject.Inject

class TrainModelWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    @Inject
    lateinit var client: FaceWatchClient

    override fun doWork(): Result {

        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(applicationContext)).build()
            .inject(this)

        val token = inputData.getString(TOKEN_KEY)
        if (token != null) {
            val response = client.trainModel(token).execute()

            if (!response.isSuccessful) {
                return Result.retry()
            }

            createCustomNotificationChannel()

            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.train_icon)
                .setContentTitle("Model Training")
                .setContentText(response.body()?.statusMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createContentIntent())
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(NOTIFICATION_ID, builder.build())
            }

            return Result.success()

        } else {
            return Result.failure()
        }
    }

    private fun createCustomNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.channel_name)

            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = applicationContext.getString(R.string.channel_description)

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(applicationContext, 0, intent, 0)
    }

    companion object {
        const val CHANNEL_ID = "facewatch-channel-id"
        const val NOTIFICATION_ID = 117
    }
}