package com.kyangc.audiorecorder.utils;

/**
 * Usage:
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class TimeUtils {

    public static String formatTime(long totalTime) {
        long hour = 0;
        long minute = 0;
        long second = 0;
        second = totalTime / 1000L;
        if (totalTime <= 1000 && totalTime > 0) {
            second = 1;
        }
        if (second > 60) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        // 转换时分秒 00:00:00
        return (hour >= 10 ? hour : "0" + hour)
                + ":"
                + (minute >= 10 ? minute : "0" + minute)
                + ":"
                + (second >= 10 ? second : "0" + second);
    }
}
