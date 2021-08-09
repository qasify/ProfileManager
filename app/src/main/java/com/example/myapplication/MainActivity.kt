package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.ui.home.HomeFragment
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        var notClick: Boolean = true
    }

    var name: TextView? = null
    var email: TextView? = null
    var dp: ImageView? = null
    var user =User()

    private var googleSignInClient : GoogleSignInClient? = null

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val intent = intent
        user= intent.getSerializableExtra("user") as User

        var database = Firebase.database
        var myRef = database.reference.child("Users").child(user.userId).ref

        myRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                user = snapshot.getValue(User::class.java)!!

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "Error Fetching User from Db",
                    Toast.LENGTH_LONG).show()
            }

        })

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_logout
            ), drawerLayout

        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_profile->{
                    val bundle = Bundle()
                    bundle.putSerializable("User",user)
                    navController.navigate(R.id.home_to_profile,bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_home->{
                    navController.popBackStack()
                    val bundle = Bundle()
                    bundle.putString("UserId",user.userId)
                    navController.navigate(R.id.profile_to_home,bundle)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
        navController.popBackStack()
        val bundle = Bundle()
        bundle.putString("UserId",user.userId)
        navController.navigate(R.id.profile_to_home,bundle)
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }

    override fun onStart() {
        super.onStart()
        user.status="online"
        Firebase.database.reference.child("Users").child(user.userId).child("status").setValue(user.status)
    }

    override fun onStop() {
        super.onStop()
        if(user.status=="online" && notClick) {
            user.status = "away"
            Firebase.database.reference.child("Users").child(user.userId).child("status")
                .setValue(user.status)
        }
        notClick=true
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        user.status = "offline"
//        Firebase.database.reference.child("Users").child(user.userId).child("status").setValue("offline")
//    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        name = findViewById<View>(R.id.drawr_name) as TextView
        email = findViewById<View>(R.id.drawr_mail) as TextView
        dp= findViewById<View>(R.id.drawr_dp) as ImageView?

        name!!.text="${user.firstName} ${user.lastName}"
        email!!.text=user.email
        if(user.dpUrl!="Not-Specified"){
            Picasso.get().load(user.dpUrl).into(dp)
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    fun onclickLogout(item: MenuItem) {
        Firebase.database.reference.child("Users").child(user.userId).child("status").setValue("offline")
        LoginManager.getInstance().logOut()
        googleSignInClient?.signOut()
        Firebase.auth.signOut()

        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}