package com.hernandez.mickael.go4lunch.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
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

    private lateinit var mPlaceholder : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mAdapter = RestaurantListAdapter(context!!, R.layout.row_restaurant, placesList)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setting up adapter with arrayList
        val listView = view.findViewById<ListView>(R.id.places_list)
        listView.adapter = mAdapter
        listView.emptyView = view.findViewById(R.id.list_empty)

        // Item click listener
        listView.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(context, RestaurantActivity::class.java)
            intent.putExtra("Restaurant", placesList[i])
            val bStream = ByteArrayOutputStream()
            placesList[i].img.compress(Bitmap.CompressFormat.PNG, 100, bStream)
            val byteArray = bStream.toByteArray()
            intent.putExtra("Image", byteArray)
            startActivity(intent)
        }
    }

    fun resetList(){
        placesList.clear()
        /*if(!mAdapter.isEmpty){
            mAdapter.notifyDataSetChanged()
        }*/
    }

    fun getList(): ArrayList<Restaurant>{
        return placesList
    }
    fun addRestaurant(place: Restaurant){
        placesList.add(place)
        mAdapter.notifyDataSetChanged()
    }
}