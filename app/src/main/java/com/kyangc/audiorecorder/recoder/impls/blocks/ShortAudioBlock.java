package com.kyangc.audiorecorder.recoder.impls.blocks;

import com.kyangc.audiorecorder.recoder.interfaces.IAudioBlock;

/**
 * Usage: Audio data block for holding short format data.
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class ShortAudioBlock implements IAudioBlock {

    public short[] mShorts;

    public int numberOfShortsRead;

    public ShortAudioBlock(short[] shorts) {
        mShorts = shorts;
    }

    @Override
    public double maxVolume() {
        long v = 0;
        for (int i = 0; i < toShort().length; i++) {
            v += toShort()[i] * toShort()[i];
        }
        double mean = v / (double) numberOfShortsRead;
        return 10 * Math.log10(mean);
    }

    @Override
    public short[] toShort() {
        return mShorts;
    }

    @Override
    public byte[] toBytes() {
        int shortIndex, byteIndex;
        byte[] buffer = new byte[numberOfShortsRead * 2];
        shortIndex = byteIndex = 0;
        for (; shortIndex != numberOfShortsRead; ) {
            buffer[byteIndex] = (byte) (mShorts[shortIndex] & 0x00FF);
            buffer[byteIndex + 1] = (byte) ((mShorts[shortIndex] & 0xFF00) >> 8);
            ++shortIndex;
            byteIndex += 2;
        }
        return buffer;
    }
}
