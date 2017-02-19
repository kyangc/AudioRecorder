package com.kyangc.audiorecorder.recoder.helper;

import android.content.Context;
import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import java.io.File;

/**
 * Usage: Helper for converting wav to flac.
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class FlacHelper {

    private boolean mIsReady = false;

    private FlacHelper() {
    }

    public static FlacHelper getInstance() {
        return H.I;
    }

    public void init(Context context) {
        AndroidAudioConverter.load(context, new ILoadCallback() {
            @Override
            public void onSuccess() {
                mIsReady = true;
            }

            @Override
            public void onFailure(Exception e) {
                mIsReady = false;
                e.printStackTrace();
            }
        });
    }

    public void convertFlac(Context context, File wav, IConvertCallback callback) {
        AndroidAudioConverter.with(context)
                .setFile(wav)
                .setFormat(AudioFormat.FLAC)
                .setCallback(callback)
                .convert();
    }

    private static class H {
        private static final FlacHelper I = new FlacHelper();
    }
}
