package com.lanxun.simpleweather.util;

import java.io.InputStream;

/**
 * Created by Joey on 2016/11/6.
 */
public interface HttpCallbackListener {
    void onFinish(InputStream in);
    void onError(Exception e);
}
