package com.example.vidhi.firebasechatapp

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.makeramen.roundedimageview.RoundedImageView
import org.jetbrains.anko.toast

class HomeActivity : AppCompatActivity() {


    var name: String? = "null"
    var imgUrl: String? = "null"
    val auth = FirebaseAuth.getInstance()
    val dbRefThreadMember = FirebaseDatabase.getInstance().getReference("Thread-Member")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val recyclerview = findViewById(R.id.rvFdList) as RecyclerView
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val btn = findViewById(R.id.btnNewChat)
        val toolbar = findViewById(R.id.toolbar) as android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)
        btn.setOnClickListener {
            val intent = Intent(this@HomeActivity, UserActivity::class.java)
            startActivity(intent)
        }

        val dbRefUserThread = FirebaseDatabase.getInstance().getReference("User-Thread").child(auth.currentUser?.uid)

        var mAdapter = object : FirebaseRecyclerAdapter<Boolean, MyHolder>(Boolean::class.java,
                R.layout.list_user, MyHolder::class.java, dbRefUserThread) {
            override fun populateViewHolder(myholder: MyHolder, user: Boolean, position: Int) {

                val currentThread = getRef(myholder.adapterPosition).key
                val dbRefThreadUser = FirebaseDatabase.getInstance().getReference("Thread-Member").child(currentThread)
                dbRefThreadUser.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        println(p0)
                        for (childSnapshot in p0?.children!!) {
                            val usersId = childSnapshot?.key


                            if (!usersId!!.equals(auth.currentUser?.uid)) {
                                val dbRefSecUser = FirebaseDatabase.getInstance().getReference("User").child(usersId)
                                dbRefSecUser.addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot?) {
                                        val userName: User? = p0?.getValue(User::class.java)
                                        name = userName?.name
                                        println(name)
                                        if (userName?.online.equals("true")) {
                                            myholder.ivOnline.visibility = View.VISIBLE
                                        } else {
                                            myholder.ivOnline.visibility = View.GONE

                                        }
//...............................................received msg enable disable............................................................................................................
                                        var userIdSet: Set<String?>? = null
                                        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                        val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                                            //toast("Settings key changed: " + key)
                                            //here get user id from service class and cmpare here with old id ............

                                            userIdSet = sharedPref.getStringSet("userIdSet", userIdSet)
                                            if (userIdSet != null) {
                                                if (userIdSet!!.contains(usersId)) {
                                                    myholder.ivMsgAlert.visibility = View.VISIBLE

                                                }
                                            }

//                                            myholder.ivMsgAlert.visibility = View.VISIBLE

                                        }
                                        sharedPref.registerOnSharedPreferenceChangeListener(prefListener)


//////////////////////////////////////////////////////////////////////////////...End msg alert............///////////////////////////////////////////////////////////////
                                        myholder.tvUser.setText(name)
                                        imgUrl = userName!!.photoUrl
                                        val imageView = myholder.ivImg
                                        Glide.with(applicationContext)
                                                .load(imgUrl)
                                                .placeholder(R.mipmap.ic_launcher_round)
                                                .into(imageView);


                                        myholder.tvUser.setOnLongClickListener({
                                            val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@HomeActivity)
                                            subdialog.setTitle("Delete chat with " + name + " ?")
                                                    .setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                                                        val dlt = FirebaseDatabase.getInstance().getReference("User-Thread").child(auth.currentUser?.uid).child(currentThread).removeValue()
                                                        println(dlt)
                                                    })
                                                    .setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                                            val display = subdialog
                                            display.create()
                                            display.show()
                                            false
                                        })

                                        myholder.tvUser.setOnClickListener {
                                            // .....................remove visibility of msg alert button .........................................................
                                            myholder.ivMsgAlert.visibility = View.GONE

                                            val Intent = Intent(this@HomeActivity, ChatActivity::class.java)
                                            Intent.putExtra("threadId", currentThread)
                                            Intent.putExtra("uName", userName.name)
                                            Intent.putExtra("photoUrl", userName.photoUrl)
                                            Intent.putExtra("userId", usersId)

                                            startActivity(Intent)
                                        }

                                    }

                                })

                            }
                        }


                    }

                })

            }
        }

        recyclerview.adapter = mAdapter
    }

    class MyHolder(v: View) : RecyclerView.ViewHolder(v) {

        internal val tvUser: TextView
        internal val ivImg: RoundedImageView
        internal val ivOnline: ImageView
        internal val layoutUser: LinearLayout
        internal val ivMsgAlert: ImageView

        init {
            tvUser = v.findViewById(R.id.tvFd) as TextView
            ivImg = v.findViewById(R.id.iv_user) as RoundedImageView
            ivOnline = v.findViewById(R.id.ivOnline) as ImageView
            layoutUser = v.findViewById(R.id.layout_user) as LinearLayout
            ivMsgAlert = v.findViewById(R.id.iv_msg_alert) as ImageView
        }
    }


}

