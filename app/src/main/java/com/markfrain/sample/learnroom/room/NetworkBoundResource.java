package com.markfrain.sample.learnroom.room;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.markfrain.sample.learnroom.BaseManager;
import com.markfrain.sample.learnroom.api.HttpObserver;

import io.reactivex.Observable;

/**
 * @authoer create by markfrain
 * @github https://github.com/furuiCQ
 * 高怀见物理 和气得天真
 * 时间: 12/9/20
 * 描述: NetworkBoundResource
 */
public abstract class NetworkBoundResource<ResultType> {
    private final AppExecutors appExecutors;

    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();

    @MainThread
    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        if (BaseManager.getInstance().isRoomInit()) {
            result.setValue(Resource.loading(null));
            LiveData<ResultType> dbSource = loadFromDb();
            result.addSource(dbSource, data -> {
                result.removeSource(dbSource);
                if (shouldFetch(data)) {
                    fetchFromNetwork(dbSource);
                } else {
                    result.addSource(dbSource, newData -> setValue(Resource.success(newData)));
                }
            });
        } else {
            noCacheNetWork();
        }

    }
    protected void onFetchFailed() {

    }

    @MainThread
    private void setValue(Resource<ResultType> newValue) {
        if (!Objects.equals(result.getValue(), newValue)) {
            result.setValue(newValue);
        }
    }

    //没有本地数据库功能时直接调用这个方法
    private void noCacheNetWork() {
        Observable<ResultType> observable = createCall();
        observable.subscribe(new HttpObserver<ResultType>() {
            @Override
            public void onRequestSuccess(ResultType data) {
                setValue(Resource.success(data));
            }

            @Override
            public void onRequestError(Throwable throwable) {
                onFetchFailed();
                setValue(Resource.error(throwable.getLocalizedMessage(), null));
            }
        });
    }

    private void fetchFromNetwork(final LiveData<ResultType> dbSource) {
        Observable<ResultType> observable = createCall();
        MutableLiveData<Observable<ResultType>> apiResponse = new MutableLiveData<>();
        apiResponse.setValue(observable);
//        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource, newData -> setValue(Resource.loading(newData)));
        result.addSource(apiResponse, response -> {
            result.removeSource(apiResponse);
            result.removeSource(dbSource);
            response.subscribe(new HttpObserver<ResultType>() {
                @Override
                public void onRequestSuccess(ResultType data) {
                    appExecutors.diskIO().execute(() -> {
                        saveCallResult(data);
                        appExecutors.mainThread().execute(() ->
                                result.addSource(loadFromDb(),
                                        newData -> setValue(Resource.success(newData)))
                        );
                    });
                }

                @Override
                public void onRequestError(Throwable throwable) {
                    onFetchFailed();
                    result.addSource(dbSource,
                            newData -> setValue(Resource.error(throwable.getLocalizedMessage(), newData)));
                }
            });
        });
    }


    public LiveData<Resource<ResultType>> asLiveData() {
        return result;
    }

    @WorkerThread
    protected abstract void saveCallResult(@NonNull ResultType item);

    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    @NonNull
    @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    @NonNull
    @MainThread
    protected abstract Observable<ResultType> createCall();


}
