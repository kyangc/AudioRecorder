package com.kyangc.audiorecorder.recoder.helper;

import android.media.AudioFormat;
import com.kyangc.audiorecorder.recoder.interfaces.IAudioSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Usage: Helper for saving .wav file.
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class WavHelper {

    public static void writeWavHeader(IAudioSource audioSource, File file) throws IOException {
        long totalAudioLen = new FileInputStream(file).getChannel().size();
        try {
            RandomAccessFile wavFile = new RandomAccessFile(file, "rw");
            wavFile.seek(0); // to the beginning
            wavFile.write(new WavHeader(audioSource, totalAudioLen).toBytes());
            wavFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class WavHeader {

        private final IAudioSource source;

        private final long length;

        WavHeader(IAudioSource audioRecordSource, long totalAudioLength) {
            this.source = audioRecordSource;
            this.length = totalAudioLength;
        }

        public byte[] toBytes() {
            long sampleRateInHz = source.sampleRateInHz();
            int channels = (source.channel() == AudioFormat.CHANNEL_IN_MONO ? 1 : 2);
            byte bitsPerSample;
            switch (source.format()) {
                case AudioFormat.ENCODING_PCM_16BIT:
                    bitsPerSample = 16;
                    break;
                case AudioFormat.ENCODING_PCM_8BIT:
                    bitsPerSample = 8;
                    break;
                default:
                    bitsPerSample = 16;
                    break;
            }
            return wavFileHeader(length, length + 36, sampleRateInHz, channels,
                    bitsPerSample * sampleRateInHz * channels / 8, bitsPerSample);
        }

        private byte[] wavFileHeader(long totalAudioLen, long totalDataLen, long longSampleRate,
                int channels, long byteRate, byte bitsPerSample) {

            byte[] header = new byte[44];

            header[0] = 'R'; // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (channels * (bitsPerSample / 8)); //
            // block align
            header[33] = 0;
            header[34] = bitsPerSample; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            return header;
        }
    }
}
