package com.hernandez.mickael.go4lunch.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.facebook.FacebookSdk
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.hernandez.mickael.go4lunch.R
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.activity_connection.*
import okhttp3.*
import java.io.IOException
import java.math.BigInteger
import java.util.*
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.Callback

/**
 * Created by Mickael Hernandez on 31/01/2018.
 */
class ConnectionActivity : FragmentActivity() {
    val RC_LOGOUT = 456
    val RC_GOOGLE = 123
    val RC_TWITTER = 140
    val RC_FACEBOOK = 64206
    val REDIRECT_URL_CALLBACK = "https://go4lunch-91fa5.firebaseapp.com/__/auth/handler"

    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(this)

        // Initialize Twitter SDK
        Twitter.initialize(this)

        // Inflates the layout
        setContentView(R.layout.activity_connection)

        btn_twitter.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                //Log.d(FragmentActivity.TAG, "twitterLogin:success" + result)
                handleTwitterSession(result.data)
            }

            override fun failure(exception: TwitterException) {
                //Log.w(FragmentActivity.TAG, "twitterLogin:failure", exception)
                //updateUI(null)
            }
        }

        btn_github.setOnClickListener {
            signInGitHub()
        }


    }

    private fun signInGitHub() {
        val httpUrl = HttpUrl.Builder()
                .scheme("http")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", getString(R.string.github_id))
                .addQueryParameter("redirect_uri", REDIRECT_URL_CALLBACK)
                .addQueryParameter("state", BigInteger(130, Random()).toString(32))
                .addQueryParameter("scope", "user:email")
                .build()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()))
        startActivity(intent)

        var uri = getIntent().data
        if(uri != null && uri.toString().startsWith(REDIRECT_URL_CALLBACK)){
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if(code != null && state != null){
                sendPost(code, state)
            }
        }
    }

    private fun sendPost(code: String, state: String) {
        val okHttpClient = OkHttpClient()
        var form = FormBody.Builder()
                .add("client_id", getString(R.string.github_id))
                .add("client_secret", getString(R.string.github_secret))
                .add("code", code)
                .add("redirect_uri", REDIRECT_URL_CALLBACK)
                .add("state", state)
                .build()
        val request = Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(form)
                .build()

        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseBody = response!!.body().string()
                val splitted = responseBody.split("=|&")
                if(splitted[0] == "access_token") {
                    signInWithToken(splitted[1])
                } else {
                    Toast.makeText(applicationContext, responseBody, Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    fun signInWithToken(token: String){
        val cred = GithubAuthProvider.getCredential(token)
        mAuth.signInWithCredential(cred)
                .addOnCompleteListener({
                    if(!it.isSuccessful) {
                        it.exception!!.printStackTrace()
                    }
                })
    }

    private fun handleTwitterSession(session: TwitterSession) {
        //Log.d(FragmentActivity.TAG, "handleTwitterSession:" + session)

        val credential = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret)

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Log.d(FragmentActivity.TAG, "signInWithCredential:success")
                        //val user = mAuth.currentUser
                        //updateUI(user)
                        // Launches MainActivity
                        startActivityForResult(Intent(applicationContext, MainActivity::class.java), RC_LOGOUT)
                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(FragmentActivity.TAG, "signInWithCredential:failure", task.getException())
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                    // ...
                })
    }
        //startAuthActivity()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RC_LOGOUT -> {
                FirebaseAuth.getInstance().signOut()
            }
            RC_GOOGLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Launches MainActivity
                    startActivityForResult(Intent(applicationContext, MainActivity::class.java), RC_LOGOUT)
                } else {
                    // Sign in failed, check response for error code
                    Toast.makeText(applicationContext, getString(R.string.connectionfail), Toast.LENGTH_SHORT).show()
                }
            }
            RC_FACEBOOK -> {

            }
            RC_TWITTER -> {
                btn_twitter.onActivityResult(requestCode, resultCode, data)
            }
        }

    }


}