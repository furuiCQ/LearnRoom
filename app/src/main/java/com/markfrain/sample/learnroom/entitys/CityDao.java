package com.markfrain.sample.learnroom.entitys;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CityDao {
    @Query("SELECT * FROM city WHERE  city =:city")
    LiveData<List<City>> getAll(String city);

    @Query("SELECT * FROM system_user")
    LiveData<List<City>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<City> cityList);
}
