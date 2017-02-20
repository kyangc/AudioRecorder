package com.kyangc.audiorecorder.recoder.impls.sources;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.kyangc.audiorecorder.recoder.interfaces.IAudioSource;
import com.kyangc.audiorecorder.recoder.interfaces.State;

/**
 * Usage: Default audio source.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public class DefaultAudioSource implements IAudioSource {

    @State private int mState = State.Stopped;

    private AudioRecord mAudioRecord;

    @Override
    public AudioRecord recorder() {
        if (mAudioRecord == null) {
            mAudioRecord = new AudioRecord(source(), sampleRateInHz(), channel(), format(),
                    minBufferSize());
        }
        return mAudioRecord;
    }

    @Override
    public int channel() {
        return AudioFormat.CHANNEL_IN_STEREO;
    }

    @Override
    public int source() {
        return MediaRecorder.AudioSource.MIC;
    }

    @Override
    public int format() {
        return AudioFormat.ENCODING_PCM_16BIT;
    }

    @Override
    public int sampleRateInHz() {
        return 44100;
    }

    @Override
    public int minBufferSize() {
        return AudioRecord.getMinBufferSize(sampleRateInHz(), channel(), format());
    }

    @Override
    public int recordState() {
        return mState;
    }

    @Override
    public void setRecordState(@State int state) {
        this.mState = state;
    }
}
