package com.markfrain.sample.learnroom.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.markfrain.sample.learnroom.entitys.City;
import com.markfrain.sample.learnroom.entitys.CityDao;

@Database(entities = {City.class}, version = 1)
public abstract class BaseDB extends RoomDatabase {
    public abstract CityDao cityDao();
}