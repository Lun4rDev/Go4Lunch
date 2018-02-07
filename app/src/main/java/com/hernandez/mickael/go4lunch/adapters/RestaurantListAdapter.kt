package com.hernandez.mickael.go4lunch.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Restaurant


/**
 * Created by Mickael Hernandez on 30/01/2018.
 */
open class RestaurantListAdapter(context: Context, resource: Int, list: ArrayList<Restaurant>) : ArrayAdapter<Restaurant>(context, resource, list) {
    override fun getView(position: Int, originalView: View?, container: ViewGroup?): View {
        val convertView : View = originalView ?: LayoutInflater.from(context).inflate(R.layout.row_place, container, false)
        val item = getItem(position)

        // Name
        convertView.findViewById<TextView>(R.id.place_name).text = item.name

        // Description (type and address)
        convertView.findViewById<TextView>(R.id.place_desc).text = item.address

        // Opening time
        //convertView.findViewById<TextView>(R.id.place_open).text = item.phoneNumber

        // Image
        convertView.findViewById<ImageView>(R.id.place_image).setImageBitmap(item.img)

        // Distance
        val dStr = item.distance.toString() + "m"
        convertView.findViewById<TextView>(R.id.place_distance).text = dStr

        return convertView
    }
}