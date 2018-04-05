package com.example.vidhi.firebasechatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ResultCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

class MainActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tkn = FirebaseInstanceId.getInstance().getToken();
        Log.d("###Token Generated", "Token [" + tkn + "]");



        if (FirebaseAuth.getInstance().currentUser != null) {

            val tdbRef = FirebaseDatabase.getInstance().getReference("User-Token")
            tdbRef.run { child(auth.currentUser?.uid).child(tkn).setValue(true) }
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()

        } else {

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                            .build(),
                    RC_SIGN_IN)

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ResultCodes.OK) {

            val uEmail: String? = auth.currentUser!!.email
            val photoURL: String? = auth.currentUser!!.photoUrl?.toString()
            val uName: String? = auth.currentUser!!.displayName
            val uId: String? = auth.currentUser!!.uid
            val user = User(uName, uEmail, photoURL)
            val udbRef = FirebaseDatabase.getInstance().getReference("User")
            Log.d("$$$$$$$$$$$$$$$$", udbRef.toString())

            udbRef.run { child(uId).setValue(user) }
            finish()

        }

//        startService(Intent(FirebaseMessagingService::class.java!!.getName()))
    }


    companion object {

        private val RC_SIGN_IN = 123
    }


}

