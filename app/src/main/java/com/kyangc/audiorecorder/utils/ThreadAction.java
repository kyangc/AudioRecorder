package com.kyangc.audiorecorder.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Usage: Utils for thread switching.
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class ThreadAction {

    private static Handler mHandler;

    public static void executeOnUi(Runnable runnable) {
        if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());
        if (runnable != null) mHandler.post(runnable);
    }

    public static void executeOnNewThread(Runnable runnable) {
        new Thread(runnable).start();
    }
}
