package com.markfrain.sample.learnroom.api;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 *
 */

public abstract class HttpObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {
        onRequestStart();
    }

    @Override
    public void onNext(T t) {
        if (t != null) {
            onRequestSuccess(t);
        }
    }

    @Override
    public void onError(Throwable t) {
        onRequestError(t);
    }

    @Override
    public void onComplete() {
        Log.e("TAG", "---------------------------------request-end");
    }


    private void onRequestStart() {

    }

    public abstract void onRequestSuccess(T data);

    public abstract void onRequestError(Throwable throwable);
}
