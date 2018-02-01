package com.hernandez.mickael.go4lunch.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.gms.location.places.Place
import com.hernandez.mickael.go4lunch.R


/**
 * Created by Mickael Hernandez on 30/01/2018.
 */
open class PlacesListAdapter(context: Context, resource: Int, list: ArrayList<Place>) : ArrayAdapter<Place>(context, resource, list) {
    override fun getView(position: Int, originalView: View?, container: ViewGroup?): View {
        val convertView : View = originalView ?: LayoutInflater.from(context).inflate(R.layout.row_place, container, false)
        val item = getItem(position)
        convertView.findViewById<TextView>(R.id.place_name).text = item.name
        return convertView
    }
}