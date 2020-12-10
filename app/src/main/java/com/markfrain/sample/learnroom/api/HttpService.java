package com.markfrain.sample.learnroom.api;

import com.markfrain.sample.learnroom.entitys.City;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * @authoer create by markfrain
 * @github https://github.com/furuiCQ
 * 高怀见物理 和气得天真
 * 时间: 12/10/20
 * 描述: HttpService
 */
public interface HttpService {
    //TODO 接口乱写的，请自行替换自用接口测试
    @GET("/weather/city")
    Observable<ApiResponse<List<City>>> getUserList();

}
