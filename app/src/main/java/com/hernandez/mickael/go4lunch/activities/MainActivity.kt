package com.hernandez.mickael.go4lunch.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.SupportMapFragment
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.adapters.BottomBarAdapter
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import com.hernandez.mickael.go4lunch.fragments.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var pagerAdapter : BottomBarAdapter

    /** Navigation drawer */
    lateinit var navView : NavigationView

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {
                //changeFragment(0)
                viewPager.currentItem = 0
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_list -> {
                //changeFragment(1)
                viewPager.currentItem = 1
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_people -> {
                //changeFragment(2)
                viewPager.currentItem = 2
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Sets the toolbar
        //setSupportActionBar(toolbar)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        viewPager.setPagingEnabled(false)
        pagerAdapter = BottomBarAdapter(supportFragmentManager)
        pagerAdapter.addFragments(SupportMapFragment())
        pagerAdapter.addFragments(ListFragment())
        pagerAdapter.addFragments(PeopleFragment())
        viewPager.adapter = pagerAdapter

        // Drawer configuration
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        toolbar.inflateMenu(R.menu.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        return true
    }
}
