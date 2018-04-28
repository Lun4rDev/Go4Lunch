
package com.hernandez.mickael.go4lunch.model.details;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DetailsResult {
    @Expose
    private String formattedAddress;

    @SerializedName("formatted_phone_number")
    @Expose
    private String formattedPhoneNumber;

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("id")
    @Expose
    private String id;

    /*@SerializedName("international_phone_number")
    @Expose
    private String internationalPhoneNumber;*/

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    @SerializedName("photos")
    @Expose
    private List<Photo> photos = null;

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @SerializedName("rating")
    @Expose
    private Double rating;

    @SerializedName("reference")
    @Expose
    private String reference;

    /*@SerializedName("scope")
    @Expose
    private String scope;*/
    @SerializedName("types")
    @Expose
    private List<String> types = null;
    @SerializedName("url")
    @Expose
    private String url;

    /*@SerializedName("vicinity")
    @Expose
    private String vicinity;*/
    @SerializedName("website")
    @Expose
    private String website;

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getFormattedPhoneNumber() {
        return formattedPhoneNumber;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Double getRating() {
        return rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebsite() {
        return website;
    }

}
