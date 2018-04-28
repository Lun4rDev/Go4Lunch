package com.hernandez.mickael.go4lunch.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.receivers.AlarmReceiver
import kotlinx.android.synthetic.main.activity_parameters.*
import java.lang.Exception

/**
 * Created by Mickael Hernandez on 22/02/2018.
 */
class ParametersActivity : AppCompatActivity() {

    /** Shared Preferences key */
    private var KEY_SWITCH = "SWITCH"

    /** Shared Preferences instance */
    private lateinit var mSharedPrefs: SharedPreferences

    /** Notification alarm manager */
    lateinit var mAlarm : AlarmManager

    /** Notification intent */
    private lateinit var notifIntent: PendingIntent

    /** Name text field */
    private lateinit var editName : EditText

    /** Image url text field */
    private lateinit var editImgUrl : EditText

    /** Boolean telling if image url points to an actual image */
    private var isimageValid = false

    /** Current user signed in Firebase */
    var mUser = FirebaseAuth.getInstance().currentUser

    /** Firestore user document reference */
    private var mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating layout
        setContentView(R.layout.activity_parameters)

        // Sets the layout's custom toolbar as toolbar of this activity
        setSupportActionBar(findViewById(R.id.toolbar_params))

        // allow backward navigation
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // hide the title
        title = ""

        // Gets Shared Preferences instance
        mSharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        // Resume switch state
        switch_notifications.isChecked = mSharedPrefs.getBoolean(KEY_SWITCH, false)

        // Get instance of AlarmManager
        mAlarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Alarm intent
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        notifIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Notification switch listener
        switch_notifications.setOnClickListener {
            if(switch_notifications.isChecked){
                setRecurringAlarm(this)
            } else {
                mAlarm.cancel(notifIntent)
            }
            mSharedPrefs.edit().putBoolean(KEY_SWITCH, switch_notifications.isChecked).apply()
        }

        // Text inputs
        editName = findViewById(R.id.edit_name)
        editImgUrl = findViewById(R.id.edit_imgurl)

        // Database call
        mDocRef.addSnapshotListener { snapshot, _ ->
            if(snapshot != null && snapshot.exists()){
                // Puts database values into the text inputs
                editName.setText(snapshot.getString("displayName"), TextView.BufferType.EDITABLE)
                editImgUrl.setText(snapshot.getString("photoUrl"), TextView.BufferType.EDITABLE)
            }
        }

        // Image url EditText listener
        editImgUrl.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(txt: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Glide.with(applicationContext).load(txt.toString()).centerCrop().listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                        isimageValid = false
                        return true
                    }

                    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        isimageValid = true
                        return false
                    }

                }).into(findViewById(R.id.image_profile))
            }
        })
    }

    /** Inflates the menu */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_parameters, menu)
        return true
    }

    /** When the user selects an option in the toolbar */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle item selection
        return when (item?.itemId) {

            // If the user wants to apply the changes
            R.id.action_apply -> {
                val name = editName.text.toString()
                val imgUrl = editImgUrl.text.toString()
                if(name != "" && imgUrl != ""){
                    if(isimageValid){
                        // updates username in database
                        mDocRef.update("displayName", name)
                        mDocRef.update("photoUrl", imgUrl)
                        val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(editName.text.toString())
                                .setPhotoUri(Uri.parse(editImgUrl.text.toString()))
                                .build()
                        mUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            if(it.isSuccessful){
                                Toast.makeText(applicationContext, getString(R.string.changes_success),Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(applicationContext, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(applicationContext, "Invalid image URL", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, getString(R.string.params_missing), Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun setRecurringAlarm(context: Context) {

        // Repeats the alarm in an hour everyday
        //alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager., AlarmManager.INTERVAL_DAY, recurringNotif)

        mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, AlarmManager.INTERVAL_DAY, notifIntent)

        NotificationCompat.Builder(context, getString(R.string.app_name))
    }
}