package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable


class SignupActivity : AppCompatActivity() {
    var email: EditText? = null
    var password: EditText? = null
    var confirmPassword: EditText? = null

    var progressBar: ProgressBar? = null
    private var auth : FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_layout)

        email = findViewById<View>(R.id.signup_email) as EditText
        password = findViewById<View>(R.id.signup_password) as EditText
        confirmPassword = findViewById<View>(R.id.signup_confirm_password) as EditText

        auth = Firebase.auth

        val currentUser = auth!!.currentUser
    }

    fun onClickSignUpToLogin(view: View?) {
        finish()
    }

    fun onClickSignUpButton(view: View?) {

        var uEmail = email?.text.toString()
        var uPassword = password?.text.toString()
        var uConfirmPassword = confirmPassword?.text.toString()

        var existEmail: Boolean = false
        var nonFilled: Boolean = false

        var database = Firebase.database
        var myRef = database.reference.child("Users")

        if (uEmail.length == 0) {
            email!!.error = "Email is required!"
            nonFilled=true
        }
        if (uPassword.length == 0) {
            password!!.error = "Password is required!"
            nonFilled=true
        }else if (uPassword.length < 8) {
            password!!.error = "Password must be minimum 8 digits!"
            nonFilled=true
        }

        if (!uConfirmPassword.equals(uPassword)) {
            confirmPassword!!.error = "Password mismatched!"
            nonFilled=true
        }
        if(!nonFilled) {
            auth?.createUserWithEmailAndPassword(uEmail, uPassword)
                ?.addOnCompleteListener(this@SignupActivity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val u = auth!!.currentUser
                        if (u != null) {
                            var user =
                                User(userId = u.uid, email = uEmail, password = uPassword)
                            myRef.child(u.uid).setValue(user).addOnCompleteListener {
                                Toast.makeText(
                                    this@SignupActivity,
                                    "SetUp Profile",
                                    Toast.LENGTH_SHORT
                                ).show()
                                moveNext(user)
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext, "Email Already Used/ password issue!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
        }
    }

    private fun moveNext(u:User)
    {
        val intent = Intent(this@SignupActivity, ProceedActivity::class.java)
        intent.putExtra("user",u as Serializable)
        startActivity(intent)
        finish()
    }
}
