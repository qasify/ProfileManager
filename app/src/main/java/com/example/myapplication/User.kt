package com.example.myapplication

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class User(var userId: String = "Not-Specified",
                var firstName: String = "Not-Specified",
                var lastName: String = "Not-Specified",
                var email: String = "Not-Specified",
                var password: String = "Not-Specified",
                var number: String = "Not-Specified",
                var dob: String = "Not-Specified",
                var dpUrl: String = "Not-Specified",
                var status: String = "offline") : Serializable
{ }