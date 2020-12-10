# 将现成的RxJava+LiveData的项目加入Room的改造Demo

近来公司项目从mvp改造成mvvm。然后升级到AndroidX，于是我就想着把Room也加上。虽然JetPack都发布2年了，但是作为等等党，除了学习以外，终于把项目完整改造过来了。
以下是改造思路：在不破坏Rxjava项目结构的情况下，引入Room，并遵循官方的NetworkBoundResource的单一资源原则来进行改造。以次抛砖引玉，毕竟我查询全网也没有相关案例。
官方案例 GithubBrowserSample的策略是在网络层将数据转换成LiveData<ApiResponse<RequestType>。

```java
LiveData<ApiResponse<RequestType>> apiResponse = createCall();

    @Singleton @Provides
    GithubService provideGithubService() {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(GithubService.class);
    }
```

并且如上创建Retrofit时候使用自定义的AdapterFactory。而这个LiveDataCallAdapterFactory本质是将网络请求返回的ApiResponse，包装成LiveData。具体细节请自行查看源码。
愚蠢的我第一时间想到的是照搬。然后发现并不适合。

1.旧项目的代码结构固化。大部分单独使用RxJava做网络请求基本上配置是这样。

```java
  Retrofit.Builder builder = new Retrofit.Builder()
                .client(okHttpClient())
                .baseUrl(baseUrl())
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
                
```

然后还有自定义Transformer

```java

public static <T> ObservableTransformer<BaseApiEntity<T>, T> applySchedule() {
        return upstream -> upstream
                .map(tBaseApiEntity -> {
                    if (tBaseApiEntity != null) {
                        String code = tBaseApiEntity.getCode();
                        if (ApiCode.SUCCESS.equals(code)) {
                            if (tBaseApiEntity.getData() == null) {
                                throw new NullFromServerException(tBaseApiEntity.getCode(), tBaseApiEntity.getMsg());
                            } else {
                                return tBaseApiEntity.getData();
                            }
                        } else {
                            throw new HttpException(code, tBaseApiEntity.getMsg());
                        }
                    }
                    throw new HttpException(ApiCode.ERROR_NULL_FROM_SERVER);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

```

以及在实际调用接口时是这样

```java

retrofitApi
                .createRetrofit()
                .create(ApiService.User.class)
                .login(account, password)
                .compose(ApiTransformer.<UserInfo>applySchedule())
                .subscribe(new HttpObserver<UserInfo>() {
                    @Override
                    public void onRequestSuccess(UserInfo data) {
                        
                    }

                    @Override
                    public void onRequestError(Throwable throwable) {

                    }
                });
```

所以我第一次尝试就是照搬官方Demo发现不行。

首先Retrofit不支持addCallAdapterFactory多个适配器工厂。
然后我想到自定义AdapterFactory，想说把RxJava2CallAdapterFactory和LiveDataCallAdapterFactory的逻辑合并。然而并不行，因为

```java
public final class RxJava2CallAdapterFactory extends CallAdapter.Factory {
```

哎呀，这丫的是fianl。罢了罢了。我重新开始审视。

2.仔细查看官方Demo，重新🤔思考怎么改造。

逻辑上来说：

```java
//1.无非就是在调用接口前判断本地数据库是否有数据，
//2.如果有就查询数据库数据
//3.如果没有就进行网络请求
//4.网络请求后把数据插入数据库

```

所以按照这个思路，官方的NetworkBoundResource的功能是没什么问题的，问题就出在，如果改造适用于我们自己的网络请求，即

```java

Observable<ResultType> observable = createCall();

```

首先, NetworkBoundResource的构造器改造

```java
@MainThread
    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        if (XXXManager.getInstance().isRoomInit()) {//判断是否需要Room
            result.setValue(Resource.loading(null));//以下和官方代码相同不做赘述
            LiveData<ResultType> dbSource = loadFromDb();
            result.addSource(dbSource, data -> {
                result.removeSource(dbSource);
                if (shouldFetch(data)) {
                    fetchFromNetwork(dbSource);
                } else {
                    result.addSource(dbSource, 
                       newData -> setValue(Resource.success(newData)));
                }
            });
        } else {//不需要Room直接调用网络请求
            noCacheNetWork();
        }

    }
```
    
构造器增加配置判断引用插件包的应用是否需要Room支持。
其次，noCacheNetWork方法如下，也很简单，照着官方的示例做的。


```java
    //没有本地数据库功能时直接调用这个方法
    private void noCacheNetWork() {
        Observable<ResultType> observable = createCall();
        //observable本身做了线程切换
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
```
    
然后，

```java

 private void fetchFromNetwork(final LiveData<ResultType> dbSource) {
       //官方Demo这里是获得一个 LiveData<ApiResponse<RequestType>>,我这里换成observable
        Observable<ResultType> observable = createCall();
       //定义一个liveData包装下上面的observable
        MutableLiveData<Observable<ResultType>> apiResponse = new MutableLiveData<>();
        apiResponse.setValue(observable);
        //private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();
        //MediatorLiveData可以监听liveData变化，从而刷新数据。
        result.addSource(dbSource, newData -> setValue(Resource.loading(newData)));
        result.addSource(apiResponse, response -> {
            result.removeSource(apiResponse);//请求需要从result中移除
            result.removeSource(dbSource);
            //官方这里的是判断response.isSuccessful()，
            //我们这里可以直接改成observable.subscribe
            response.subscribe(new HttpObserver<ResultType>() {
                @Override
                public void onRequestSuccess(ResultType data) {
                    appExecutors.diskIO().execute(() -> {
                        saveCallResult(data);//保存到数据库
                        appExecutors.mainThread().execute(() ->
                              //从数据库加载数据，上面保存完成后，
                             //result监听到数据库数据变化就会刷新数据
                                result.addSource(loadFromDb(),
                                        newData -> setValue(Resource.success(newData)))
                        );
                    });
                }
                //下面是照着官方做的，不赘述。
                @Override
                public void onRequestSuccessButDataNull(String responseCode, String responseMsg) {
                    super.onRequestSuccessButDataNull(responseCode, responseMsg);
                    onFetchFailed();
                    result.addSource(dbSource,
                            newData -> setValue(Resource.error(responseMsg, newData)));
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
```
    
最后，在ViewModel中使用改造后的NetworkBoundResource。

```java
public class SysUserViewModel extends BaseViewModel {
    //官方是使用了dagger2的，不影响。
    //request这个liveData是用来刷新数据的。
    MutableLiveData<Request> request = new MutableLiveData<>();
    
    public MutableLiveData<Request> getRequest() {
        return request;
    }
    //定义我们的用户数据源。
    private LiveData<Resource<List<SystemUser>>> listLiveData = AbsentLiveData.create();

    //官方的类RateLimiter，用于网络请求刷新间隔，
    //逻辑大致是使用ArrayMap存储我们网络请求的参数，比如我这里的userName
    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(10, TimeUnit.MINUTES);
    
     //在构造器中初始化我们的数据源
    public SysUserViewModel() {
        //Transformations.switchMap用于监听request的变化。
        listLiveData = Transformations.switchMap(request, input -> {
            if (input == null) {//初始化构造的时候request肯定是空
                return AbsentLiveData.create();//返回空列表
            } else {
                //获取网络请求的observable
                Observable<List<SystemUser>> observable = new ProjectRequest(new BaseHttpApi())
                            .getUserList(input.pCode, input.userName)
                            .compose(ApiTransformer.<List<SystemUser>>justSwitch());
                //调用NetworkBoundResource。传入AppExecutors
                return new NetworkBoundResource<List<SystemUser>>(
                        new AppExecutors()) {
                    @Override
                    protected void saveCallResult(@NonNull List<SystemUser> item) {
                       //全局初始化一个Dao对象来做数据库操作，这里是往数据库插入数据
                        XXXXManager.getInstance().getSystemUserDao().insertAll(item);
                    }
                    //判断是否要进行网络请求
                    @Override
                    protected boolean shouldFetch(@Nullable List<SystemUser> data) {
                        return data == null || data.isEmpty()
                     || repoListRateLimit.shouldFetch(input.userName);//条件查询需要比对请求参数
                    }

                    @NonNull
                    @Override
                    protected LiveData<List<SystemUser>> loadFromDb() {
                        if (input.userName == null) {
                            return XXXXManager.getInstance().getSystemUserDao().getAll();
                        }
                        return XXXXanager.getInstance().getSystemUserDao().getAll(input.userName);
                    }

                    @NonNull
                    @Override
                    protected Observable<List<SystemUser>> createCall() {
                        return observable;
                    }

                }.asLiveData();
            }

        });
    }

    public LiveData<Resource<List<SystemUser>>> getListLiveData() {
        return listLiveData;
    }

    public void getSystemUserList(String pCode, String filterUserName) {
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
```

OK，大致思路就是这样的。
