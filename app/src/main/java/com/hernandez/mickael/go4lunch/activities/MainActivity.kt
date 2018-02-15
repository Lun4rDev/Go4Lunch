package com.hernandez.mickael.go4lunch.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.maps.android.SphericalUtil
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.model.Restaurant
import com.hernandez.mickael.go4lunch.adapters.BottomBarAdapter
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import com.hernandez.mickael.go4lunch.fragments.WorkmatesFragment
import com.hernandez.mickael.go4lunch.model.Workmate
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


open class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    /** Sign-in intent code */
    val RC_SIGN_IN = 123

    val RC_LOGOUT = 456

    val RC_SELECT = 789

    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    /** Personal Firestore document reference */
    lateinit var mDocRef : DocumentReference

    /** Users Firestore collection reference */
    var mColRef = FirebaseFirestore.getInstance().collection("users")

    /** Shared preferences */
    lateinit var mSharedPrefs : SharedPreferences

    /** Firebase user object */
    var mUser : FirebaseUser? = null

    /** Navigation drawer */
    lateinit var navView : NavigationView

    /** Bottom navigation adapter */
    private lateinit var pagerAdapter : BottomBarAdapter

    /** Map fragment */
    private var mMapFragment = SupportMapFragment()

    /** Google Map object */
    private lateinit var mMap : GoogleMap

    private var mLocationPermissionGranted = false

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

    private lateinit var mGoogleApiClient: GoogleApiClient

    private lateinit var mGeoDataClient: GeoDataClient

    private lateinit var mPlaceDetectionClient: PlaceDetectionClient

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var mLastKnownLocation: Location

    private val DEFAULT_ZOOM = 16f

    private lateinit var mDefaultLocation: LatLng

    private val mListFragment = ListFragment()

    private val mWorkmatesFragment = WorkmatesFragment()

    /** On class creation */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Shared Preferences
        mSharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        // Google API Client
        mGoogleApiClient = GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()
        mGoogleApiClient.connect()

        mMapFragment.getMapAsync(this)

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)

        // Construct a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Navigation drawer
        navView = findViewById(R.id.nav_view)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Bottom navigation view
        pagerAdapter = BottomBarAdapter(supportFragmentManager)
        // Adding the 3 fragments
        pagerAdapter.addFragments(mMapFragment)
        pagerAdapter.addFragments(mListFragment)
        pagerAdapter.addFragments(mWorkmatesFragment)
        // Setting the adapter
        viewPager.adapter = pagerAdapter
        viewPager.setPagingEnabled(false)

        // Drawer configuration
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        toolbar.inflateMenu(R.menu.toolbar)

        // Toolbar search item and view
        val si = toolbar.menu.findItem(R.id.search_item)
        val searchView = si.actionView as SearchView

        // Toolbar search listener
        searchView.setOnQueryTextListener (object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                mListFragment.resetList()
                // Restaurants filter
                val filter = AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT).build()

                // Places Autocomplete result
                val result = Places.GeoDataApi.getAutocompletePredictions(
                        mGoogleApiClient, query, toBounds(mLastKnownLocation, 10000.0), filter)
                result.setResultCallback {
                    it.forEach {
                        Places.GeoDataApi.getPlaceById(mGoogleApiClient, it.placeId).setResultCallback {
                            if(it.count > 0){
                                // Restaurant object init
                                val place = it[0]
                                // Distance between last location and restaurant
                                val distance = floatArrayOf(0f)
                                Location.distanceBetween(place.latLng.latitude, place.latLng.longitude,
                                        mLastKnownLocation.latitude, mLastKnownLocation.longitude,
                                        distance)

                                // Map marker
                                val marker = MarkerOptions()
                                marker.position(place.latLng)
                                mMap.addMarker(marker)

                                // Get photo
                                Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, place.id).setResultCallback {
                                    if(it.photoMetadata != null && it.photoMetadata.count > 0) {
                                        it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                                            val res = Restaurant(place, distance[0], it.bitmap)
                                            mListFragment.addRestaurant(res)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    it.release()
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })

        /*val autocompleteFragment = PlaceAutocompleteFragment()
        //supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment)

        val listener = object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place?) {

            }

            override fun onError(p0: Status?) {
            }

        }
        autocompleteFragment.setOnPlaceSelectedListener(listener)*/

        // Fill UI with Firebase user data
        mUser = FirebaseAuth.getInstance().currentUser
        navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_name).text = mUser!!.displayName
        navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_mail).text = mUser!!.email
        Glide.with(this).load(mUser!!.photoUrl).centerCrop().into(navView.getHeaderView(0).findViewById(R.id.img_user))
        mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)
        mColRef.addSnapshotListener { colSnapshot, p1 ->
            if(colSnapshot.documents.isNotEmpty()){
                val res = ArrayList<Workmate>()
                for(doc in colSnapshot.documents){
                    if(doc != null && doc.exists()){
                        res.add(doc.toObject(Workmate::class.java))
                    }
                }
                mWorkmatesFragment.setWorkmates(res)
            }
        }

    }

    /** Displays restaurant according to its id */
    fun displayRestaurant(placeId: String){
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId).setResultCallback {
            if(it.count > 0){
                // Restaurant object init
                val place = it[0]
                // Distance between last location and restaurant
                val distance = floatArrayOf(0f)
                Location.distanceBetween(place.latLng.latitude, place.latLng.longitude,
                        mLastKnownLocation.latitude, mLastKnownLocation.longitude,
                        distance)

                // Map marker
                val marker = MarkerOptions()
                marker.position(place.latLng)
                mMap.addMarker(marker)

                // Get photo
                Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, place.id).setResultCallback {
                    if(it.photoMetadata != null && it.photoMetadata.count > 0) {
                        it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                            val intent = Intent(applicationContext, RestaurantActivity::class.java)
                            intent.putExtra("Restaurant", Restaurant(place, distance[0], it.bitmap))
                            val bStream = ByteArrayOutputStream()
                            it.bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream)
                            val byteArray = bStream.toByteArray()
                            intent.putExtra("Image", byteArray)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateFirestoreData()
    }

    /** Sends Firebase user data to the Firestore database */
    private fun updateFirestoreData(){
        val hMap = HashMap<String, Any>()
        hMap["uid"] = mUser!!.uid
        hMap["displayName"] = mUser!!.displayName.toString()
        hMap["photoUrl"] = mUser!!.photoUrl.toString()
        mDocRef.update(hMap)
    }

    /** On Google Map ready */
    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        mMap.uiSettings.isMyLocationButtonEnabled = true
        //gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

    }

    /** Get permission to access device location */
    private fun getLocationPermission() {
        /*
       * Request location permission, so that we can get the location of the
       * device. The result of the permission request is handled by a callback,
       * onRequestPermissionsResult.
       */
        if ((ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
        {
            mLocationPermissionGranted = true
        }
        else
        {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                //mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    private fun getDeviceLocation() {
        /*
       * Get the best and most recent location of the device, which may be null in rare
       * cases when a location is not available.
       */
        try
        {
            if (mLocationPermissionGranted)
            {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(mLastKnownLocation.latitude,
                                        mLastKnownLocation.longitude), DEFAULT_ZOOM))
                    } else {
                        //Log.d(TAG, "Current location is null. Using defaults.")
                        //Log.e(TAG, "Exception: %s", task.getException())
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM))
                        mMap.uiSettings.isMyLocationButtonEnabled = true
                    }
                }
            }
        }
        catch (e:SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    fun toBounds(location: Location, radiusInMeters: Double): LatLngBounds {
        val center = LatLng(location.longitude, location.latitude)
        val distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0)
        val southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(southwestCorner, northeastCorner)
    }


    /** Handles click on a navigation drawer item */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_lunch -> {
                mDocRef.get().addOnCompleteListener {
                    if(it.isSuccessful){
                        val id = it.result.getString("restaurantId")
                        if(it.result.exists() && id != "") {
                            displayRestaurant(id)
                        } else {
                            Toast.makeText(applicationContext, "You didn\'t select any restaurant.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.item_settings -> {}
            R.id.item_logout -> {
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /** Prevents user from going back to ConnectionActivity */
    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    /** On Google API connection failed */
    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
