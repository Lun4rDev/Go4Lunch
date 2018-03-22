package com.hernandez.mickael.go4lunch.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.adapters.BottomBarAdapter
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import com.hernandez.mickael.go4lunch.fragments.WorkmatesFragment
import com.hernandez.mickael.go4lunch.model.Restaurant
import com.hernandez.mickael.go4lunch.model.Workmate
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


open class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        SearchView.OnQueryTextListener {
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

    /** Bottom navigation adapter */
    private lateinit var pagerAdapter : BottomBarAdapter

    /** Map fragment */
    private var mMapFragment = SupportMapFragment()
    //private lateinit var mMapFragment: SupportMapFragment

    /** Google Map object */
    private lateinit var mMap : GoogleMap

    private var mLocationPermissionGranted = false

    private lateinit var mGoogleApiClient: GoogleApiClient

    private lateinit var mGeoDataClient: GeoDataClient

    private lateinit var mPlaceDetectionClient: PlaceDetectionClient

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var mLastKnownLocation = Location("")

    private val DEFAULT_ZOOM = 16f

    private var mDefaultLocation = LatLng(.0, .0)

    private val mListFragment = ListFragment()

    //private val mWorkmatesList = ArrayList<Workmate>()

    private val mWorkmatesFragment = WorkmatesFragment()

    /** On class creation */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager.offscreenPageLimit = 3

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


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)

        // Construct a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Navigation drawer
        navigation.setOnNavigationItemSelectedListener{
            item ->
            when (item.itemId) {
                R.id.navigation_map -> {
                    //changeFragment(0)
                    viewPager.currentItem = 0
                    toolbar.title = getString(R.string.restaurant_map)
                }
                R.id.navigation_list -> {
                    //changeFragment(1)
                    viewPager.currentItem = 1
                    toolbar.title = getString(R.string.restaurant_list)
                }
                R.id.navigation_people -> {
                    //changeFragment(2)
                    viewPager.currentItem = 2
                    toolbar.title = getString(R.string.people_view)
                }
            }
            true
        }

        nav_view.setNavigationItemSelectedListener(this)

        // Retain map fragment instance
        mMapFragment.retainInstance = true

        //mMapFragment = map as SupportMapFragment
        mMapFragment.getMapAsync(this)


        // Bottom bar navigation adapter
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

        // Toolbar configuration
        toolbar.inflateMenu(R.menu.toolbar_main)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.setTitle(R.string.restaurant_map)

        // Toolbar search item and view
        val si = toolbar.menu.findItem(R.id.search_item)
        val searchView = si.actionView as SearchView
        /*val searchText = searchView.findViewById<EditText>(android.support.v7.appcompat.R.id.search_src_text)
        searchText.setTextColor(Color.WHITE)
        searchText.setHintTextColor(Color.WHITE)*/
        // Toolbar search listener
        searchView.setOnQueryTextListener(this)

        // Fill UI with Firebase user data
        mUser = FirebaseAuth.getInstance().currentUser
        if(mUser != null && mUser?.uid != null){
            val navView = findViewById<NavigationView>(R.id.nav_view)
            navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_name).text = mUser?.displayName
            navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_mail).text = mUser?.email
            Glide.with(this).load(mUser?.photoUrl).centerCrop().into(nav_view.getHeaderView(0).findViewById(R.id.img_user))
            mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)
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

                // Get photo
                Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, place.id).setResultCallback {
                    if(it.photoMetadata != null && it.photoMetadata.count > 0) {
                        it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                            val intent = Intent(applicationContext, RestaurantActivity::class.java)
                            intent.putExtra("Restaurant", Restaurant(place, arrayListOf(), distance[0], it.bitmap))
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
        mMapFragment.getMapAsync(this)
        updateFirestoreData()
    }

    /** Sends Firebase user data to the Firestore database */
    private fun updateFirestoreData(){
        if(::mDocRef.isInitialized){
            mDocRef.addSnapshotListener { snapshot, firestoreException ->
                if(snapshot != null && snapshot.exists()){
                    val hMap = HashMap<String, Any>()
                    if(!snapshot.contains("uid")) hMap["uid"] = mUser!!.uid
                    if(!snapshot.contains("displayName")) hMap["displayName"] = mUser!!.displayName.toString()
                    if(!snapshot.contains("photoUrl")) hMap["photoUrl"] = mUser!!.photoUrl.toString()
                    mDocRef.update(hMap)
                }
            }
        }
    }

    /** On Google Map ready */
    override fun onMapReady(pMap: GoogleMap?) {
        // Initialize map object
        mMap = pMap!!

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        // Check for permission and enables user location to be pointed at
        if ((ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.isMyLocationEnabled = true
        }

        if(markerBuffer.size > 0){
            for (m in markerBuffer){
                mMap.addMarker(m)
            }
            markerBuffer.clear()
        }

        // Location button
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Zoom buttons
        mMap.uiSettings.isZoomControlsEnabled = true

        // Compass button
        mMap.uiSettings.isCompassEnabled = true

        // Marker window click listener
        mMap.setOnInfoWindowClickListener { marker ->
            val res = mListFragment.getList().firstOrNull {
                it.name == marker.title
            }
            if(res != null){
                displayRestaurant(res.id)
            }
            true
        }

        searchRestaurants("restaurant")
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
                    if (task.isSuccessful && task.result != null) {
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
                            Toast.makeText(applicationContext, getString(R.string.no_restaurant_selected), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.item_settings -> {
                // Start parameters activity
                startActivity(Intent(applicationContext, ParametersActivity::class.java))
            }
            R.id.item_logout -> {
                // Go back to connection activity
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

    private var markerBuffer = ArrayList<MarkerOptions>()

    /** Use Google Places API to find nearby restaurants according to the query string */
    private fun searchRestaurants(query: String?){
        // Shows the loading animation

        loading_view.visibility = View.VISIBLE
        // Removes all rows in list
        mListFragment.resetList()
        // Removes all markers on map
        mMap.clear()
        // Restaurants filter
        val filter = AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT).build()

        // Places Autocomplete result
        val result = Places.GeoDataApi.getAutocompletePredictions(
                mGoogleApiClient, query, toBounds(mLastKnownLocation, 1.0), filter)
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
                        marker.title(place.name.toString())
                        marker.snippet(place.address.toString())
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                        mMap.addMarker(marker)
                        /*if(::mMap.isInitialized){
                        } else {
                            markerBuffer.add(marker)
                        }*/

                        val mates = ArrayList<Workmate>()
                        mColRef.addSnapshotListener { colSnapshot, p1 ->
                            if(colSnapshot != null && colSnapshot.documents.isNotEmpty()){
                                for(doc in colSnapshot.documents){
                                    if(doc.get("restaurantId") == place.id){
                                        mates.add(doc.toObject(Workmate::class.java))
                                    }
                                }
                                if(mates.count() > 0){
                                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green))
                                }
                            }
                        }
                        // Get photo
                        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, place.id).setResultCallback {
                            if(it.photoMetadata != null && it.photoMetadata.count > 0) {
                                it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {

                                   // Add the restaurant to the list
                                    mListFragment.addRestaurant(Restaurant(place, mates, distance[0], it.bitmap))

                                    // Hides the loading animation
                                    if(loading_view.visibility == View.VISIBLE){
                                        loading_view.visibility = View.GONE
                                    }
                                }
                            }
                        }

                    }
                }
            }
            it.release()
        }
    }

    /** When the user submit a search query */
    override fun onQueryTextSubmit(query: String?): Boolean {
        searchRestaurants(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    fun getRestaurantCount(): Int {
        return mListFragment.getList().size
    }

    fun getWorkmatesCount(): Int {
        return mWorkmatesFragment.getList().size
    }

}
