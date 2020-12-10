package com.markfrain.sample.learnroom.api;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ApiTransformer {

    public static <T> ObservableTransformer<ApiResponse<T>, T> applySchedule() {
        return upstream -> upstream
                .map(tApiResponse -> {
                    if (tApiResponse != null) {
                        String code = tApiResponse.getCode();
                        if (ApiCode.SUCCESS.equals(code)) {
                            return tApiResponse.getData();
                        } else {
                            throw new HttpException(code, tApiResponse.getMsg());
                        }
                    }
                    throw new HttpException(ApiCode.ERROR);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }
}