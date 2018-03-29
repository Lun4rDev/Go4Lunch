package com.hernandez.mickael.go4lunch.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.hernandez.mickael.go4lunch.model.details.DetailsResult;

import java.util.ArrayList;


/**
 * Created by Mickael Hernandez on 05/02/2018.
 */
public class Restaurant implements Parcelable {
    // result ID
    public String id;

    // Name
    public CharSequence name;

    // Type
    public String type;

    // Address
    public CharSequence address;

    // Rating
    public Double rating = -1d;

    // Phone number
    public CharSequence phone = "";

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


    public Restaurant(DetailsResult result, ArrayList<Workmate> pWorkmates, Float pDistance, Bitmap pImg, Boolean pOpen){
        id = result.getId();
        name = result.getName();
        type = result.getTypes().get(0);
        address = result.getFormattedAddress();
        workmates = pWorkmates;
        distance = pDistance;
        img = pImg;
        open = pOpen;
        if(result.getFormattedPhoneNumber() != null){ phone = result.getFormattedPhoneNumber(); }
        if(result.getRating() != null){rating = result.getRating();}
        if(result.getWebsite() != null){website = result.getWebsite();}
    }

    private Restaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        address = in.readString();
        rating = in.readDouble();
        phone= in.readString();
        website = in.readString();
        workmates = in.readArrayList(Workmate.class.getClassLoader());
        distance = in.readFloat();
        open = in.readByte() != 0;
        //img = in.readParcelable(resultPhotoResult.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name.toString());
        dest.writeString(type);
        dest.writeString(address.toString());
        dest.writeDouble(rating);
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
/*data class Restaurant(val result: result): Parcelable {

    // result ID
    var id = result.id

    // Name
    var name: CharSequence = result.name

    // Type
    private var type = result.resultTypes[0]

    // Address
    var address: CharSequence = result.address

    // Rating
    var rating = result.rating

    // Distance
    var distance = 0.0f

    // Image
    lateinit var img: resultPhotoResult

    /*constructor(parcel: Parcel) : this(
        id = parcel.readString(),
        name = parcel.readString(),
        type = parcel.readInt(),
        address = parcel.readString(),
        rating = parcel.readFloat(),
        distance = parcel.readFloat(),
        img = parcel.readParcelable<Bitmap>(resultPhotoResult::class.java.classLoader))*/

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