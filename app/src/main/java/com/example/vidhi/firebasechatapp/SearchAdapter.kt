package com.example.vidhi.firebasechatapp

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.makeramen.roundedimageview.RoundedImageView


/**
 * Created by vidhi on 6/14/2017.
 */
class SearchAdapter(context: Context, list: MutableList<User>) : RecyclerView.Adapter<SearchAdapter.viewHolder>() {

    var myList: MutableList<User> = list
    val auth = FirebaseAuth.getInstance()
    val context: Context = context


    class viewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        override fun onClick(v: View?) {
            var p: Int = adapterPosition
        }

        internal var name: TextView
        internal var userImf: ImageView

        init {

            name = v.findViewById(R.id.textView) as TextView
            userImf = v.findViewById(R.id.iv_user) as RoundedImageView

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val itemView = LayoutInflater.from(
                parent.context).inflate(R.layout.list_search, parent, false)
        return viewHolder(itemView)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {

        if (auth.currentUser!!.uid != myList[position].userKey) {
            holder.name.setText(myList[position].name)
            Glide.with(holder.userImf.getContext())
                    .load(myList[position].photoUrl)
                    .into(holder.userImf)
        } else {
            holder.name.visibility = View.GONE
            holder.userImf.visibility = View.GONE

        }

        holder.name.setText(myList[position].name)
        Glide.with(holder.userImf.getContext())
                .load(myList[position].photoUrl)
                .into(holder.userImf)

        holder.name.setOnClickListener {

            val dbRefThreadMember = FirebaseDatabase.getInstance().getReference("Thread-Member")
            val idList = listOf<String?>(myList[position].userKey, auth.currentUser?.uid)
            val newList1 = idList.sortedBy { it.toString() }
            val threadId = newList1[0] + "-" + newList1[1]


            dbRefThreadMember.run { child(threadId).child(auth.currentUser?.uid).setValue(true) }
            dbRefThreadMember.run { child(threadId).child(myList[position].userKey).setValue(true) }

            val dbRefUserThread = FirebaseDatabase.getInstance().getReference("User-Thread")
            dbRefUserThread.run { child(auth.currentUser?.uid).child(threadId).setValue(true) }
            dbRefUserThread.run { child(myList[position].userKey).child(threadId).setValue(true) }

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("threadId", threadId)
            intent.putExtra("uName", holder.name.text)
            intent.putExtra("photoUrl", myList[position].photoUrl)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context?.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun getItemId(position: Int): Long {
        return myList.get(position).hashCode().toLong()
    }
}

