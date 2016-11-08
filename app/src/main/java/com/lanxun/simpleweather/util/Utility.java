package com.lanxun.simpleweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import com.lanxun.simpleweather.db.SimpleWeatherDB;
import com.lanxun.simpleweather.model.City;
import com.lanxun.simpleweather.model.District;
import com.lanxun.simpleweather.model.Province;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joey on 2016/11/6.
 */

public class Utility {
    private static SimpleWeatherDB msimpleWeatherDB;
    public static boolean handleResponse(SimpleWeatherDB simpleWeatherDB, InputStream in) {
        msimpleWeatherDB = simpleWeatherDB;
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        boolean flag = false;
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String nodeName = reader.nextName();
                if (nodeName.equals("resultcode")) {
                    Log.d("Tag","resultcode"+reader.nextString());
                    flag = true;
                } else if (nodeName.equals("result") && flag) {
                    saveAreaToDatabase(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            reader.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存到数据库
     */
    private static boolean saveAreaToDatabase(JsonReader reader) {
        String provinceName = null;
        String cityName = null;
        String districtName = null;
        List<String> provinceNames = new ArrayList<>();
        List<String> cityNames = new ArrayList<>();
        boolean changedProvince = false;
        boolean changedCity = false;
        int provinceId = 0;
        int cityId = 0;
        int districtId = 0;
        Province previousProvince = new Province();
        City previousCity = new City();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String nodeName = reader.nextName();
                    if (nodeName.equals("province")) {
                        provinceName = reader.nextString().trim();
                        if (!provinceNames.contains(provinceName)) {
                            provinceNames.add(provinceName);
                            changedProvince = true;
                            provinceId++;
                        }
                    } else if (nodeName.equals("city")) {
                        cityName = reader.nextString().trim();
                        if (!cityNames.contains(cityName)) {
                            cityNames.add(cityName);
                            changedCity = true;
                            cityId++;
                            Log.d("Utility", "changedCity");
                        }
                    } else if (nodeName.equals("district")) {
                        districtName = reader.nextString().trim();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();

                if (changedProvince) {
                    Province province = new Province();
                    province.setId(provinceId);
                    province.setProvinceName(provinceName);
                    previousProvince = province;
                    msimpleWeatherDB.saveProvince(province);
                    changedProvince = false;
                }

                if (changedCity) {
                    City city = new City();
                    city.setId(cityId);
                    city.setCityName(cityName);
                    city.setProvinceId(previousProvince.getId());
                    previousCity = city;
                    msimpleWeatherDB.saveCity(city);
                    changedCity = false;
                }

                District district = new District();
                districtId++;
                district.setId(districtId);
                district.setDistrictName(districtName);
                district.setCityId(previousCity.getId());
                msimpleWeatherDB.saveDistrict(district);
            }
            reader.endArray();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *处理服务器返回的JSON数据
     */
    public static boolean handleWeatherResponse(Context context, InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuilder response = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return parseWeatherInfo(context, response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解析天气信息
     * @param context
     * @param data
     * @return
     */
    private static boolean parseWeatherInfo(Context context, String data) {

        try {
            JSONObject response = new JSONObject(data);
            String resultCode = response.getString("resultcode");
            if (resultCode.equals("200")) {
                JSONObject result = response.getJSONObject("result");
                JSONObject today = result.getJSONObject("today");
                String temperature = today.getString("temperature");
                String cityName = today.getString("city");
                String weather = today.getString("weather");
                String date = today.getString("date_y");
                return saveWeatherInfo(context, cityName, weather, temperature, date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存天气信息
     * @param context
     * @param cityName
     * @param weather
     * @param temperature
     * @param date
     * @return
     */
    private static boolean saveWeatherInfo(Context context, String cityName, String weather,
                                           String temperature, String date) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather", weather);
        editor.putString("temperature", temperature);
        editor.putString("date", date);
        editor.commit();
        return false;
    }}
