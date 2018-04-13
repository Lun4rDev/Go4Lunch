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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
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
    private val DEFAULT_ZOOM = 16f

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

    /** On class creation */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Toolbar search listener
        searchView.setOnQueryTextListener(this)

        // Fill UI with Firebase user data
        mUser = FirebaseAuth.getInstance().currentUser
        if(mUser != null && mUser?.uid != null){
            fillDrawerHeader()

            // Initiate user document reference
            mDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser!!.uid)

            // Get workmates from the database
            mColRef.addSnapshotListener { colSnapshot, _ ->
                    if(colSnapshot != null && colSnapshot.documents.isNotEmpty()){
                        for(doc in colSnapshot.documents){
                            mWorkmatesList.add(doc.toObject(Workmate::class.java))
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
        Glide.with(this).load(mUser?.photoUrl).centerCrop().into(nav_view.getHeaderView(0).findViewById(R.id.img_user))
    }

    /** Sends Firebase user data to the Firestore database */
    private fun updateFirestoreData(){
        if(::mDocRef.isInitialized){
            mDocRef.addSnapshotListener { snapshot, _ ->
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

        // Location button
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Zoom buttons
        mMap.uiSettings.isZoomControlsEnabled = true

        // Compass button
        mMap.uiSettings.isCompassEnabled = true

        // Markers buffer
        for(m in mMarkersBuffer){
            mMap.addMarker(m)
        }
        mMarkersBuffer.clear()

        // Marker window click listener
        mMap.setOnInfoWindowClickListener { marker ->
            if(marker.tag is String){
                displayRestaurant(marker.tag.toString())
            }
            /*val res = mListFragment.getList().firstOrNull {
                it.name == marker.title
            }
            if(res != null){
                displayRestaurant(res.id)
            }*/
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
                if(::mDocRef.isInitialized){
                    mDocRef.get().addOnCompleteListener {
                        if(it.isSuccessful && it.result.exists()){
                            val id = it.result.getString("restaurantId")
                            if(id != "") {
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

    private fun locToStr(loc: Location): String {
        return "${loc.latitude}, ${loc.longitude}"
    }

    /** Displays restaurant according to its id */
    fun displayRestaurant(placeId: String){
        // Shows the loading animation
        loading_view.visibility = View.VISIBLE

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

                            // Get photo
                            Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, res.placeId).setResultCallback {
                                if (it.photoMetadata != null && it.photoMetadata.count > 0) {
                                    it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                                        val intent = Intent(applicationContext, RestaurantActivity::class.java)
                                        // TODO: Create getplacebyid by web api to get the open state
                                        intent.putExtra("Restaurant", Restaurant(res, arrayListOf<Workmate>(), distance[0], false, it.bitmap))
                                        val bStream = ByteArrayOutputStream()
                                        it.bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream)
                                        val byteArray = bStream.toByteArray()
                                        intent.putExtra("Image", byteArray)

                                        // Hides the loading animation
                                        loading_view.visibility = View.GONE

                                        startActivity(intent)
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

    var mMarkersBuffer = ArrayList<MarkerOptions>()

    /** Adds the restaurants to the Map and List fragments */
    fun addRestaurants(res: List<Result>){
        mRestaurantCount = res.size
        for(p in res){
            //if(p.types.contains("restaurant")){
            val open = p.openingHours != null && p.openingHours.openNow != null && p.openingHours.openNow
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
            } else {
                mMarkersBuffer.add(marker)
            }

                // Details API call throttled with RxJava
                ApiSingleton.getInstance().details(p.placeId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .throttleLast(REQUEST_DELAY, TimeUnit.SECONDS)
                        .subscribe(object: Observer<DetailsResponse> {
                            override fun onComplete() {

                            }
                            override fun onSubscribe(d: Disposable) {
                            }
                            override fun onNext(t: DetailsResponse) {
                                val result = t.detailsResult
                                if(result != null){
                                    val restaurant = Restaurant(result, mates, distance[0], open)
                                    /*if(!result.photos.isEmpty()){

                                            val bmp = Glide.with(applicationContext)
                                                    .load(ApiSingleton.getUrlFromPhotoReference(result.photos[0].photoReference))
                                                    .asBitmap()
                                                    .centerCrop()
                                                    .into(200, 200)
                                                    .get()
                                            restaurant.img = bmp
                                            mListFragment.addRestaurant(restaurant)
                                            // Hides the loading animation
                                            if(loading_view.visibility == View.VISIBLE){
                                                loading_view.visibility = View.GONE
                                            }
                                        val bmp = Glide.with(applicationContext)
                                                .load(ApiSingleton.getUrlFromPhotoReference(result.photos[0].photoReference))
                                                .asBitmap()
                                                .centerCrop()
                                                .into(200, 200)
                                                .get()

                                                .into(object : Target<Bitmap>(300, 300){
                                                    override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                                                        restaurant.img = resource
                                                        mListFragment.addRestaurant(restaurant)
                                                        // Hides the loading animation
                                                        if(loading_view.visibility == View.VISIBLE){
                                                            loading_view.visibility = View.GONE
                                                        }
                                                    }

                                                })
                                    } else {*/
                                        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, result.placeId).setResultCallback {
                                            if (it.photoMetadata != null && it.photoMetadata.count > 0) {
                                                it.photoMetadata[0].getPhoto(mGoogleApiClient).setResultCallback {
                                                    // Add the restaurant to the list
                                                    restaurant.img = it.bitmap
                                                    mListFragment.addRestaurant(restaurant)

                                                    // Hides the loading animation
                                                    if(loading_view.visibility == View.VISIBLE){
                                                        loading_view.visibility = View.GONE
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            override fun onError(e: Throwable) {

                            }
                        })
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
        return mRestaurantCount
    }

    fun getWorkmatesCount(): Int {
        return mWorkmatesFragment.getList().size
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
