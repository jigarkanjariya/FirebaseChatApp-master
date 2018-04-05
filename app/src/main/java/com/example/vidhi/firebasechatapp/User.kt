package com.example.vidhi.firebasechatapp

/**
 * Created by Priya on 07-06-2017.
 */
data class User(var name: String? = "",
                var email: String? = "",
                val photoUrl: String? = "",
                var userKey: String? = "",
                var online: String? = "",
                var lastSeen: Long? = -1)