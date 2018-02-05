package com.hernandez.mickael.go4lunch

import android.graphics.Bitmap
import com.google.android.gms.location.places.Place

/**
 * Created by Mickael Hernandez on 05/02/2018.
 */
class Restaurant(pl: Place) {

    // Place ID
    var id = pl.id

    // Name
    var name: CharSequence = pl.name

    // Type
    private var type = pl.placeTypes[0]

    // Address
    var address: CharSequence = pl.address

    // Rating
    var rating = pl.rating

    // Distance
    var distance = 0.0f

    // Image
    lateinit var img: Bitmap

}