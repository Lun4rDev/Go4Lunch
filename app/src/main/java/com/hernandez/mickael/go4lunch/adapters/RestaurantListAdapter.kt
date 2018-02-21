package com.hernandez.mickael.go4lunch.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
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
        //convertView.findViewById<TextView>(R.id.place_open).text = item.phoneNumber

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
        convertView.findViewById<TextView>(R.id.place_rating).text = item.rating.toString() + "/5"

        // Distance
        val dStr = item.distance.roundToInt().toString() + "m"
        convertView.findViewById<TextView>(R.id.place_distance).text = dStr

        // Image
        convertView.findViewById<ImageView>(R.id.place_image).setImageBitmap(item.img)

        return convertView
    }
}