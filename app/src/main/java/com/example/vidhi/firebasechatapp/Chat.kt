package com.example.vidhi.firebasechatapp

/**
 * Created by Priya on 06-06-2017.
 */
data class Chat(var msg: String? = "",
                var time: Long = -1,
                var userId: String? = "",
                var contact: Contact? = null,
                var filePath: String? = "",
                var imgPath: String? = "",
                var audioPath: String? = "",
                val location: Location? = null)

data class Contact(var name: String? = "",
                   var number: String? = "")

data class Location(var lat: Double? = null,
                    var long: Double? = null)
