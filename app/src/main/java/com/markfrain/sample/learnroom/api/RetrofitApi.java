package com.markfrain.sample.learnroom.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 */

public abstract class RetrofitApi {

    public Retrofit createRetrofit() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .client(okHttpClient())
                .baseUrl(baseUrl())
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        return builder.build();
    }

    private Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient();
        okHttpClient().newBuilder();
        return gsonBuilder.create();
    }

    protected abstract OkHttpClient okHttpClient();

    protected abstract String baseUrl();

}
