package com.mbanking.m_banking.other

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mbanking.m_banking.R
import com.mbanking.m_banking.view.activity.ChattingUserActivity
import com.mbanking.m_banking.view.activity.ChattingUserActivity.Companion.EXTRA_ID
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "notification_channel"
        var sharedPreferences: SharedPreferences? = null

        var tokens: String?
            get() {
                return sharedPreferences?.getString("token", "")
            }
            set(value) {
                sharedPreferences?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokens = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val receiverId = message.from?.replace("/topics/", "")

        val userRef =
            Firebase.database.reference.child("Users").child(receiverId.toString())
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val isLogin = snapshot.child("login").value

                    if (isLogin == true) {
                        notificationEntered(message)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun notificationEntered(message: RemoteMessage) {
        val intent = Intent(this, ChattingUserActivity::class.java)
        intent.putExtra(EXTRA_ID, message.data["id"])

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "ChannelLiveChat"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "Firebase Chat Description"
            enableLights(true)
            lightColor = Color.WHITE
        }
        notificationManager.createNotificationChannel(channel)
    }
}