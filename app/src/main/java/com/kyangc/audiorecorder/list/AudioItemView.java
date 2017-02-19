package com.kyangc.audiorecorder.list;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kyangc.audiorecorder.PlayerActivity;
import com.kyangc.audiorecorder.R;
import com.kyangc.audiorecorder.utils.FileUtils;
import com.kyangc.audiorecorder.utils.TimeUtils;
import java.io.File;
import java.util.Date;

/**
 * Usage: Item view for display single wav file.
 *
 * Created by chengkangyang on 2017/2/19.
 */
public class AudioItemView extends RelativeLayout {

    TextView mTvFileName, mTvExt, mTvDuration, mTvCreate;

    File mFile;

    AudioTrackListAdapter.OnItemLongClickListener mLongClickListener;

    public AudioItemView(Context context) {
        this(context, null);
    }

    public AudioItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_audio_list_item, this);
        mTvFileName = (TextView) findViewById(R.id.tvFileName);
        mTvExt = (TextView) findViewById(R.id.tvExt);
        mTvDuration = (TextView) findViewById(R.id.tvDuration);
        mTvCreate = (TextView) findViewById(R.id.tvCreate);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFile != null) {
                    PlayerActivity.Launch(getContext(), mFile);
                }
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClick(mFile);
                }
                return false;
            }
        });
    }

    public void setData(File file) {
        if (file != null) {
            mFile = file;
            mTvExt.setText(FileUtils.getFileExtention(file).toUpperCase());
            mTvFileName.setText(FileUtils.getFileName(file));
            mTvDuration.setText(
                    String.format("时长 %s", TimeUtils.formatTime(FileUtils.getWavDuration(file))));
            mTvCreate.setText(String.format("创建于 %s",
                    DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date(file.lastModified()))));
        }
    }

    public void bindLongClickListener(AudioTrackListAdapter.OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }
}
