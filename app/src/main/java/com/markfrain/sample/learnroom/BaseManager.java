package com.markfrain.sample.learnroom;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.markfrain.sample.learnroom.entitys.CityDao;
import com.markfrain.sample.learnroom.room.BaseDB;

public class BaseManager {
    private static volatile BaseManager instance;
    private Application application;

    private boolean roomInit;
    private BaseDB db;
    private CityDao cityDao;

    public static BaseManager getInstance() {
        if (instance == null) {
            synchronized (BaseManager.class) {
                if (instance == null) {
                    instance = new BaseManager();
                }
            }
        }
        return instance;
    }

    public Context getContext() {
        if (application != null) {
            return application;
        } else {
            throw new NullPointerException("未初始化BaseManager");
        }
    }

    public BaseManager init(Application application) {
        if (this.application == null) {
            this.application = application;
        }
        return getInstance();
    }

    //判断是否初始化Room数据库
    public BaseManager initRoom(boolean init) {
        roomInit = init;
        if (init) {
            db = Room.databaseBuilder(application,
                    BaseDB.class, application.getPackageName() + "-demoDb")
                    .allowMainThreadQueries()//允许在主线程中查询
                    .build();
            cityDao = db.cityDao();

        }
        return getInstance();
    }

    public BaseDB getDb() {
        return db;
    }

    public CityDao getCityDao() {
        return cityDao;
    }

    public boolean isRoomInit() {
        return roomInit;
    }
}
