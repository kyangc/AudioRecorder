package com.kyangc.audiorecorder.recoder.interfaces;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Usage: Recording state.
 *
 * Created by chengkangyang on 2017/2/18.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ State.Paused, State.Recording, State.Stopped })
public @interface State {
    int Recording = 1;
    int Paused = 2;
    int Stopped = 3;
}
