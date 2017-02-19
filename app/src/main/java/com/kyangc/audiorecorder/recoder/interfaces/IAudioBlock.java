package com.kyangc.audiorecorder.recoder.interfaces;

/**
 * Usage: A toShort structure for a block of toShort in a single polling.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public interface IAudioBlock {

    /**
     * Get max volume in decibel in this audio block.
     */
    double maxVolume();

    /**
     * Get audio data in short array.
     */
    short[] toShort();

    /**
     * Get audio data in byte array.
     */
    byte[] toBytes();
}
