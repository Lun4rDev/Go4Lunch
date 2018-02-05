package com.hernandez.mickael.go4lunch.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Place
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.Restaurant
import com.hernandez.mickael.go4lunch.adapters.RestaurantListAdapter


/**
 * Created by Mickael Hernandez on 24/01/2018.
 */
class ListFragment : Fragment() {

    private var placesList = ArrayList<Restaurant>()

    private lateinit var mAdapter : RestaurantListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var convertView = inflater.inflate(R.layout.fragment_list, container, false)

        // Setting up adapter with arrayList and listView
        mAdapter = RestaurantListAdapter(context!!, R.layout.row_place, placesList)
        convertView.findViewById<ListView>(R.id.places_list).adapter = mAdapter
        return convertView
    }

    fun resetList(){
        placesList.clear()
        mAdapter.notifyDataSetChanged()
    }
    fun addRestaurant(place: Restaurant){
        placesList.add(place)
        mAdapter.notifyDataSetChanged()
    }
}