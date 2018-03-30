package com.hernandez.mickael.go4lunch.api;

import android.content.res.Resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hernandez.mickael.go4lunch.R;

import java.io.IOException;

import io.reactivex.internal.schedulers.RxThreadFactory;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dw on 22/02/17.
 */

/** API Singleton setting up Retrofit with OkHttp3 and Gson, and using the API interface */
public class ApiSingleton {

    /** ApiInterface instance */
    private static ApiInterface mInstance = null;

    //private ApiSingleton(){}

    /** Returns the ApiInterface instance */
    public static ApiInterface getInstance() {
        // instantiate the object if not done already
        if (mInstance == null) {
            mInstance = getRetrofit().create(ApiInterface.class);
        }
        return mInstance;
    }

    /** Returns new Retrofit object */
    private static Retrofit getRetrofit() {
        // Customise Gson instance
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        // Debug interceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Append api-key parameter to every query
        Interceptor apiKeyInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                HttpUrl url = request.url().newBuilder()
                        .addQueryParameter("type", "restaurant") // Only searching restaurants
                        .addQueryParameter("key", ApiInterface.API_KEY) // Places API Key
                        .build();

                request = request.newBuilder().url(url).build();
                return chain.proceed(request);
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(interceptor)  // Enable debugging
                .build();

        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(ApiInterface.API_BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}