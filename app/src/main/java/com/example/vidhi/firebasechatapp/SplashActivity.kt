package com.example.vidhi.firebasechatapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log


class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT = 3000
    private val TAG = "PermissionDemo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setupPermissions()



    }

    private fun setupPermissions() {

        val values = arrayOf("android.Manifest.permission.ACCESS_FINE_LOCATION","android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE",
                "android.Manifest.permission.READ_CONTACTS","android.Manifest.permission.RECORD_AUDIO")
        val permission = ContextCompat.checkSelfPermission(this,
                values.toString())


        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }



    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_CONTACTS,android.Manifest.permission.RECORD_AUDIO),
                101)

    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED
                        || grantResults[2] != PackageManager.PERMISSION_GRANTED || grantResults[3] != PackageManager.PERMISSION_GRANTED || grantResults[4] != PackageManager.PERMISSION_GRANTED) {
                    makeRequest()
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    Handler().postDelayed(Runnable /*
     * Showing splash screen with a timer. This will be useful when you
     * want to show case your app logo / company
     */

                    {
                        // This method will be executed once the timer is over
                        // Start your app main activity

                        val i = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(i)

                        // close this activity
                        finish()
                    }, 3000)

                }
            }
        }
    }


}
