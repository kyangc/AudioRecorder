package com.kyangc.audiorecorder.recoder.interfaces;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Usage: Audio source object.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public interface IAudioSource {

    /**
     * Get {@link AudioRecord} instance.
     */
    AudioRecord recorder();

    /**
     * Get recording channel int. Like {@link AudioFormat#CHANNEL_IN_MONO}
     */
    int channel();

    /**
     * Get audio source. Like {@link MediaRecorder.AudioSource#MIC}.
     */
    int source();

    /**
     * Get recording format. Like {@link AudioFormat#ENCODING_PCM_16BIT}.
     */
    int format();

    /**
     * Get recording sample rate in HZ. Like {@link AudioFormat#ENCODING_PCM_16BIT}
     */
    int sampleRateInHz();

    /**
     * Get min buffer size by invoking {@link AudioRecord#getMinBufferSize(int, int, int)}.
     */
    int minBufferSize();

    /**
     * Flag for using or not this audio toShort source.
     */
    @State
    int recordState();

    /**
     * Set toShort source usability.
     */
    void setRecordState(@State int state);
}
