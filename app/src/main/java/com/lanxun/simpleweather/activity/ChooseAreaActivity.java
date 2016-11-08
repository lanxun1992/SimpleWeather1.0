package com.lanxun.simpleweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lanxun.simpleweather.R;
import com.lanxun.simpleweather.db.SimpleWeatherDB;
import com.lanxun.simpleweather.model.City;
import com.lanxun.simpleweather.model.District;
import com.lanxun.simpleweather.model.Province;
import com.lanxun.simpleweather.util.HttpCallbackListener;
import com.lanxun.simpleweather.util.HttpUtil;
import com.lanxun.simpleweather.util.Utility;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joey on 2016/11/6.
 */

public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_DISTRICT = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private SimpleWeatherDB simpleWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<District> districtList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     *是否从WeatherActivity跳转过来
     */
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("city_selected", false)&&!isFromWeatherActivity ) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        simpleWeatherDB = SimpleWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryDistricts();
                } else if (currentLevel == LEVEL_DISTRICT) {
                    String districtName = districtList.get(i).getDistrictName();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("district_name", districtName);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    @Override
    public void onBackPressed() {
        if(currentLevel == LEVEL_DISTRICT) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    private void queryProvinces() {
        provinceList = simpleWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            queryFromServer("Province");
        }
    }

    private void queryCities() {
        cityList = simpleWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0 ) {
            dataList.clear();
            for (City city: cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer("City");
        }
    }


    private void queryDistricts() {
        districtList = simpleWeatherDB.loadDistricts(selectedCity.getId());
        if (districtList.size() > 0) {
            dataList.clear();
            for (District district:districtList) {
                dataList.add(district.getDistrictName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_DISTRICT;
        }
        else {
            queryFromServer("District");
        }
    }

    private void queryFromServer(final String type) {
        showProgressDialog();
        String address = "http://v.juhe.cn/weather/citys?key=bc52bd0429b5edb0ba49b5850f2b5489";
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(InputStream in) {
                boolean result = Utility.handleResponse(simpleWeatherDB, in); //???
                if (result) {
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals("Province")) {
                                queryProvinces();
                            } else if (type.equals("City")) {
                                queryCities();
                            } else if (type.equals("District")) {
                                queryDistricts();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
