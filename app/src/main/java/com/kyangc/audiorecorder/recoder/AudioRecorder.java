package com.kyangc.audiorecorder.recoder;

import android.media.AudioRecord;
import android.os.Environment;
import android.text.format.DateFormat;
import com.kyangc.audiorecorder.recoder.helper.WavHelper;
import com.kyangc.audiorecorder.recoder.impls.blocks.ShortAudioBlock;
import com.kyangc.audiorecorder.recoder.interfaces.IAudioSource;
import com.kyangc.audiorecorder.recoder.interfaces.IRecorder;
import com.kyangc.audiorecorder.recoder.interfaces.OnAudioSilenceTriggeredListener;
import com.kyangc.audiorecorder.recoder.interfaces.OnRecordingStateChangeListener;
import com.kyangc.audiorecorder.recoder.interfaces.State;
import com.kyangc.audiorecorder.utils.FileUtils;
import com.kyangc.audiorecorder.utils.ThreadAction;
import java.io.File;
import java.io.OutputStream;
import java.util.Date;

/**
 * Usage: Audio recoder.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public class AudioRecorder implements IRecorder {

    private File mOutputFile;

    private OutputStream mOutputStream;

    private boolean mShouldJudgeSilence = false;

    private double mSilenceVolumeThreshold;

    private long mSilenceTimeThreshold;

    private IAudioSource mAudioSource;

    private OnAudioSilenceTriggeredListener mOnSilenceListener;

    private OnRecordingStateChangeListener mOnRecordingStateChangeListener;

    private AudioRecorder(File outputFile, boolean shouldJudgeSilence,
            double silenceVolumeThreshold, long silenceTimeThreshold, IAudioSource audioSource) {
        mOutputFile = outputFile;
        mShouldJudgeSilence = shouldJudgeSilence;
        mSilenceVolumeThreshold = silenceVolumeThreshold;
        mSilenceTimeThreshold = silenceTimeThreshold;
        mAudioSource = audioSource;
    }

    private AudioRecorder() {

    }

    /**
     * Get a builder object to config things like filename, whether to detect silence, and set
     * callbacks.
     */
    public static Builder build() {
        return new Builder();
    }

    /**
     * Set {@link OnAudioSilenceTriggeredListener} for responding silence event.
     */
    private void setOnSilenceListener(OnAudioSilenceTriggeredListener onSilenceListener) {
        mOnSilenceListener = onSilenceListener;
    }

    /**
     * Set {@link OnRecordingStateChangeListener} for responding audio data fetching event.
     */
    private void setOnRecordingStateChangeListener(
            OnRecordingStateChangeListener onRecordingStateChangeListener) {
        mOnRecordingStateChangeListener = onRecordingStateChangeListener;
    }

    /**
     * Get current recording state. See {@link State}.
     */
    @State
    public int getCurrentRecordState() {
        return mAudioSource.recordState();
    }

    /**
     * Start to record audio. If current state is {@link State#Stopped}, it will open an output
     * stream and start to poll data into it.  If current state is {@link State#Paused}, it will
     * re-use an existing output stream and continue to poll data.
     */
    @Override
    public void start() {
        if (mAudioSource.recordState() == State.Recording) return;
        if (mAudioSource.recordState() == State.Stopped) {
            mOutputStream = FileUtils.outputStream(mOutputFile);
        }
        if (mOnRecordingStateChangeListener != null) {
            if (mAudioSource.recordState() == State.Stopped) {
                ThreadAction.executeOnUi(new Runnable() {
                    @Override
                    public void run() {
                        mOnRecordingStateChangeListener.onRecordStart();
                    }
                });
            } else if (mAudioSource.recordState() == State.Paused) {
                ThreadAction.executeOnUi(new Runnable() {
                    @Override
                    public void run() {
                        mOnRecordingStateChangeListener.onRecordResumed();
                    }
                });
            }
        }
        startPollingAudioData(mAudioSource, mOutputStream);
    }

    /**
     * Stop recording, make up output wav file.
     */
    @Override
    public void stop() {
        mAudioSource.setRecordState(State.Stopped);
    }

    /**
     * Pause recording.
     */
    @Override
    public void pause() {
        mAudioSource.setRecordState(State.Paused);
    }

    /**
     * Resume recording.
     */
    @Override
    public void resume() {
        startPollingAudioData(mAudioSource, mOutputStream);
    }

    /**
     * Inner method for starting record process and handle recording events.
     */
    private void startPollingAudioData(final IAudioSource source, final OutputStream stream) {
        ThreadAction.executeOnNewThread(new Runnable() {

            long firstSilenceTime = 0;

            int normalSoundBetweenTwoSilence = 0;

            @Override
            public void run() {
                try {
                    //Start recording
                    source.recorder().startRecording();
                    //Set recording flag
                    source.setRecordState(State.Recording);
                    //Create data block
                    final ShortAudioBlock block =
                            new ShortAudioBlock(new short[source.minBufferSize()]);
                    //Keep output when state is recording.
                    while (source.recordState() == State.Recording) {
                        //read audio data, get audio data length.
                        block.numberOfShortsRead =
                                source.recorder().read(block.mShorts, 0, block.mShorts.length);
                        //If nothing went wrong...
                        if (AudioRecord.ERROR_INVALID_OPERATION != block.numberOfShortsRead) {
                            //call back data update on ui thread.
                            if (mOnRecordingStateChangeListener != null) {
                                ThreadAction.executeOnUi(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOnRecordingStateChangeListener.onAudioDataBlockGet(block);
                                    }
                                });
                            }
                            //stream data to file anyway.
                            stream.write(block.toBytes());
                            //check silence if needed
                            if (mShouldJudgeSilence) {
                                if (block.maxVolume() < mSilenceVolumeThreshold) {
                                    //silence happened.
                                    if (firstSilenceTime == 0) {
                                        firstSilenceTime = System.currentTimeMillis();
                                    }

                                    final long silenceDuration =
                                            System.currentTimeMillis() - firstSilenceTime;
                                    if (silenceDuration > mSilenceTimeThreshold
                                            && normalSoundBetweenTwoSilence >= 3) {
                                        //silence event triggered.
                                        normalSoundBetweenTwoSilence = 0;
                                        if (mOnSilenceListener != null) {
                                            ThreadAction.executeOnUi(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mOnSilenceListener.onSilence(silenceDuration);
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    //silence not happen, reset time sum.
                                    firstSilenceTime = 0;
                                    normalSoundBetweenTwoSilence += 1;
                                    normalSoundBetweenTwoSilence =
                                            Math.min(normalSoundBetweenTwoSilence, 5);
                                }
                            }
                        } else {
                            source.setRecordState(State.Stopped);
                        }
                    }
                    //If state is not recording, stop recording, break loop.
                    source.recorder().stop();
                    //And if it's stopped, close output stream.
                    if (source.recordState() == State.Stopped) {
                        //close stream
                        stream.close();
                        //notify stop
                        if (mOnRecordingStateChangeListener != null) {
                            ThreadAction.executeOnUi(new Runnable() {
                                @Override
                                public void run() {
                                    mOnRecordingStateChangeListener.onRecordStop();
                                }
                            });
                        }
                        //write wav header
                        WavHelper.writeWavHeader(source, mOutputFile);
                        //notify write wav
                        if (mOnRecordingStateChangeListener != null) {
                            ThreadAction.executeOnUi(new Runnable() {
                                @Override
                                public void run() {
                                    mOnRecordingStateChangeListener.onAudioSaved(mOutputFile);
                                }
                            });
                        }
                    } else if (source.recordState() == State.Paused) {
                        //notify pause
                        if (mOnRecordingStateChangeListener != null) {
                            ThreadAction.executeOnUi(new Runnable() {
                                @Override
                                public void run() {
                                    mOnRecordingStateChangeListener.onRecordPaused();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * A builder for config audio recorder.
     */
    public static class Builder {

        String fileName = "record_" + DateFormat.format("yyyy-MM-dd-HH:mm:ss", new Date()) + ".wav";

        boolean shouldJudgeSilence;

        double silenceVolumeThreshold;

        long silenceTimeThreshold;

        IAudioSource mAudioSource;

        OnAudioSilenceTriggeredListener mOnSilenceListener;

        OnRecordingStateChangeListener mOnRecordingStateChangeListener;

        public Builder setAudioSource(IAudioSource audioSource) {
            mAudioSource = audioSource;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setSilenceStop(boolean isOpen, double volumeThreshold, long timeThreshold) {
            shouldJudgeSilence = isOpen;
            silenceTimeThreshold = timeThreshold;
            silenceVolumeThreshold = volumeThreshold;
            return this;
        }

        public Builder setOnSilenceListener(OnAudioSilenceTriggeredListener onSilenceListener) {
            mOnSilenceListener = onSilenceListener;
            return this;
        }

        public Builder setOnRecordingStateChangeListener(
                OnRecordingStateChangeListener onRecordingStateChangeListener) {
            mOnRecordingStateChangeListener = onRecordingStateChangeListener;
            return this;
        }

        public AudioRecorder create() {
            AudioRecorder recorder =
                    new AudioRecorder(new File(Environment.getExternalStorageDirectory(), fileName),
                            shouldJudgeSilence, silenceVolumeThreshold, silenceTimeThreshold,
                            mAudioSource);
            if (mOnRecordingStateChangeListener != null) {
                recorder.setOnRecordingStateChangeListener(mOnRecordingStateChangeListener);
            }
            if (mOnSilenceListener != null) {
                recorder.setOnSilenceListener(mOnSilenceListener);
            }
            return recorder;
        }
    }
}
