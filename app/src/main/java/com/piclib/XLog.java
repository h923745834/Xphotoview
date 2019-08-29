package com.piclib;

import android.util.Log;

/**
 * Created by hsd on 2019/8/29.
 */
public class XLog {
    private static boolean isDebug = true;

    /**
     *
     * @param isLog  true output log
     */
    public static void Open(boolean isLog){
        isDebug = isLog;
    }

    public static void i(String TAG,String msg){
        if(isDebug){
            Log.i(TAG,msg);
        }
    }

    public static void d(String TAG,String msg){
        if(isDebug){
            Log.d(TAG,msg);
        }
    }

    public static void e(String TAG,String msg){
        if(isDebug){
            Log.e(TAG,msg);
        }
    }
}
