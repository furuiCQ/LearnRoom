package com.markfrain.sample.learnroom.api;

import com.google.gson.annotations.SerializedName;

/**
 *
 *
 */

public class ApiResponse<T> {

    @SerializedName("code")
    private String code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private T data;

    private Object count;

    public ApiResponse() {
    }

    public ApiResponse(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseApiEntity{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public Object getCount() {
        return count;
    }

    public void setCount(Object count) {
        this.count = count;
    }
}
