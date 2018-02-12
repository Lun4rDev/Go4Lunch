package com.hernandez.mickael.go4lunch.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mickael Hernandez on 08/02/2018.
 */
public class Workmate implements Parcelable {
    // ID
    public String uid;

    // Display name
    public String displayName;

    // Photo URL
    public String photoUrl;

    // Restaurant ID
    public String restaurantId;

    // Restaurant name
    public String restaurantName;

    private Workmate(){}

    private Workmate(String pUid, String pDisplayName, String pPhotoUrl, String pRestaurantId, String pRestaurantName){
        uid = pUid;
        displayName = pDisplayName;
        photoUrl = pPhotoUrl;
        restaurantId = pRestaurantId;
        restaurantName = pRestaurantName;
    }

    private Workmate(Parcel in) {
        uid = in.readString();
        displayName = in.readString();
        photoUrl = in.readString();
        restaurantId = in.readString();
        restaurantName = in.readString();
    }

    public static final Creator<Workmate> CREATOR = new Creator<Workmate>() {
        @Override
        public Workmate createFromParcel(Parcel in) {
            return new Workmate(in);
        }

        @Override
        public Workmate[] newArray(int size) {
            return new Workmate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(displayName);
        parcel.writeString(photoUrl);
        parcel.writeString(restaurantId);
        parcel.writeString(restaurantName);
    }
}