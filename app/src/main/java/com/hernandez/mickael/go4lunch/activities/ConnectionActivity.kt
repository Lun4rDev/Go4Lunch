package com.hernandez.mickael.go4lunch.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.dialogs.EmailDialogFragment
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.Callback
import kotlinx.android.synthetic.main.activity_connection.*
import okhttp3.*
import java.io.IOException
import java.math.BigInteger
import java.util.*

/**
 * Created by Mickael Hernandez on 31/01/2018.
 */
class ConnectionActivity : FragmentActivity(), EmailDialogFragment.NoticeDialogListener {

    /** Debug tag */
    val TAG = "DEBUGTAG"

    /** Logout request code */
    val RC_LOGOUT = 456

    /** Google request code */
    val RC_GOOGLE = 123

    /** Facebook request code */
    val RC_FACEBOOK = 64206

    /** Firebase auth instance */
    private var mAuth = FirebaseAuth.getInstance()

    /** Facebook callback manager */
    private lateinit var mFbCallbackManager: CallbackManager

    /** Google sign-in client */
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent user with no Internet connection from entering the app
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(cm.activeNetworkInfo == null || !cm.activeNetworkInfo.isConnectedOrConnecting){
            Toast.makeText(applicationContext, "You need an Internet connection to use this application.", Toast.LENGTH_LONG).show()
            finish()
        }

        // If user is already signed in
        mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser != null){
            startMainActivity()
            return
        }

        // Recovering Facebook account
        val fbToken = AccessToken.getCurrentAccessToken()
        if(fbToken != null){
            signInFacebook(fbToken)
        }

        // Recovering Google account
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account != null){
            signInGoogle(account.idToken.toString())
        }

        // Catches GitHub auth intent
        val uri = intent.data
        if(uri != null && uri.toString().startsWith(getString(R.string.github_app_url))){
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if(code != null && state != null){
                sendPost(code, state)
            }
        }

        // Inflates the layout
        setContentView(R.layout.activity_connection)

        // Initialize Twitter SDK
        Twitter.initialize(applicationContext)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(applicationContext, gso)

        // Google sign-in button
        btn_google.setOnClickListener {
            startActivityForResult(mGoogleSignInClient.signInIntent, RC_GOOGLE)
        }

        // Facebook sign-in button
        mFbCallbackManager = CallbackManager.Factory.create()
        val loginButton = findViewById<LoginButton>(R.id.btn_facebook)
        loginButton.setReadPermissions("email", "public_profile")
        loginButton.registerCallback(mFbCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                signInFacebook(loginResult.accessToken)
            }
            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }
            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })

        // Twitter sign-in button
        btn_twitter.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                Log.d(TAG, "twitterLogin:success$result")
                signInTwitter(result.data)
            }
            override fun failure(exception: TwitterException) {
                Log.w(TAG, "twitterLogin:failure", exception)
            }

        }

        //GitHub sign-in button
        btn_github.setOnClickListener {
            val httpUrl = HttpUrl.Builder()
                    .scheme("http")
                    .host("github.com")
                    .addPathSegment("login")
                    .addPathSegment("oauth")
                    .addPathSegment("authorize")
                    .addQueryParameter("client_id", getString(R.string.github_id))
                    .addQueryParameter("redirect_uri", getString(R.string.github_app_url))
                    .addQueryParameter("state", BigInteger(130, Random()).toString(32))
                    .addQueryParameter("scope", "user:email")
                    .build()

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()))
            startActivity(intent)
        }

        // E-mail sign-in button
        btn_email.setOnClickListener {
            val newFragment = EmailDialogFragment()
            newFragment.show(supportFragmentManager, "missiles")

        }
    }

    /** Positive response from email sign-in dialog */
    override fun onDialogPositiveClick(dialog: DialogFragment, email:String, password:String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful){
                // If authentication succeeds
                startMainActivity()
            } else {
                // If auth fails, try to create the account
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful){
                        startMainActivity()
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.email_signin_fail), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /** Negative response from mail sign-in dialog */
    override fun onDialogNegativeClick(dialog: DialogFragment) {}

    /** Send POST request to GitHub with OkHttp3 */
    private fun sendPost(code: String, state: String) {
        val okHttpClient = OkHttpClient()
        val form = FormBody.Builder()
                .add("client_id", getString(R.string.github_id))
                .add("client_secret", getString(R.string.github_secret))
                .add("code", code)
                .add("redirect_uri", getString(R.string.github_app_url))
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
                val responseBody = response?.body()?.string()
                val split = responseBody!!.split("[=&]".toRegex())
                if(split[0] == "access_token") {
                    signInGitHub(split[1])
                } else {
                    Looper.prepare()
                    Toast.makeText(applicationContext, responseBody, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    /** Google credentials */
    private fun signInGoogle(token: String){
        val cred = GoogleAuthProvider.getCredential(token, null)
        signInWithCredential(cred)
    }

    /** Facebook credentials */
    private fun signInFacebook(token: AccessToken) {
        val cred = FacebookAuthProvider.getCredential(token.token)
        signInWithCredential(cred)
    }

    /** GitHub credentials */
    fun signInGitHub(token: String){
        val cred = GithubAuthProvider.getCredential(token)
        signInWithCredential(cred)
    }

    /** Twitter credentials */
    private fun signInTwitter(session: TwitterSession) {
        val cred = TwitterAuthProvider.getCredential(session.authToken.token, session.authToken.secret)
        signInWithCredential(cred)
    }

    /** FirebaseAuth sign-in with credentials from third parties */
    private fun signInWithCredential(credential: AuthCredential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                     if (task.isSuccessful) {
                        // Sign in success, start MainActivity
                        Log.d(TAG, "signInWithCredential:success")
                        //val user = mAuth.currentUser
                        startMainActivity()
                    } else {
                        if(task.exception is FirebaseAuthUserCollisionException) {
                            mAuth.currentUser?.linkWithCredential(credential)
                                    ?.addOnCompleteListener{
                                        if (it.isSuccessful) {
                                            Log.d(TAG, "signInWithCredential:success")
                                            startMainActivity()
                                        } else {
                                            mAuth.currentUser?.unlink(mAuth.currentUser!!.providerId)
                                            Log.d(TAG, "linkWithCredential:failure", task.exception)
                                            Toast.makeText(applicationContext, "Account unlinked because of conflicts. Please retry.", Toast.LENGTH_LONG).show()
                                        }

                                    }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.exception)
                            Toast.makeText(applicationContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                })
    }

    /** Start MainActivity for result */
    private fun startMainActivity(){
        if(FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivityForResult(intent, RC_LOGOUT)
        } else {
            // If user is signed in but not in Firebase Auth, recreate to recover the token correctly
            this.recreate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RC_LOGOUT -> {
                if(::mGoogleSignInClient.isInitialized){
                    mGoogleSignInClient.signOut()
                }
                // Sign Firebase user out
                FirebaseAuth.getInstance().signOut()
                // Sign Facebook user out
                LoginManager.getInstance().logOut()
                this.recreate()
            }
            RC_GOOGLE -> {
                if (resultCode == RESULT_OK) {
                        startMainActivity()
                } else {
                    // Sign in failed, check response for error code
                    Toast.makeText(applicationContext, getString(R.string.connectionfail), Toast.LENGTH_SHORT).show()
                }
            }
            Twitter.getInstance().twitterAuthConfig.requestCode -> {
                btn_twitter.onActivityResult(requestCode, resultCode, data)
            }
        }
        if(FacebookSdk.isFacebookRequestCode(requestCode) && resultCode == RESULT_OK){
            if(::mFbCallbackManager.isInitialized){
                mFbCallbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }

    }


}