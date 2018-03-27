package com.hernandez.mickael.go4lunch.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.receivers.AlarmReceiver
import kotlinx.android.synthetic.main.activity_parameters.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Created by Mickael Hernandez on 22/02/2018.
 */
class ParametersActivity : AppCompatActivity() {

    private var KEY_SWITCH = "SWITCH"

    private lateinit var mSharedPrefs: SharedPreferences
    lateinit var mAlarm : AlarmManager

    private lateinit var notifIntent: PendingIntent

    private lateinit var editName : EditText

    private lateinit var editImgUrl : EditText

    // Fill UI with Firebase user data
    var mUser = FirebaseAuth.getInstance().currentUser

    // Firestore user document
    private var mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters)
        setSupportActionBar(findViewById(R.id.toolbar_params))

        // allow backward navigation
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // hide the title
        title = ""

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
        editImgUrl.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(txt: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Glide.with(applicationContext).load(txt.toString()).centerCrop().into(findViewById(R.id.image_profile))
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_parameters, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle item selection
        return when (item?.itemId) {

            // If the user wants to apply the changes
            R.id.action_apply -> {
                val name = editName.text.toString()
                val imgUrl = editImgUrl.text.toString()
                if(name != "" && imgUrl != ""){
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

        NotificationCompat.Builder(context, "MyNews")
    }
}