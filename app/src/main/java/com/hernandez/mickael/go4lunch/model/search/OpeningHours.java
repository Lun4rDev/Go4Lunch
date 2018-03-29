package com.hernandez.mickael.go4lunch.model.search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OpeningHours {

@SerializedName("open_now")
@Expose
private Boolean openNow;

public Boolean getOpenNow() {
return openNow;
}

public void setOpenNow(Boolean openNow) {
this.openNow = openNow;
}

}