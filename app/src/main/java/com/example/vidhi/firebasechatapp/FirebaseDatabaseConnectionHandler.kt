package com.example.vidhi.firebasechatapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by Priya on 27-06-2017.
 */
class FirebaseDatabaseConnectionHandler : Application.ActivityLifecycleCallbacks {

    var count: Int = 0
    val delayedTimeMillis: Long = 10000 // change this if you want different timeout
    val mHandler: Handler = Handler()
    val dbRefUser = FirebaseDatabase.getInstance().getReference("User")
    val auth = FirebaseAuth.getInstance()

    override fun onActivityStarted(activity: Activity) {

        count++
        if (count > 0) {
            FirebaseDatabase.getInstance().goOnline()

            if (auth.currentUser?.uid != null) {
                dbRefUser.run { child(auth.currentUser?.uid).child("online").setValue("true") }
                FirebaseDatabase.getInstance().getReference("User").child(auth.currentUser?.uid).child("lastSeen").removeValue()

            }
        }


    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        count--
        Log.d(TAG, "onActivityStopped: count=" + count)
        if (count == 0) {

            Log.d(TAG, "onActivityStopped: going offline in 5 seconds..")
            mHandler.postDelayed({
                // just make sure that in the defined seconds no other activity is brought to front
                Log.d(TAG, "run: confirming if it is safe to go offline. Activity count: " + count)
                if (count == 0) {
                    Log.d(TAG, "run: going offline...")
                    FirebaseDatabase.getInstance().goOffline()
                } else {
                    Log.d(TAG, "run: Not going offline..")
                }
            }, delayedTimeMillis)
            FirebaseDatabase.getInstance().getReference("User").child(auth.currentUser?.uid).child("online").removeValue()
            val cTime: Long = System.currentTimeMillis()

            if (auth.currentUser?.uid != null) {
                dbRefUser.run { child(auth.currentUser?.uid).child("lastSeen") }.setValue(cTime)
            }


        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {

    }

    companion object {

        private val TAG = FirebaseDatabaseConnectionHandler::class.java.simpleName
    }
}