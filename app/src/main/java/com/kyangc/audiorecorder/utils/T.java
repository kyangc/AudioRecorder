package com.kyangc.audiorecorder.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Usage: Utils for showing toast.
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class T {

    public static void quick(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public static void slow(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }
}
