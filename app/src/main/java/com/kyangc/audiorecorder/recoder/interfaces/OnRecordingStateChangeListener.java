package com.kyangc.audiorecorder.recoder.interfaces;

import android.support.annotation.UiThread;
import java.io.File;

/**
 * Usage: Audio event listener.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public interface OnRecordingStateChangeListener {

    @UiThread
    void onAudioDataBlockGet(IAudioBlock audioBlock);

    @UiThread
    void onAudioSaved(File file);

    @UiThread
    void onRecordStart();

    @UiThread
    void onRecordStop();

    @UiThread
    void onRecordPaused();

    @UiThread
    void onRecordResumed();

    class SimpleAdapter implements OnRecordingStateChangeListener {

        @Override
        public void onAudioDataBlockGet(IAudioBlock audioBlock) {

        }

        @Override
        public void onAudioSaved(File file) {

        }

        @Override
        public void onRecordStart() {

        }

        @Override
        public void onRecordStop() {

        }

        @Override
        public void onRecordPaused() {

        }

        @Override
        public void onRecordResumed() {

        }
    }
}
