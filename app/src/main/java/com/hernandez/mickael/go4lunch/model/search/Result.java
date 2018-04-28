package com.hernandez.mickael.go4lunch.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

@SerializedName("geometry")
@Expose
private Geometry geometry;

@SerializedName("icon")
@Expose
private String icon;

@SerializedName("id")
@Expose
private String id;

@SerializedName("name")
@Expose
private String name;

@SerializedName("place_id")
@Expose
private String placeId;

@SerializedName("reference")
@Expose
private String reference;

@SerializedName("vicinity")
@Expose
private String vicinity;

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

public String getPlaceId() {
return placeId;
}

public String getReference() {
return reference;
}

public void setReference(String reference) {
this.reference = reference;
}

public String getVicinity() {
return vicinity;
}

}