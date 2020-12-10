package com.markfrain.sample.learnroom.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 *
 * 阳光城api接口
 */

public class BaseHttpApi extends RetrofitApi {

    private final long stamp;

    public BaseHttpApi() {
        stamp = System.currentTimeMillis();
    }


    public long getStamp() {
        return stamp;
    }

    @Override
    public Retrofit createRetrofit() {
        return super.createRetrofit();
    }

    @Override
    protected OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(50, TimeUnit.SECONDS);//设置读取超时时间
        return builder.build();
    }


    @Override
    protected String baseUrl() {
        return "https://jisuweather.api.bdymkt.com/";//TODO 请自行替换后测试使用
    }

}
