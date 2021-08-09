package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable

open class LoginActivity : AppCompatActivity() {
    var email: EditText? = null
    var password: EditText? = null
    var user: User? = null
    var facebookButton: Button? = null
    var googleButton: Button? = null

    var type:String = "Email"

    private var auth : FirebaseAuth? = null
    private var callbackManager : CallbackManager? = null
    private var googleSignInClient : GoogleSignInClient? = null

    val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        email = findViewById<View>(R.id.login_email) as EditText
        password = findViewById<View>(R.id.login_password) as EditText
        facebookButton = findViewById<View>(R.id.facebook) as Button
        googleButton = findViewById<View>(R.id.google) as Button

        auth = Firebase.auth
        val currentUser = auth!!.currentUser
        if(currentUser != null){
            Toast.makeText(
                baseContext, "Logging In",
                Toast.LENGTH_SHORT
            ).show()
            updateUI(currentUser)
        }

        FacebookSdk.sdkInitialize(applicationContext)
        //Facebook Sign in
        callbackManager = CallbackManager.Factory.create()


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(
                    baseContext, "Google sign-in failed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        else
            callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    fun onClickFacebook(view: View?) {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email","public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    // App code
                    if (loginResult != null) {
                        handleFacebookAccessToken(loginResult.accessToken)
                    }
                }


                override fun onCancel() {
                    // App code
                    Toast.makeText(
                        baseContext, "Facebook sign-in canceled!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    Toast.makeText(
                        baseContext, "Facebook sign-in Error!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }


    fun onClickGoogle(view: View?) {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun onClickLoginToHome(view: View?) {

        var uEmail = email?.text.toString().trim()
        var uPassword = password?.text.toString()
        var nonFilled: Boolean = false

        if (uEmail.length == 0) {
            email!!.error = "Email is required!"
            nonFilled=true
        }
        if (uPassword.length == 0) {
            password!!.error = "Password is required!"
            nonFilled=true
        }
        if(!nonFilled) {

            var database = Firebase.database
            var myRef = database.reference.child("Users")


            auth?.signInWithEmailAndPassword(uEmail, uPassword)
                ?.addOnCompleteListener(this@LoginActivity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val u = auth!!.currentUser

                        Toast.makeText(
                            baseContext, "Email Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        myRef.addValueEventListener(object: ValueEventListener {

                            override fun onDataChange(snapshot: DataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                val user = u?.let { snapshot.child(it.uid).getValue(User::class.java) }
                                if (user != null) {
                                    moveNext(user)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(baseContext, "Error Fetching User from Db",
                                    Toast.LENGTH_LONG).show()
                            }

                            })
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Email Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }


    }

    fun onClickLoginToSignUp(view: View?) {
        val intent = Intent(this@LoginActivity, SignupActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    fun onClickLoginToReset(view: View?) {
        val intent = Intent(this@LoginActivity, ResetActivity::class.java)
        startActivity(intent)
    }

    private fun moveNext(u: User) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra("user", u as Serializable)
        startActivity(intent)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        baseContext, "Facebook Authentication Successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = auth?.currentUser
                    type="Facebook"
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Facebook Authentication failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        baseContext, "Google Authentication Successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = auth?.currentUser
                    auth?.currentUser
                    type="Google"
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Google Authentication Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUI(u: FirebaseUser?) {

        var ur = User()
        if(u != null) {

            var database = Firebase.database
            var myRef = database.reference.child("Users").child(u.uid).ref

                myRef.addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if(snapshot.exists()) {
                            ur = snapshot.getValue(User::class.java)!!

                            if (password?.text.toString() != ur.password && !password?.text?.isEmpty()!!) {
                                ur.password = password?.text.toString()
                                database.reference.child("Users").child(ur.userId).setValue(ur)
                            }

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("user", ur as Serializable)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            if (u.email.toString() != "null") {
                                ur.email = u.email.toString()
                            }
                            if (u.phoneNumber.toString() != "null") {
                                ur.number = u.phoneNumber.toString()
                            }
                            if (u.photoUrl.toString() != "null") {
                                ur.dpUrl = "${u.photoUrl.toString()}?type=large"
                            }
                            ur.userId = u.uid

                            var name = u?.displayName
                            var delimiter = " "
                            val parts = name?.split(delimiter)

                            ur.firstName = parts?.first().toString()
                            ur.lastName = parts?.last().toString()

                            myRef.setValue(ur).addOnCompleteListener {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("user", ur as Serializable)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(baseContext, "Error Fetching User from Db",
                            Toast.LENGTH_LONG).show()
                    }

                })

        }
    }

}

