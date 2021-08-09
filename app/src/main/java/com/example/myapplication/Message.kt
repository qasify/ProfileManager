package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.*

class Message (
    var sender: String = "Not Specified",
    var time: String = SimpleDateFormat("dd/M/yyyy hh:mm:ss aa").format(Date()),
    var text: String = "Default Message"
) {
}