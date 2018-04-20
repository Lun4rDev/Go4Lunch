package com.hernandez.mickael.go4lunch.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.hernandez.mickael.go4lunch.api.ApiSingleton;
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

    // Image url
    //public String imgUrl;

    // Distance
    public Float distance;

    // Workmates coming to this restaurant
    public ArrayList<Workmate> workmates;

    // True if open
    public Boolean open;


    public Restaurant(DetailsResult result, ArrayList<Workmate> pWorkmates, Float pDistance, Boolean pOpen){
        id = result.getPlaceId();
        name = result.getName();
        type = result.getTypes().get(0);
        address = result.getFormattedAddress();
        workmates = pWorkmates;
        distance = pDistance;
        open = pOpen;
        //imgUrl = ApiSingleton.getUrlFromPhotoReference(result.getPhotos().get(0).getPhotoReference());
        if(result.getFormattedPhoneNumber() != null){ phone = result.getFormattedPhoneNumber(); }
        if(result.getRating() != null){rating = result.getRating();}
        if(result.getWebsite() != null){website = result.getWebsite();}
    }

    public Restaurant(DetailsResult result, ArrayList<Workmate> pWorkmates, Float pDistance, Boolean pOpen, Bitmap pImg){
        id = result.getPlaceId();
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
        //imgUrl = in.readString();
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
        //dest.writeString(imgUrl);
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