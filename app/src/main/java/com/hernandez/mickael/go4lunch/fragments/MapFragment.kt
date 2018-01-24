package com.hernandez.mickael.go4lunch.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import com.hernandez.mickael.go4lunch.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View


/**
 * Created by Mickael Hernandez on 24/01/2018.
 */
class MapFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater?.inflate(R.layout.fragment_map, container, false)
    }

}