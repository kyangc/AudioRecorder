package com.kyangc.audiorecorder.utils;

/**
 * Usage: Utils for judging is a block of audio is silence.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public class SilenceUtils {

    public static boolean isSilence(short[] audio, long silenceThreshold) {
        for (short anAudio : audio) {
            if ((anAudio >= silenceThreshold) || (anAudio <= -silenceThreshold)) {
                return false;
            }
        }
        return true;
    }
}
