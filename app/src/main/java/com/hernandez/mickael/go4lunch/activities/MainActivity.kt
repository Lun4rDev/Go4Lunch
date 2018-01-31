package com.hernandez.mickael.go4lunch.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.adapters.BottomBarAdapter
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import com.hernandez.mickael.go4lunch.fragments.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    /** Sign-in intent code */
    val RC_SIGN_IN = 123
    val RC_LOGOUT = 456

    /** Shared preferences */
    lateinit var mSharedPrefs : SharedPreferences

    /** Firebase user object */
    var mUser : FirebaseUser? = null

    /** Navigation drawer */
    lateinit var navView : NavigationView

    /** Bottom navigation adapter */
    private lateinit var pagerAdapter : BottomBarAdapter

    private lateinit var mMap : GoogleMap

    /** Bottom navigation click listener */
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

    /** On class creation */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        // Bottom navigation view and adapter init
        navView = findViewById(R.id.nav_view)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        viewPager.setPagingEnabled(false)
        pagerAdapter = BottomBarAdapter(supportFragmentManager)
        pagerAdapter.addFragments(SupportMapFragment.newInstance())
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

        val autocompleteFragment = PlaceAutocompleteFragment()
        //supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment)

        val listener = object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place?) {

            }

            override fun onError(p0: Status?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
        autocompleteFragment.setOnPlaceSelectedListener(listener)

        mUser = FirebaseAuth.getInstance().currentUser
        if(mUser != null){
            navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_name).text = mUser!!.displayName
            navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_mail).text = mUser!!.email
            Glide.with(this).load(mUser!!.photoUrl).centerCrop().into(navView.getHeaderView(0).findViewById(R.id.img_user))
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        mMap.uiSettings.isMyLocationButtonEnabled = true
        //gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
    }

    /** Inflates the toolbar menu */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        return true
    }

    /** Handles click on a navigation drawer item */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_lunch -> {}
            R.id.item_settings -> {}
            R.id.item_logout -> {
                //FirebaseAuth.getInstance().signOut
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
