package com.kyangc.audiorecorder.recoder.interfaces;

/**
 * Usage: Interface for recorder instance.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public interface IRecorder {

    /**
     * Start recording process. Data are streamed into a given output file path.
     */
    void start();

    /**
     * Stop recording process.
     */
    void stop();

    /**
     * Pause recording process.
     */
    void pause();

    /**
     * Resume recording process.
     */
    void resume();
}
