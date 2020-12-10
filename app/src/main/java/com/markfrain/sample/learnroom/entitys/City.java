package com.markfrain.sample.learnroom.entitys;

import androidx.room.Entity;

@Entity(tableName = "city")
public class City {
    String city;
    String cityCode;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }
}
