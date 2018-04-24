package com.hernandez.mickael.go4lunch.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.activities.MainActivity

/**
 * Created by Mickael Hernandez on 22/02/2018.
 */
class AlarmReceiver: BroadcastReceiver() {

    /** Personal Firestore document reference */
    lateinit var mDocRef : DocumentReference

    /** Users Firestore collection reference */
    var mColRef = FirebaseFirestore.getInstance().collection("users")

    override fun onReceive(context: Context, p1: Intent) {
        // Intent opened by the click on notification
        val pI = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        var placeSelected = false
        var placeName: String
        var matesCount = 0

        mDocRef = mColRef.document(uid)


        // Notification text
        val notifString = if(placeSelected){

        } else {

        }

        // Building the notification
        val mBuilder = NotificationCompat.Builder(context, "MyNews")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(notifString.toString())
                .setContentIntent(pI)
                .setAutoCancel(true) // Removes the notification when clicked

        // Gets instance of NotificationManager service
        val mNotificationManager = NotificationManagerCompat.from(context) //context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        // User firestore document
        mDocRef.get().addOnCompleteListener {
            if(it.isSuccessful && it.result != null){
                // if a restaurant is selected
                if(it.result.contains("restaurantName") && it.result.getString("restaurantName") != ""){
                    placeSelected = true
                    placeName = it.result.getString("restaurantName").toString()
                    // search in every user document for workmates going to the same place
                    mColRef.addSnapshotListener { pSnapshot, _ ->
                        if(!pSnapshot?.isEmpty!!){
                            // Keeping count value, then recounting and comparing to know if it should notify
                            val matesCountTemp = matesCount
                            matesCount = 0
                            pSnapshot.forEach {
                                if(it.getString("restaurantName") == placeName && it.getString("uid") != uid){
                                    matesCount++
                                }
                            }
                            if(matesCount != matesCountTemp){
                                // Notifies
                                mBuilder.setContentText(context.getString(R.string.notification_text, placeName, matesCount))
                                mNotificationManager.notify(0, mBuilder.build())
                            }
                        }
                    }
                } else {
                    // Notifies
                    mBuilder.setContentText(context.getString(R.string.no_restaurant_selected))
                    mNotificationManager.notify(0, mBuilder.build())
                }
            }
        }
    }

}