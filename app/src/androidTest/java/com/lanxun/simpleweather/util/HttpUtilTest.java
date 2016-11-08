package com.lanxun.simpleweather.util;

import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by Joey on 2016/11/6.
 */
public class HttpUtilTest {
     public void testHttpUtil () throws Exception{
         HttpUtil.sendHttpRequest("http://v.juhe.cn/weather/citys?key=bc52bd0429b5edb0ba49b5850f2b5489",
                new HttpCallbackListener(){
                    @Override
                    public void onFinish(InputStream in) {
                        Log.d("Tag",in.toString());
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });

    }
}