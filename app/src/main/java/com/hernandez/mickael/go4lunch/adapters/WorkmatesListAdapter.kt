package com.hernandez.mickael.go4lunch.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Restaurant
import com.hernandez.mickael.go4lunch.model.Workmate

/**
 * Created by Mickael Hernandez on 08/02/2018.
 */
open class WorkmatesListAdapter(context: Context, resource: Int, list: ArrayList<Workmate>) : ArrayAdapter<Workmate>(context, resource, list) {
    override fun getView(position: Int, originalView: View?, container: ViewGroup?): View {
        val convertView: View = originalView
                ?: LayoutInflater.from(context).inflate(R.layout.row_workmate, container, false)
        val textView = convertView.findViewById<TextView>(R.id.text_workmate)
        val item = getItem(position)
        // If this workmate hasn't picked a restaurant yet
        if(item.restaurantId == null){
            textView.text = context.getString(R.string.text_notdecided, item.displayName)
            textView.setTypeface(null, Typeface.ITALIC)

        } else {
            textView.text = context.getString(R.string.text_workmate, item.displayName, item.restaurantName)
            textView.setTypeface(null, Typeface.BOLD)
        }

        // Profile image
        Glide.with(context).load(item.photoUrl).into(convertView.findViewById(R.id.image_workmate))
        return convertView
    }
}