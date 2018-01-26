package com.hernandez.mickael.go4lunch.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.hernandez.mickael.go4lunch.R
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.login.widget.LoginButton
import com.facebook.CallbackManager
import kotlinx.android.synthetic.main.activity_connection.*
import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import android.support.annotation.NonNull
import com.google.firebase.internal.FirebaseAppHelper.getToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.AuthCredential
import com.facebook.AccessToken
import com.facebook.internal.WebDialog
import com.google.android.gms.tasks.Task


/**
 * Created by Mickael Hernandez on 25/01/2018.
 */
class ConnectionActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private lateinit var mCallbackManager : CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        mAuth = FirebaseAuth.getInstance()
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create()
        login_button.setReadPermissions("email", "public_profile")
        login_button.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                //handleFacebookAccessToken(loginResult.accessToken)
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }

            override fun onCancel() {
                // ...
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(applicationContext, "Facebook connection error.", Toast.LENGTH_SHORT).show()
                // ...
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /*private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, object : WebDialog.OnCompleteListener<AuthResult>() {
                    fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth!!.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this@FacebookLoginActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }

                        // ...
                    }
                })
    }*/
}