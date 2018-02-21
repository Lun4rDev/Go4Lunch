package com.hernandez.mickael.go4lunch.activities

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Restaurant
import kotlinx.android.synthetic.main.activity_restaurant.*
import android.graphics.BitmapFactory
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hernandez.mickael.go4lunch.adapters.JoiningListAdapter
import com.hernandez.mickael.go4lunch.model.Workmate


/**
 * Created by Mickael Hernandez on 06/02/2018.
 */
class RestaurantActivity : AppCompatActivity() {

    val RC_SELECT = 789

    /** If this restaurant is selected by the current user */
    var mSelected = false

    /** Users Firestore collection reference */
    var mColRef = FirebaseFirestore.getInstance().collection("users")

    lateinit var mRestaurant: Restaurant

    lateinit var mSharedPrefs: SharedPreferences

    private var mUser = FirebaseAuth.getInstance().currentUser

    private var mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)

    private lateinit var mAdapter: JoiningListAdapter

    private var mList = ArrayList<Workmate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        // Shared Preferences
        mSharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        // Get Restaurant object from intent
        mRestaurant = intent.getParcelableExtra("Restaurant")
        mRestaurant.img = BitmapFactory.decodeByteArray(intent.getByteArrayExtra("Image"), 0, intent.getByteArrayExtra("Image").size)

        // Setting UI according to Restaurant object
        restaurant_img.setImageBitmap(mRestaurant.img)
        //findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar).title = mRestaurant.name
        restaurant_name.text = mRestaurant.name
        restaurant_desc.text = mRestaurant.address

        mAdapter = JoiningListAdapter(applicationContext, R.layout.row_workmate, mList)
        list_workmates.adapter = mAdapter

        // Text view for the empty list
        list_workmates.emptyView = text_empty

        // Getting workmates joining this restaurant from Firestore
        mColRef.addSnapshotListener { colSnapshot, p1 ->
            if(colSnapshot != null && colSnapshot.documents.isNotEmpty()){
                val res = ArrayList<Workmate>()
                for(doc in colSnapshot.documents){
                    // If this restaurant and the document restaurant corresponds
                    if(doc.get("restaurantId") == mRestaurant.id){
                        // If it's the current user document
                        if(doc.get("uid") == mUser!!.uid){
                            updateUI(true)
                        } else {
                            res.add(doc.toObject(Workmate::class.java))
                        }
                    }
                }
                mList.clear()
                mList.addAll(res)
                mAdapter.notifyDataSetChanged()
            }
        }

        // Call button listener
        button_call.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:" + mRestaurant.phone)
            if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                startActivity(callIntent)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 0)
            }
        }

        // Disable website button if there's none
        if(mRestaurant.website == null || mRestaurant.website == ""){
            button_website.isEnabled = false
        }

        // Website button listener
        button_website.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mRestaurant.website))
            startActivity(intent)
        }

        // Float action button listener
        fab_select.setOnClickListener {
            val hMap = HashMap<String, Any>()
            hMap["uid"] = mUser!!.uid
            if(mSelected){
                hMap["restaurantId"] = ""
                hMap["restaurantName"] = ""
                mDocRef.set(hMap).addOnCompleteListener {
                    if(it.isSuccessful){
                            Toast.makeText(applicationContext, getString(R.string.unselect_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                hMap["restaurantId"] = mRestaurant.id
                hMap["restaurantName"] = mRestaurant.name
                mDocRef.set(hMap).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(applicationContext, getString(R.string.select_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            /*
            mSharedPrefs.edit().putBoolean(getString(R.string.RESTAURANT_CHANGE), true).apply()
            mSharedPrefs.edit().putStringSet(getString(R.string.RESTAURANT_VALUES), setOf(mRestaurant.id.toString(), mRestaurant.name.toString()) as MutableSet<String>?).apply()
            */
            finish()
        }
    }

    private fun updateUI(selected: Boolean){
        if(selected){
            mSelected = true
            // Configure fab for cancel button
            fab_select.backgroundTintList = ColorStateList.valueOf(Color.RED)
            fab_select.setImageResource(R.drawable.ic_cancel_white)
        } else {
            mSelected = false
        }
    }
}