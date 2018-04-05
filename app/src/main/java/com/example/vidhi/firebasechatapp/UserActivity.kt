package com.example.vidhi.firebasechatapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.MenuItem
import android.view.View
import android.widget.Button
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

class UserActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val dbRefUser = FirebaseDatabase.getInstance().getReference("User")

    lateinit var recyclerview: RecyclerView
    var recyclerviewSearch: RecyclerView? = null
    var list: MutableList<User>? = null
    var listSearchUser: MutableList<User>? = null
    var logout: Button? = null
//    var userId: String? = intent.getStringExtra("userId")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        //Extension function testing
//        toast("this is on create........")

        recyclerview = findViewById(R.id.rvFdList) as RecyclerView
        recyclerviewSearch = findViewById(R.id.rvSearch) as RecyclerView
        logout = findViewById(R.id.btnlogout) as Button
        val textView = findViewById(R.id.tv_user_name) as TextView
        val toolbar = findViewById(R.id.toolbar) as android.support.v7.widget.Toolbar


        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setHomeAsUpIndicator(R.mipmap.ic_action_navigation_arrow_back)
        ab.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        recyclerview!!.setHasFixedSize(true)
        recyclerview!!.layoutManager = LinearLayoutManager(this)
        recyclerviewSearch!!.layoutManager = LinearLayoutManager(this)

        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            toast("You are logging out..")

            val intent = Intent(this@UserActivity, MainActivity::class.java)
            startActivity(intent)
        }

        val sv = findViewById(R.id.sv) as android.support.v7.widget.SearchView
        sv.setOnClickListener {
            textView.visibility = View.GONE

        }

        list = mutableListOf()

        listSearchUser = mutableListOf()

        dbRefUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                for (i in p0?.children!!) {


                    val user = i.getValue(User::class.java)
                    val uName: String? = user?.name
                    val uimgUrl: String? = user?.photoUrl
                    val uEmail: String? = user?.email
                    val uKey: String? = i.key
                    val newUser = User(uName, uEmail, uimgUrl, uKey)

                    println(".................................." + uName)
                    list?.add(newUser!!)
                }
                recyclerviewSearch?.adapter = SearchAdapter(this@UserActivity, list!!)

            }
        })

        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                recyclerviewSearch?.visibility = View.VISIBLE
                recyclerview?.visibility = View.INVISIBLE

                listSearchUser?.clear()

                for (i in list!!) {
                    if (i.name!!.toLowerCase().contains(p0.toString().toLowerCase())) {
                        listSearchUser?.add(i)
                    }
                }
                recyclerviewSearch?.adapter = SearchAdapter(this@UserActivity, listSearchUser!!)

//                val dbonline = FirebaseDatabase.getInstance().getReference("User").child(userId)

                return true
            }

        })


        var mAdapter = object : FirebaseRecyclerAdapter<User, UserActivity.MyHolder>(User::class.java,
                R.layout.list_user_new, UserActivity.MyHolder::class.java, dbRefUser) {
            public override fun populateViewHolder(myholder: UserActivity.MyHolder, user: User, position: Int) {

                val url = user.photoUrl
                if (auth.currentUser!!.uid != getRef(myholder.adapterPosition).key) {
                    myholder.tvUser.setText(user.name)

                    val imageView = myholder.ivUser
                    Glide.with(applicationContext)
                            .load(url)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .into(imageView);
                    println(user.name)
                } else {
                    myholder.tvUser.visibility = View.GONE
                    myholder.ivUser.visibility = View.GONE
                }

                myholder.tvUser.setOnClickListener {
                    val dbRefThreadMember = FirebaseDatabase.getInstance().getReference("Thread-Member")
                    val idList = listOf<String?>(getRef(myholder.adapterPosition).key, auth.currentUser?.uid)
                    val newList1 = idList.sortedBy { it.toString() }
                    val threadId = newList1[0] + "-" + newList1[1]

                    dbRefThreadMember.run { child(threadId).child(auth.currentUser?.uid).setValue(true) }
                    dbRefThreadMember.run { child(threadId).child(getRef(myholder.adapterPosition).key).setValue(true) }

                    val dbRefUserThread = FirebaseDatabase.getInstance().getReference("User-Thread")
                    dbRefUserThread.run { child(auth.currentUser?.uid).child(threadId).setValue(true) }
                    dbRefUserThread.run { child(getRef(myholder.adapterPosition).key).child(threadId).setValue(true) }

                    val intent = Intent(this@UserActivity, ChatActivity::class.java)
                    intent.putExtra("threadId", threadId)
                    intent.putExtra("uName", user.name)
                    intent.putExtra("photoUrl", url)
                    intent.putExtra("userId", getRef(myholder.adapterPosition).key)

                    startActivity(intent)
                    finish()

                }
            }
        }
        recyclerview!!.adapter = mAdapter
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    class MyHolder(v: View) : RecyclerView.ViewHolder(v) {

        internal val tvUser: TextView
        internal val ivUser: RoundedImageView

        init {
            tvUser = v.findViewById(R.id.tv_user_name) as TextView
            ivUser = v.findViewById(R.id.iv_user) as RoundedImageView
        }
    }


}
