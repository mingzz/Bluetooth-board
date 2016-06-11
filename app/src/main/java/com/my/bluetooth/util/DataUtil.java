package com.my.bluetooth.util;

import android.content.Context;

/**
 * Created by yiming on 2016/6/10 0010.
 */
public class DataUtil {
    public static String username;//BluetoothAdapter.EXTRA_LOCAL_NAME
    public static String password;

    public static String connectDeviceName = "";


    public static String getSpfString(Context context, String name, String key, String defaultValue){
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key,defaultValue);
    }

    public static void setSpfString(Context context, String name, String key, String value){
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().putString(key,value).apply();
    }

}