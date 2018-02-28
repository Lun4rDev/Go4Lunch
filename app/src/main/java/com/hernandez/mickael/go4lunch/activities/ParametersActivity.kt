package com.hernandez.mickael.go4lunch.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.receivers.AlarmReceiver
import kotlinx.android.synthetic.main.activity_parameters.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import android.widget.EditText
import android.widget.TextView

/**
 * Created by Mickael Hernandez on 22/02/2018.
 */
class ParametersActivity : AppCompatActivity() {

    private var KEY_SWITCH = "SWITCH"

    private lateinit var mSharedPrefs: SharedPreferences
    lateinit var mAlarm : AlarmManager

    private lateinit var notifIntent: PendingIntent

    private lateinit var editName : EditText

    // Fill UI with Firebase user data
    private var mUser = FirebaseAuth.getInstance().currentUser

    // Firestore user document
    private var mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters)
        setSupportActionBar(findViewById(R.id.toolbar_params))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mSharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

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

        editName = findViewById(R.id.edit_name)
        mDocRef.addSnapshotListener { snapshot, firestoreException ->
            if(snapshot.exists()){
                editName.setText(snapshot.getString("displayName"), TextView.BufferType.EDITABLE)
            }
        }
    }

    override fun onPause() {
        // updates username in database
        mDocRef.update("displayName", editName.text.toString())
        super.onPause()
    }

    private fun setRecurringAlarm(context: Context) {

        // Repeats the alarm in an hour everyday
        //alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager., AlarmManager.INTERVAL_DAY, recurringNotif)

        mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, AlarmManager.INTERVAL_DAY, notifIntent)

        NotificationCompat.Builder(context, "MyNews")
    }
}