package com.hernandez.mickael.go4lunch.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Workmate


/**
 * Created by Mickael Hernandez on 30/01/2018.
 */
open class JoiningListAdapter(context: Context, resource: Int, list: ArrayList<Workmate>) : ArrayAdapter<Workmate>(context, resource, list) {
    override fun getView(position: Int, originalView: View?, container: ViewGroup?): View {
        val convertView : View = originalView ?: LayoutInflater.from(context).inflate(R.layout.row_workmate, container, false)
        val item = getItem(position)

        // Name + "is joining"
        convertView.findViewById<TextView>(R.id.text_workmate).text = String.format(context.getString(R.string.text_workmate_joining), item.displayName)

        // Making text bold
        convertView.findViewById<TextView>(R.id.text_workmate).setTypeface(null, Typeface.BOLD)

        // Image
        Glide.with(context).load(item.photoUrl).into(convertView.findViewById(R.id.image_workmate))

        return convertView
    }
}