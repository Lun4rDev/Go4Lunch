package com.hernandez.mickael.go4lunch.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Restaurant
import kotlin.math.roundToInt


/**
 * Created by Mickael Hernandez on 30/01/2018.
 */
open class RestaurantListAdapter(context: Context, resource: Int, list: ArrayList<Restaurant>) : ArrayAdapter<Restaurant>(context, resource, list) {
    override fun getView(position: Int, originalView: View?, container: ViewGroup?): View {
        val convertView : View = originalView ?: LayoutInflater.from(context).inflate(R.layout.row_restaurant, container, false)
        val item = getItem(position)

        // Name
        convertView.findViewById<TextView>(R.id.place_name).text = item.name

        // Description (type and address)
        convertView.findViewById<TextView>(R.id.place_desc).text = item.address

        // Opening time
        if(item.open != null && item.open && item.closingTime != ""){
            // Restaurant is opened
            convertView.findViewById<TextView>(R.id.place_open).text = String.format(context.getString(R.string.openUntil), item.closingTime)
            convertView.findViewById<TextView>(R.id.place_open).setTextColor(Color.GREEN)
        } else if(item.openingTime != ""){
            // Restaurant is closed
            convertView.findViewById<TextView>(R.id.place_open).text = String.format(context.getString(R.string.closeUntil), item.openingTime)
            convertView.findViewById<TextView>(R.id.place_open).setTextColor(Color.RED)
        } else {
            // Restaurant's opening times unavailable
            convertView.findViewById<TextView>(R.id.place_open).text = context.getString(R.string.times_unavailable)
            convertView.findViewById<TextView>(R.id.place_open).setTextColor(Color.GRAY)
        }

        // Workmates number
        convertView.findViewById<TextView>(R.id.place_mates).text = "(" + item.workmates.size + ")"
        // Rating
        /*for(i in 1..item.rating.toInt()){
            if(i <= 5){
                val img = ImageView(context)
                img.setImageResource(R.drawable.ic_star_rate_black)
                convertView.findViewById<LinearLayout>(R.id.place_rating).addView(img)
            }
        }*/
        // Rating
        if(item.rating != null || item.rating < 0){
            convertView.findViewById<TextView>(R.id.place_rating).text = item.rating.toString() + "/5"
        } else {
            convertView.findViewById<TextView>(R.id.place_rating).text = "n.a"
        }

        // Distance
        val dStr = item.distance.roundToInt().toString() + "m"
        convertView.findViewById<TextView>(R.id.place_distance).text = dStr

        // Image
        if(item.img != null){
            convertView.findViewById<ImageView>(R.id.place_image).setImageBitmap(item.img)
        }

        return convertView
    }
}