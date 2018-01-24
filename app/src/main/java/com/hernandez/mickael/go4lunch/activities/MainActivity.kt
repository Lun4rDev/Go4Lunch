package com.hernandez.mickael.go4lunch.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.hernandez.mickael.go4lunch.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {
                message.setText(R.string.map_view)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_list -> {
                message.setText(R.string.list_view)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_contact -> {
                message.setText(R.string.contact_view)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
