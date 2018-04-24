package com.hernandez.mickael.go4lunch.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.facebook.login.LoginManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.SphericalUtil
import com.hernandez.mickael.go4lunch.R
import com.hernandez.mickael.go4lunch.adapters.BottomBarAdapter
import com.hernandez.mickael.go4lunch.api.ApiInterface
import com.hernandez.mickael.go4lunch.api.ApiSingleton
import com.hernandez.mickael.go4lunch.fragments.ListFragment
import com.hernandez.mickael.go4lunch.fragments.WorkmatesFragment
import com.hernandez.mickael.go4lunch.model.Restaurant
import com.hernandez.mickael.go4lunch.model.Workmate
import com.hernandez.mickael.go4lunch.model.details.DetailsResponse
import com.hernandez.mickael.go4lunch.model.search.Result
import com.hernandez.mickael.go4lunch.model.search.SearchResponse
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Subscription
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOError
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.util.concurrent.TimeUnit


open class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        SearchView.OnQueryTextListener {

    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

    /** Delay between Place Details Web API requests */
    val REQUEST_DELAY = .25.toLong()

    /** Personal Firestore document reference */
    lateinit var mDocRef : DocumentReference

    /** Users Firestore collection reference */
    var mColRef = FirebaseFirestore.getInstance().collection("users")

    /** Shared preferences */
    lateinit var mSharedPrefs : SharedPreferences

    /** Firebase user object */
    var mUser : FirebaseUser? = null

    /** Workmates list */
    private var mWorkmatesList = ArrayList<Workmate>()

    /** Actual restaurants count, used in tests */
    private var mRestaurantCount = 0

    /** Bottom navigation adapter */
    private lateinit var pagerAdapter : BottomBarAdapter

    /** Google Map object */
    private lateinit var mMap : GoogleMap

    /** Permission state */
    private var mLocationPermissionGranted = false

    /** Google APIs clients */
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mGeoDataClient: GeoDataClient
    private lateinit var mPlaceDetectionClient: PlaceDetectionClient
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    /** User last known location */
    private var mLastKnownLocation = Location("")

    /** Default GoogleMap zoom value */
    private val DEFAULT_ZOOM = 14f

    /** Default user location */
    private var mDefaultLocation = LatLng(.0, .0)

    /** Radius in meters used in Nearby Places detection */
    private var mRadius = 10000

    /** Map fragment */
    private var mMapFragment = SupportMapFragment()

    /** Restaurants list fragment */
    private val mListFragment = ListFragment()

    /**Workmates list fragment */
    private val mWorkmatesFragment = WorkmatesFragment()

    /** Restaurant list */
    private val mRestaurantList = ArrayList<Restaurant>()

    /** API Subscribe object */
    private lateinit var mSubscription : Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent unsigned user to enter the app
        if(FirebaseAuth.getInstance().currentUser == null || FirebaseAuth.getInstance().currentUser?.uid == null){
            finish()
        }

        // Inflates the layout
        setContentView(R.layout.activity_main)

        // Limiting the offscreen page limit of the viewpager
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
        mGeoDataClient = Places.getGeoDataClient(applicationContext)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(applicationContext)

        // Construct a FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Navigation drawer
        navigation.setOnNavigationItemSelectedListener{
            item ->
            when (item.itemId) {
                R.id.navigation_map -> {
                    viewPager.currentItem = 0
                    toolbar.title = getString(R.string.restaurant_map)
                }
                R.id.navigation_list -> {
                    viewPager.currentItem = 1
                    toolbar.title = getString(R.string.restaurant_list)
                }
                R.id.navigation_people -> {
                    viewPager.currentItem = 2
                    toolbar.title = getString(R.string.people_view)
                }
            }
            true
        }

        // Use this class as bottom navigation listener
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

        // Toolbar search listener
        searchView.setOnQueryTextListener(this)

        // Firebase Auth State listener
        FirebaseAuth.getInstance().addAuthStateListener {
            if(it.currentUser != null){
                mUser = it.currentUser

                fillDrawerHeader()

                // Initiate user document reference
                mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)

                // Get workmates from the database
                mColRef.addSnapshotListener { colSnapshot, _ ->
                    if(colSnapshot != null && colSnapshot.documents.isNotEmpty()){
                        for(doc in colSnapshot.documents){
                            mWorkmatesList.add(doc.toObject(Workmate::class.java)!!)
                        }
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mMapFragment.getMapAsync(this)
        fillDrawerHeader()
        updateFirestoreData()
    }

    /** Fills the drawer header with data from the user */
    private fun fillDrawerHeader(){
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_name).text = mUser?.displayName
        navView.getHeaderView(0).findViewById<TextView>(R.id.text_user_mail).text = mUser?.email
        Glide.with(applicationContext).load(mUser?.photoUrl).centerCrop().into(nav_view.getHeaderView(0).findViewById(R.id.img_user))
    }

    /** Sends Firebase user data to the Firestore database */
    private fun updateFirestoreData(){
        if(::mDocRef.isInitialized){
            val hMap = HashMap<String, Any>()
            hMap["uid"] = mUser!!.uid
            if(mUser!!.displayName != null){
                hMap["displayName"] = mUser!!.displayName.toString()
            } else {
                hMap["displayName"] = mUser!!.email!!.substring(0, mUser!!.email!!.indexOf('@'))
            }
            if(mUser!!.photoUrl != null){
                hMap["photoUrl"] = mUser!!.photoUrl.toString()
            } else {
                hMap["photoUrl"] = "https://picsum.photos/" + ((300 * Math.random()).toInt() + 300) // random profile picture if missing
            }
            mDocRef.set(hMap, SetOptions.merge())
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

        // Location button
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Zoom buttons
        mMap.uiSettings.isZoomControlsEnabled = true

        // Compass button
        mMap.uiSettings.isCompassEnabled = true

        // Marker window click listener
        mMap.setOnInfoWindowClickListener { marker ->
            if(marker.tag != null && marker.tag is String){
                displayRestaurant(marker.tag.toString())
            } else {
                for(res in mListFragment.getList()){
                    if(res.name == marker.title){
                        displayRestaurant(res.id)
                    }
                }
            }
        }
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

    /** Updates location UI depending on the permission */
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

    /**
       * Get the best and most recent location of the device, which may be null in rare
       * cases when a location is not available.
       */
    private fun getDeviceLocation() {
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
                        if(mListFragment.getList().size <= 0){
                            nearbyRestaurants()
                        }
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

    /** Handles click on a navigation drawer item */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_lunch -> {
                if(::mDocRef.isInitialized){
                    mDocRef.get().addOnCompleteListener {
                        if(it.isSuccessful && it.result.exists()){
                            val id = it.result.getString("restaurantId")
                            if(id != null && id != "") {
                                displayRestaurant(id)
                            } else {
                                Toast.makeText(applicationContext, getString(R.string.no_restaurant_selected), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                        }
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

    /** Displays restaurant according to its id */
    fun displayRestaurant(placeId: String){
        // Shows the loading animation
        loading_view.visibility = View.VISIBLE

        // Check if the restaurant data is already available
        for(res in mRestaurantList){
            if(res.id == placeId){
                displayRestaurantWithObject(res)
                return
            }
        }

        ApiSingleton.getInstance().details(placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .throttleLast(REQUEST_DELAY, TimeUnit.SECONDS)
                .subscribe(object: Observer<DetailsResponse> {
                    override fun onComplete() {}
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(t: DetailsResponse) {
                        // object init
                        val res = t.detailsResult
                        if (res != null) {
                            // Distance between last location and restaurant
                            val distance = floatArrayOf(0f)
                            Location.distanceBetween(res.geometry.location.lat, res.geometry.location.lng,
                                    mLastKnownLocation.latitude, mLastKnownLocation.longitude,
                                    distance)

                            val restaurant = Restaurant(res, arrayListOf<Workmate>(), distance[0])
                            mRestaurantList.add(restaurant)
                            if(res.photos[0].photoReference != null){
                                runOnUiThread {
                                    // Download place image into a bitmap
                                    Glide.with(applicationContext)
                                            .load(ApiSingleton.getUrlFromPhotoReference(res.photos[0].photoReference))
                                            .asBitmap()
                                            .centerCrop()
                                            .into(object: SimpleTarget<Bitmap>(){
                                                override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?) {
                                                    restaurant.img = resource
                                                    displayRestaurantWithObject(restaurant)
                                                }
                                            })
                                }
                                // Else, rely on Android Place Photos API
                            } else {
                                // Get photo
                                Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, res.placeId).setResultCallback {
                                    if (it.photoMetadata != null && it.photoMetadata.count > 0) {
                                        it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                                            restaurant.img = it.bitmap
                                            displayRestaurantWithObject(restaurant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    override fun onError(e: Throwable) {
                        loading_view.visibility = View.GONE
                    }

                })
    }

    /** Opens RestaurantActivity intent based on a Restaurant object */
    private fun displayRestaurantWithObject(res: Restaurant){
        if(res.img != null){
            val intent = Intent(applicationContext, RestaurantActivity::class.java)
            intent.putExtra("Restaurant", res)
            val bStream = ByteArrayOutputStream()
            res.img.compress(Bitmap.CompressFormat.PNG, 100, bStream)
            val byteArray = bStream.toByteArray()
            intent.putExtra("Image", byteArray)

            // Hides the loading animation
            loading_view.visibility = View.GONE

            startActivity(intent)
        }
    }

    /** Use Google Places APIs to find nearby restaurants */
    private fun nearbyRestaurants(){
        // Shows the loading animation

        loading_view.visibility = View.VISIBLE
        // Removes all rows in list
        mListFragment.resetList()
        // Removes all markers on map
        mMap.clear()

        ApiSingleton.getInstance().nearbySearch(locToStr(mLastKnownLocation), mRadius).enqueue(object: Callback<SearchResponse>{
            override fun onFailure(call: Call<SearchResponse>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<SearchResponse>?, response: Response<SearchResponse>?) {
                val res = response?.body()?.results
                if(res != null){
                    addRestaurants(res)
                }
            }

        })
    }

    /** Use Google Places APIs to find nearby restaurants according to the query string */
    private fun searchRestaurants(query: String?){

        // Shows the loading animation
        loading_view.visibility = View.VISIBLE

        // Clears restaurant list
        mRestaurantList.clear()

        // Removes all rows in list
        mListFragment.resetList()

        if(::mMap.isInitialized){
            // Removes all markers on map
            mMap.clear()
        }

        ApiSingleton.getInstance().textSearch(query, locToStr(mLastKnownLocation), mRadius).enqueue(object: Callback<SearchResponse>{
            override fun onFailure(call: Call<SearchResponse>?, t: Throwable?) {}
            override fun onResponse(call: Call<SearchResponse>?, response: Response<SearchResponse>?) {
                val res = response?.body()?.results
                if(res != null){
                    addRestaurants(res)
                }
            }
        })
    }

    /** Adds the restaurants to the Map and List fragments */
    fun addRestaurants(res: List<Result>){
        mRestaurantCount = res.size
        for(p in res){
            //if(p.types.contains("restaurant")){
            // Distance between last location and restaurant
            val distance = floatArrayOf(0f)
            Location.distanceBetween(p.geometry.location.lat, p.geometry.location.lng,
                        mLastKnownLocation.latitude, mLastKnownLocation.longitude,
                        distance)

            // Map marker
            val marker = MarkerOptions()
            marker.position(LatLng(p.geometry.location.lat, p.geometry.location.lng))
            marker.title(p.name.toString())
            marker.snippet(p.vicinity?.toString())
            marker.icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ic_marker))

            val mates = ArrayList<Workmate>()
            if(mWorkmatesList.size > 0){
                for(wm in mWorkmatesList){
                    if(wm.restaurantId == p.placeId && wm.uid != mUser?.uid){
                        mates.add(wm)
                    }
                }
                if(mates.size > 0){
                    marker.icon(bitmapDescriptorFromVector(applicationContext, R.drawable.ic_marker_green))
                }
            }
            if(::mMap.isInitialized){
                val m = mMap.addMarker(marker)
                m.tag = p.placeId
            }
                // Details API call throttled with RxJava
                ApiSingleton.getInstance().details(p.placeId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .throttleLast(REQUEST_DELAY, TimeUnit.SECONDS)
                        .subscribeWith(object: DisposableObserver<DetailsResponse>() {
                            override fun onComplete() {
                            }
                            override fun onNext(t: DetailsResponse) {
                                val result = t.detailsResult
                                if(result != null){
                                    val restaurant = Restaurant(result, mates, distance[0])
                                    mRestaurantList.add(restaurant)
                                        // If there is an image in the result
                                        if(result.photos[0].photoReference != null){
                                            runOnUiThread {
                                                // Download place image into a bitmap
                                                Glide.with(applicationContext)
                                                        .load(ApiSingleton.getUrlFromPhotoReference(result.photos[0].photoReference))
                                                        .asBitmap()
                                                        .centerCrop()
                                                        .into(object: SimpleTarget<Bitmap>(){
                                                            override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?) {
                                                                restaurantReadyToAdd(resource, restaurant)
                                                            }
                                                        })
                                            }
                                            // Else, rely on Android Place Photos API
                                        } else {
                                            Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, result.placeId).setResultCallback {
                                                if (it.photoMetadata != null && it.photoMetadata.count > 0) {
                                                    it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                                                        restaurantReadyToAdd(it.bitmap, restaurant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        })
        }
    }

    // UI function to set bitmap and add restaurant to the list, then hide the loading animation
    private fun restaurantReadyToAdd(bmp: Bitmap, rst: Restaurant) {
        // Set restaurant bitmap
        rst.img = bmp
        // Add the restaurant to the list
        mListFragment.addRestaurant(rst)
        // Hides the loading animation
        if(loading_view.visibility == View.VISIBLE){
            loading_view.visibility = View.GONE
        }
    }

    /** When the user submit a search query */
    override fun onQueryTextSubmit(query: String?): Boolean {
        searchRestaurants(query)
        return false
    }

    /** When the user changes the search query */
    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    /** Returns the number of restaurants found (used in tests) */
    fun getRestaurantCount(): Int {
        return mRestaurantCount
    }

    /** Returns the number of workmates found (used in tests) */
    fun getWorkmatesCount(): Int {
        return mWorkmatesFragment.getList().size
    }

    /** Converts a Vector resource into a BitmapDescriptor */
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /** Puts the lat/lng of a location in a string */
    private fun locToStr(loc: Location): String {
        return "${loc.latitude}, ${loc.longitude}"
    }
}
