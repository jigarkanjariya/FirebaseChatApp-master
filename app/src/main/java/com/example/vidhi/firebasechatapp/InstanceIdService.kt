package com.example.vidhi.firebasechatapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by Priya on 04-07-2017.
 */


class InstanceIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val auth = FirebaseAuth.getInstance()
        val tkn = FirebaseInstanceId.getInstance().getToken()
        Log.d("###Token Generated", "Token [" + tkn + "]")

        val userToken = FirebaseDatabase.getInstance().getReference("User-Token")
        userToken.run { child(auth.currentUser?.uid).child(tkn).setValue(true) }

    }
}
