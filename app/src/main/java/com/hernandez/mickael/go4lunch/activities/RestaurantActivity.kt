package com.hernandez.mickael.go4lunch.activities

import android.Manifest
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.BitmapDataObject
import com.hernandez.mickael.go4lunch.model.Restaurant
import kotlinx.android.synthetic.main.activity_restaurant.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


/**
 * Created by Mickael Hernandez on 06/02/2018.
 */
class RestaurantActivity : AppCompatActivity() {

    lateinit var mRestaurant: Restaurant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)
        mRestaurant = intent.getParcelableExtra("Restaurant")
        mRestaurant.img = BitmapFactory.decodeByteArray(intent.getByteArrayExtra("Image"), 0, intent.getByteArrayExtra("Image").size)
        restaurant_img.setImageBitmap(mRestaurant.img)
        //findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar).title = mRestaurant.name
        restaurant_name.text = mRestaurant.name
        restaurant_desc.text = mRestaurant.address

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

        // Website button listener
        button_website.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mRestaurant.website))
            startActivity(intent)
        }
    }
}