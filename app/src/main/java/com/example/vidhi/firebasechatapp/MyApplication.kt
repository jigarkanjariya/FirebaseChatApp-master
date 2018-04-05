package com.example.vidhi.firebasechatapp

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.toast

/**
 * Created by Priya on 27-06-2017.
 */
class MyApplication : Application() {
    val TAG = "MyApplication"


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this@MyApplication)
    }

    override fun onCreate() {
        super.onCreate()


        registerActivityLifecycleCallbacks(FirebaseDatabaseConnectionHandler())

        FirebaseDatabase.getInstance().getReference().child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                Log.d(TAG, ".............onDataChange: $p0")
                if (p0?.value == false) {
                   // toast("No Internel Connection")
                } else {
                   // toast("Connected")
                }

            }
        })


    }


}