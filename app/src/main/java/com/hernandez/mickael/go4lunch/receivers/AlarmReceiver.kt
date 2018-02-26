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

        var uid = FirebaseAuth.getInstance().uid!!
        var placeSelected = false
        var placeName = ""
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
        mDocRef.addSnapshotListener { snapshot, firestoreException ->
            // if a restaurant is selected
            if(snapshot.contains("restaurantName") && snapshot.getString("restaurantName") != ""){
                placeSelected = true
                placeName = snapshot.getString("restaurantName")
                // search in every user document for workmates going to the same place
                mColRef.addSnapshotListener { pSnapshot, pFirestoreException ->
                    if(!pSnapshot.isEmpty){
                        pSnapshot.forEach {
                            if(it.getString("restaurantName") == placeName && it.getString("uid") != uid){
                                matesCount++
                            }
                        }
                    }
                    // Notifies
                    mBuilder.setContentText(context.getString(R.string.notification_text, placeName, matesCount))
                    mNotificationManager.notify(0, mBuilder.build())
                }
            } else {
                // Notifies
                mBuilder.setContentText(context.getString(R.string.no_restaurant_selected))
                mNotificationManager.notify(0, mBuilder.build())
            }
        }
    }

}