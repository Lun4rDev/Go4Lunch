package com.hernandez.mickael.go4lunch.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Restaurant
import com.hernandez.mickael.go4lunch.activities.RestaurantActivity
import com.hernandez.mickael.go4lunch.adapters.RestaurantListAdapter
import java.io.ByteArrayOutputStream




/**
 * Created by Mickael Hernandez on 24/01/2018.
 */
class ListFragment : Fragment() {

    private var placesList = ArrayList<Restaurant>()

    private lateinit var mAdapter : RestaurantListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val convertView = inflater.inflate(R.layout.fragment_list, container, false)

        // Setting up adapter with arrayList and listView
        mAdapter = RestaurantListAdapter(context!!, R.layout.row_place, placesList)
        convertView.findViewById<ListView>(R.id.places_list).adapter = mAdapter

        // Item click listener
        convertView.findViewById<ListView>(R.id.places_list).setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(context, RestaurantActivity::class.java)
            intent.putExtra("Restaurant", placesList[i])
            val bStream = ByteArrayOutputStream()
            placesList[i].img.compress(Bitmap.CompressFormat.PNG, 100, bStream)
            val byteArray = bStream.toByteArray()
            intent.putExtra("Image", byteArray)
            startActivity(intent)
        }

        return convertView
    }

    fun resetList(){
        placesList.clear()
        if(!mAdapter.isEmpty){
            mAdapter.notifyDataSetChanged()
        }
    }
    fun addRestaurant(place: Restaurant){
        placesList.add(place)
        mAdapter.notifyDataSetChanged()
    }
}