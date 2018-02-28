package com.hernandez.mickael.go4lunch.fragments;

import android.os.Bundle;

import com.google.android.gms.maps.SupportMapFragment;

public class RetainMapFragment extends SupportMapFragment {

 @Override
 public void onActivityCreated(Bundle savedInstanceState) {
  super.onActivityCreated(savedInstanceState);
  setRetainInstance(true);
 }

}