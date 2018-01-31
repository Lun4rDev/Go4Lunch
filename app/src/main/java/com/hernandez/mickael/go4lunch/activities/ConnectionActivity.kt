package com.hernandez.mickael.go4lunch.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.hernandez.mickael.go4lunch.R
import java.util.*


/**
 * Created by Mickael Hernandez on 31/01/2018.
 */
class ConnectionActivity : FragmentActivity() {
    val RC_SIGN_IN = 123
    val RC_LOGOUT = 456
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAuthActivity()
    }

    private fun startAuthActivity(){
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                // Choose authentication providers
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build(),
                                AuthUI.IdpConfig.FacebookBuilder().build(),
                                AuthUI.IdpConfig.TwitterBuilder().build()
                        ))
                        .build(),
                RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            //val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Launches MainActivity
                startActivityForResult(Intent(applicationContext, MainActivity::class.java), RC_LOGOUT)
            } else {
                // Sign in failed, check response for error code
                Toast.makeText(applicationContext, getString(R.string.connectionfail), Toast.LENGTH_SHORT).show()
            }
        }
        if(requestCode == RC_LOGOUT){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        Toast.makeText(this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show()
                    }
            startAuthActivity()
        }
    }


}