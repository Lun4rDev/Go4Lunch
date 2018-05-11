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
import com.hernandez.mickael.go4lunch.activities.RestaurantActivity
import com.hernandez.mickael.go4lunch.adapters.RestaurantListAdapter
import com.hernandez.mickael.go4lunch.model.Restaurant
import java.io.ByteArrayOutputStream


/**
 * Fragment containing a ListView that is displaying restaurants
 * Created by Mickael Hernandez on 24/01/2018.
 */
class ListFragment : Fragment() {

    /** Restaurant list */
    private var placesList = ArrayList<Restaurant>()

    /** Layout's ListView */
    private lateinit var listView: ListView

    /** Adapter between the restaurant list and the ListView */
    private lateinit var mAdapter : RestaurantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mAdapter = RestaurantListAdapter(context!!, R.layout.row_restaurant, placesList)
    }

    override fun onResume() {
        super.onResume()
        if(!::mAdapter.isInitialized){
            mAdapter = RestaurantListAdapter(context!!, R.layout.row_restaurant, placesList)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setting up adapter with arrayList
        listView = view.findViewById(R.id.places_list)
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

    /** Clears the list's content */
    fun resetList(){
        if(placesList.size > 0){
            placesList.clear()
            if(::mAdapter.isInitialized){
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    /** Returns the restaurant list */
    fun getList(): ArrayList<Restaurant>{
        return placesList
    }

    /** Adds a restaurant to the list */
    fun addRestaurant(place: Restaurant){
        if(placesList.none { it.id == place.id }){
            placesList.add(place)
            if(::mAdapter.isInitialized){
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    fun updateRestaurantBitmap(bmp:Bitmap, rst: String){
        placesList.find { it.id == rst }?.img = bmp
        mAdapter.notifyDataSetChanged()
    }
}