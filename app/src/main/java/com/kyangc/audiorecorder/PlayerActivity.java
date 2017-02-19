package com.kyangc.audiorecorder;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.kyangc.audiorecorder.utils.FileUtils;
import com.kyangc.audiorecorder.utils.T;
import com.kyangc.audiorecorder.utils.TimeUtils;
import java.io.File;
import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    File mFile;

    TextView mTvTime;

    ImageView mIvPlayOrPause, mIvStop;

    SeekBar mSeekBar;

    MediaPlayer mPlayer;

    final Runnable mUpdateAction = new Runnable() {
        @Override
        public void run() {
            updateView();
        }
    };

    ProgressDialog mDialog;

    public static void Launch(Context context, File file) {
        Intent i = new Intent(context, PlayerActivity.class);
        i.putExtra("file", file);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //action bar
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get file
        mFile = (File) getIntent().getSerializableExtra("file");
        if (mFile == null) {
            T.quick(this, "缺少必要信息，初始化失败");
            finish();
        }

        //title
        getSupportActionBar().setTitle(FileUtils.getFileName(mFile));

        //View
        mIvPlayOrPause = (ImageView) findViewById(R.id.ivPlay);
        mIvStop = (ImageView) findViewById(R.id.ivStop);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mTvTime = (TextView) findViewById(R.id.tvTime);

        //Seek bar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mPlayer != null) {
                    mPlayer.seekTo(progress);
                    updateView();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //media player
        mPlayer = new MediaPlayer();
        mPlayer.setLooping(false);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(mFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        showProgressDialog("载入中...");
        mPlayer.prepareAsync();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
                mIvPlayOrPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                updateView();
            }
        });
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                updateTime(0, mp.getDuration());
                updateSeekBar(0, mp.getDuration());
            }
        });

        //button control
        mIvStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop
                if (mPlayer != null) {
                    mPlayer.seekTo(0);
                    mPlayer.pause();
                    updateView();

                    mIvPlayOrPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                }
            }
        });

        mIvPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        //pause
                        mPlayer.pause();
                        mIvPlayOrPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    } else {
                        //play
                        mPlayer.start();
                        updateView();
                        mIvPlayOrPause.setImageResource(R.drawable.ic_pause_white_48dp);
                    }
                }
            }
        });
    }

    private void showProgressDialog(String mesg) {
        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage(mesg);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @SuppressLint("SimpleDateFormat")
    private void updateTime(long current, long total) {
        mTvTime.setText(String.format("%s / %s", TimeUtils.formatTime(current),
                TimeUtils.formatTime(total)));
    }

    private void updateSeekBar(int current, int total) {
        if (mSeekBar.getMax() != total) mSeekBar.setMax(total);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mSeekBar.setProgress(current, true);
        } else {
            mSeekBar.setProgress(current);
        }
    }

    private void updateView() {
        if (mPlayer != null) {
            updateTime(mPlayer.getCurrentPosition(), mPlayer.getDuration());
            updateSeekBar(mPlayer.getCurrentPosition(), mPlayer.getDuration());
            if (mPlayer.isPlaying()) {
                mTvTime.post(mUpdateAction);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
