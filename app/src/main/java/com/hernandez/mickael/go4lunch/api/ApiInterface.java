package com.hernandez.mickael.go4lunch.api;

import com.hernandez.mickael.go4lunch.model.details.DetailsResponse;
import com.hernandez.mickael.go4lunch.model.search.SearchResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
/**
 * Created by Mickael Hernandez on 22/02/17.
 */

/** New York Times API interface */
public interface ApiInterface {

    /** API key */
    String API_KEY = "AIzaSyClssA-rx1d2Wip2KBO25PQMQqAuM5IL0o";

    /** API base URL */
    String API_BASE_URL = "https://maps.googleapis.com/maps/api/place/";

    /** Nearby Search API call */
    @GET("nearbysearch/json")
    Call<SearchResponse> nearbySearch(
            @Query("location") String loc,
            @Query("radius") Integer rad);

    /** Text Search API call */
    @GET("textsearch/json")
    Call<SearchResponse> textSearch(
            @Query("query") String query,
            @Query("location") String loc,
            @Query("radius") Integer rad);

    /** Place Details API call */
    @GET("details/json")
    Observable<DetailsResponse> details(
            @Query("placeid") String placeId);

}