package com.hernandez.mickael.go4lunch.model;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoResult;

import java.util.ArrayList;

import kotlinx.android.parcel.Parceler;
import kotlinx.android.parcel.Parcelize;

import static java.lang.System.out;


/**
 * Created by Mickael Hernandez on 05/02/2018.
 */
public class Restaurant implements Parcelable {
    // Place ID
    public String id;

    // Name
    public CharSequence name;

    // Type
    public Integer type;

    // Address
    public CharSequence address;

    // Rating
    public Float rating;

    // Phone number
    public CharSequence phone;

    // Website
    public String website;

    // Image
    public Bitmap img;

    // Distance
    public Float distance;

    // Workmates coming to this restaurant
    public ArrayList workmates;

    // True if open
    public Boolean open;


    public Restaurant(Place place, ArrayList<Workmate> pWorkmates, Float pDistance, Bitmap pImg, Boolean pOpen){
        id = place.getId();
        name = place.getName();
        type = place.getPlaceTypes().get(0);
        address = place.getAddress();
        rating = place.getRating();
        phone = place.getPhoneNumber();
        if(place.getWebsiteUri() != null){website = place.getWebsiteUri().toString();}
        workmates = pWorkmates;
        distance = pDistance;
        img = pImg;
        open = pOpen;
    }

    private Restaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readInt();
        address = in.readString();
        rating = in.readFloat();
        phone= in.readString();
        website = in.readString();
        workmates = in.readArrayList(Workmate.class.getClassLoader());
        distance = in.readFloat();
        open = in.readByte() != 0;
        //img = in.readParcelable(PlacePhotoResult.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name.toString());
        dest.writeInt(type);
        dest.writeString(address.toString());
        dest.writeFloat(rating);
        dest.writeString(phone.toString());
        dest.writeString(website);
        dest.writeList(workmates);
        dest.writeFloat(distance);
        dest.writeByte((byte) (open ? 1 : 0));
        //dest.writeParcelable(img, 0);
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };
}

// val pId: String, val pName: CharSequence, val pType: Int, val pAddress: CharSequence, val pRating: Float
/*@Parcelize
@SuppressLint("ParcelCreator")
data class Restaurant(var pId: String, val pName: CharSequence, val pType: Int, val pAddress: CharSequence, val pRating: Float): Parcelable {
    constructor() : this()
}*/
/*data class Restaurant(val place: Place): Parcelable {

    // Place ID
    var id = place.id

    // Name
    var name: CharSequence = place.name

    // Type
    private var type = place.placeTypes[0]

    // Address
    var address: CharSequence = place.address

    // Rating
    var rating = place.rating

    // Distance
    var distance = 0.0f

    // Image
    lateinit var img: PlacePhotoResult

    /*constructor(parcel: Parcel) : this(
        id = parcel.readString(),
        name = parcel.readString(),
        type = parcel.readInt(),
        address = parcel.readString(),
        rating = parcel.readFloat(),
        distance = parcel.readFloat(),
        img = parcel.readParcelable<Bitmap>(PlacePhotoResult::class.java.classLoader))*/

    /*override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name.toString())
        parcel.writeInt(type)
        parcel.writeString(address.toString())
        parcel.writeFloat(rating)
        parcel.writeFloat(distance)
        parcel.writeParcelable(img, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        override fun createFromParcel(parcel: Parcel): Restaurant {
            return Restaurant(parcel)
        }

        override fun newArray(size: Int): Array<Restaurant?> {
            return arrayOfNulls(size)
        }
    }

}*/