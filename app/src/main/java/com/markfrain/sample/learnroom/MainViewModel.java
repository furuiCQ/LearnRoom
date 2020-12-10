package com.markfrain.sample.learnroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.markfrain.sample.learnroom.api.ApiTransformer;
import com.markfrain.sample.learnroom.api.BaseHttpApi;
import com.markfrain.sample.learnroom.api.HttpService;
import com.markfrain.sample.learnroom.entitys.City;
import com.markfrain.sample.learnroom.room.AbsentLiveData;
import com.markfrain.sample.learnroom.room.AppExecutors;
import com.markfrain.sample.learnroom.room.NetworkBoundResource;
import com.markfrain.sample.learnroom.room.RateLimiter;
import com.markfrain.sample.learnroom.room.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class MainViewModel extends ViewModel {
    MutableLiveData<Request> request = new MutableLiveData<>();

    public MutableLiveData<Request> getRequest() {
        return request;
    }

    private LiveData<Resource<List<City>>> listLiveData = AbsentLiveData.create();


    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(10, TimeUnit.MINUTES);

    public MainViewModel() {
        listLiveData = Transformations.switchMap(request, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            } else {
                Observable<List<City>> observable =
                        new BaseHttpApi().createRetrofit()
                                .create(HttpService.class)
                                .getUserList()
                                .compose(ApiTransformer.<List<City>>applySchedule());
                return new NetworkBoundResource<List<City>>(
                        new AppExecutors()) {
                    @Override
                    protected void saveCallResult(@NonNull List<City> item) {
                        BaseManager.getInstance().getCityDao().insertAll(item);
                    }

                    @Override
                    protected boolean shouldFetch(@Nullable List<City> data) {
                        return data == null || data.isEmpty() || repoListRateLimit.shouldFetch(input.userName);//条件查询需要比对请求参数
                    }

                    @NonNull
                    @Override
                    protected LiveData<List<City>> loadFromDb() {
                        if (input.userName == null) {
                            return BaseManager.getInstance().getCityDao().getAll();
                        }
                        return BaseManager.getInstance().getCityDao().getAll(input.userName);
                    }

                    @NonNull
                    @Override
                    protected Observable<List<City>> createCall() {
                        return observable;
                    }

                }.asLiveData();
            }

        });
    }

    public LiveData<Resource<List<City>>> getListLiveData() {
        return listLiveData;
    }

    public void getCityList(String pCode, String filterUserName) {
        getRequest().setValue(new Request(pCode, filterUserName));
    }

    public class Request {
        String pCode;
        String userName;

        public Request(String pCode, String userName) {
            this.pCode = pCode;
            this.userName = userName;
        }

    }
}
