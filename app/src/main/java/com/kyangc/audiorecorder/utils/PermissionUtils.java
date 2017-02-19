package com.kyangc.audiorecorder.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import java.util.List;

/**
 * Usage:
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class PermissionUtils {

    public static boolean checkPermissions(Context context, List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String perm : permissions) {
            if (ActivityCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requirePermission(Activity context, List<String> permissions) {
        ActivityCompat.requestPermissions(context, (String[]) permissions.toArray(), 1);
    }
}
