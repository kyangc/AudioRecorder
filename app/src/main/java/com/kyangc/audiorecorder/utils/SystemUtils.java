package com.kyangc.audiorecorder.utils;

import android.app.Activity;

/**
 * Usage:
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class SystemUtils {

    static long lastBackTime = 0L;

    public static void quit(Activity activity) {
        if (System.currentTimeMillis() - lastBackTime < 2000) {
            activity.finish();
        } else {
            lastBackTime = System.currentTimeMillis();
            T.quick(activity, "再按返回键退出");
        }
    }
}
