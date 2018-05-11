package com.hernandez.mickael.go4lunch.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.hernandez.mickael.go4lunch.model.details.DetailsResult;

import java.util.ArrayList;
import java.util.Calendar;


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
    public ArrayList workmates;

    // True if opened
    public Boolean open;

    // Opening hour and minute in HHMM format
    public String openingTime;

    // Closing hour and minute in HHMM format
    public String closingTime;

    // -2 if(== -1){ = 6
    public Restaurant(DetailsResult result, ArrayList<Workmate> pWorkmates, Float pDistance){
        // In Calendar class, monday is 2 when it is 0 in Places API result
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if(day == -1){ day = 6; }

        id = result.getPlaceId();
        name = result.getName();
        type = result.getTypes().get(0);
        address = result.getFormattedAddress();
        workmates = pWorkmates;
        distance = pDistance;
        open = result.getOpeningHours() != null && result.getOpeningHours().getOpenNow() != null && result.getOpeningHours().getOpenNow();
        if(result.getOpeningHours() != null && result.getOpeningHours().getPeriods().size() >= 7){
            openingTime = result.getOpeningHours().getPeriods().get(day).getOpen().getTime();
            closingTime = result.getOpeningHours().getPeriods().get(day).getOpen().getTime();
            addSeparatorToTimes();
        } else {
            openingTime = "";
            closingTime = "";
        }
        //imgUrl = ApiSingleton.getUrlFromPhotoReference(result.getPhotos().get(0).getPhotoReference());
        if(result.getFormattedPhoneNumber() != null){ phone = result.getFormattedPhoneNumber(); }
        if(result.getRating() != null){rating = result.getRating();}
        if(result.getWebsite() != null){website = result.getWebsite();}
    }

    public Restaurant(DetailsResult result, ArrayList<Workmate> pWorkmates, Float pDistance, Boolean pOpen, Bitmap pImg){
        // In Calendar class, monday is 2 when it is 0 in Places API result
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if(day == -1){ day = 6; }

        id = result.getPlaceId();
        name = result.getName();
        type = result.getTypes().get(0);
        address = result.getFormattedAddress();
        workmates = pWorkmates;
        distance = pDistance;
        img = pImg;
        open = result.getOpeningHours() != null && result.getOpeningHours().getOpenNow() != null && result.getOpeningHours().getOpenNow();
        if(result.getOpeningHours() != null){
            openingTime = result.getOpeningHours().getPeriods().get(day).getOpen().getTime();
            closingTime = result.getOpeningHours().getPeriods().get(day).getOpen().getTime();
            addSeparatorToTimes();
        } else {
            openingTime = "";
            closingTime = "";
        }
        if(result.getFormattedPhoneNumber() != null){ phone = result.getFormattedPhoneNumber(); }
        if(result.getRating() != null){rating = result.getRating();}
        if(result.getWebsite() != null){website = result.getWebsite();}
    }

    private void addSeparatorToTimes() {
        openingTime = openingTime.substring(0, 2) + ":" + openingTime.substring(2);
        closingTime = closingTime.substring(0, 2) + ":" + closingTime.substring(2);
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
        openingTime = in.readString();
        closingTime = in.readString();
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
        dest.writeString(openingTime);
        dest.writeString(closingTime);
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