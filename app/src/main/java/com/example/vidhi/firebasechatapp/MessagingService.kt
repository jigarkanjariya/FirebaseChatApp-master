package com.example.vidhi.firebasechatapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.livinglifetechway.k4kotlin.orZero
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashSet


/**
 * Created by Priya on 04-07-2017.
 */

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        val TAG = "Message"
        val title = remoteMessage?.data?.get("title")
        val body = remoteMessage?.data?.get("body")
        val url = remoteMessage?.data?.get("icon")
        val mId : String? = remoteMessage?.data?.get("id")
        val thread = remoteMessage?.data?.get("threadId")


        Log.d(TAG, "onMessageReceived: $title")
        Log.d(TAG, "onMessageReceived: $body")

        ////....store userId in preference

        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val prefUserId: String = sharedPref.getString("userId", "")
        val userIdSet = HashSet<String?>()
        userIdSet.add(mId)
        val editor = sharedPref.edit()
        editor.putStringSet("userIdSet",userIdSet)
        editor.commit()



//       user id stores for Received message enable and disable............................
//        val notificationUid: MutableSet<String>? = null
//        val receivedMsgUsedId = sharedPref.edit()
//
//        receivedMsgUsedId.run {
//            putStringSet("userNotificationId", mId)
//            Log.d(TAG , "Notification Id........." + mId)
//            commit()
//        }



        if (mId != prefUserId) {

            fun getBitmapFromUrl(url: String?): Bitmap {
                val url = URL(url)
                val connection = url.openConnection() as HttpURLConnection
                connection.setDoInput(true)
                connection.connect()
                val input = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(input)
                return bitmap
            }

            val imgBitmap = getBitmapFromUrl(url)

            val mBuilder = NotificationCompat.Builder(this@MessagingService)
                    .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                .setAutoCancel (autoCancel : bo)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setLargeIcon(imgBitmap)
            val intent = Intent(this@MessagingService, ChatActivity::class.java)
            intent.putExtra("threadId", thread)
//        intent.putExtra("threadId", currentThread)
            intent.putExtra("uName", title)
            intent.putExtra("photoUrl", url)
            intent.putExtra("userId", mId)


            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addNextIntentWithParentStack(Intent(this, HomeActivity::class.java))
            stackBuilder.addNextIntent(intent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            mBuilder.setContentIntent(resultPendingIntent);
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(mId?.hashCode().orZero() as Int, mBuilder.build())
        }
    }
}