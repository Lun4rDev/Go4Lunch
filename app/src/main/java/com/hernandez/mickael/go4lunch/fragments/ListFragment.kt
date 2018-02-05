package com.hernandez.mickael.go4lunch.fragments

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.Places
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.adapters.PlacesListAdapter


/**
 * Created by Mickael Hernandez on 24/01/2018.
 */
class ListFragment : Fragment() {

    private lateinit var mGoogleApiClient : GoogleApiClient

    private var placesList = ArrayList<Place>()

    private lateinit var mAdapter : PlacesListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var convertView = inflater.inflate(R.layout.fragment_list, container, false)

        // Setting up adapter with arrayList and listView
        mAdapter = PlacesListAdapter(context!!, R.layout.row_place, placesList)
        convertView.findViewById<ListView>(R.id.places_list).adapter = mAdapter

        // Google API Client
        /*mGoogleApiClient = GoogleApiClient.Builder(context!!)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                //.enableAutoManage(this, this)
                .build()

        mGoogleApiClient.connect()
        if(ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED){
            val result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null)
            result.setResultCallback { likelyPlaces ->
                for (placeLikelihood in likelyPlaces) {
                    placesList.add(placeLikelihood.place)
                }
                likelyPlaces.release()
                mAdapter.notifyDataSetChanged()
            }
        }*/
        return convertView
    }

    fun resetList(){
        placesList.clear()
        mAdapter.notifyDataSetChanged()
    }
    fun addPlace(place: Place){
        placesList.add(place)
        mAdapter.notifyDataSetChanged()
    }
}