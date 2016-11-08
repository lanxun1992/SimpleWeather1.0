package com.lanxun.simpleweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Joey on 2016/11/6.
 */

public class SimpleWeatherOpenHelper extends SQLiteOpenHelper {

    /**
     *
     * Province表建表语句
     */
    public static final String CREATE_PROVINCE = "create table Province("
            +"id integer primary key autoincrement,"
            +"province_name text)";

    /**
     *City表建表语句
     */
    public static final String CREATE_CITY = "create table City("
            +"id integer primary key autoincrement,"
            +"city_name text,"
            +"province_id integer)";

    /**
     *District表建表语句
     */
    public static final String CREATE_DISTRICT = "create table District("
            +"id integer primary key autoincrement,"
            +"district_name text,"
            +"city_id integer)";

    public SimpleWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory
            factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_DISTRICT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
