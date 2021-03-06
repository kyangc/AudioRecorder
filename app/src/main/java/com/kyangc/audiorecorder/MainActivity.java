package com.kyangc.audiorecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import com.kyangc.audiorecorder.list.AudioTrackListAdapter;
import com.kyangc.audiorecorder.recoder.AudioRecorder;
import com.kyangc.audiorecorder.recoder.helper.FlacHelper;
import com.kyangc.audiorecorder.recoder.impls.sources.DefaultAudioSource;
import com.kyangc.audiorecorder.recoder.interfaces.IAudioBlock;
import com.kyangc.audiorecorder.recoder.interfaces.OnAudioSilenceTriggeredListener;
import com.kyangc.audiorecorder.recoder.interfaces.OnRecordingStateChangeListener;
import com.kyangc.audiorecorder.recoder.interfaces.State;
import com.kyangc.audiorecorder.utils.FileUtils;
import com.kyangc.audiorecorder.utils.PermissionUtils;
import com.kyangc.audiorecorder.utils.SystemUtils;
import com.kyangc.audiorecorder.utils.T;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    AudioRecorder mRecorder;

    LinearLayout mLlIndicator;

    RelativeLayout mRlButton;

    RecyclerView mRecyclerView;

    SwipeRefreshLayout mRefreshLayout;

    ImageView mIvIcon;

    AudioTrackListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PermissionUtils.checkPermissions(this,
                Arrays.asList(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO))) {
            //has key permission, init app.
            initComponent();
            initRecorder();
        } else {
            PermissionUtils.requirePermission(this,
                    Arrays.asList(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO));
        }
    }

    @Override
    public void onBackPressed() {
        SystemUtils.quit(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecorder.getCurrentRecordState() == State.Recording) {
            mRecorder.stop();
        }
    }

    private void initComponent() {
        //find..
        mIvIcon = (ImageView) findViewById(R.id.ivIcon);
        mRlButton = (RelativeLayout) findViewById(R.id.rlButton);
        mLlIndicator = (LinearLayout) findViewById(R.id.llVolumeIndicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.rvList);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);

        //Record button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRlButton.setElevation(20);
        }
        mRlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecorder.getCurrentRecordState() == State.Stopped) {
                    mRecorder.start();
                } else if (mRecorder.getCurrentRecordState() == State.Paused) {
                    mRecorder.resume();
                } else if (mRecorder.getCurrentRecordState() == State.Recording) {
                    mRecorder.stop();
                }
            }
        });

        //Volume indicator
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.volume_margin);
        lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.volume_margin);
        for (int i = 0; i < 10; i++) {
            View indicator = new View(this);
            indicator.setBackgroundResource(R.color.colorPrimary);
            indicator.setVisibility(View.INVISIBLE);
            mLlIndicator.addView(indicator, lp);
        }

        //list wav files
        mAdapter = new AudioTrackListAdapter(this);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setLongClickListener(new AudioTrackListAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(final File file) {
                new AlertDialog.Builder(MainActivity.this).setMessage("确定要删除该录音文件吗？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAdapter.removeData(file);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        refreshAudioList();

        //refresh layout
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAudioList();
                mRefreshLayout.setRefreshing(false);
            }
        });

        //init flac helper
        FlacHelper.getInstance().init(this);
    }

    private void initRecorder() {
        mRecorder = AudioRecorder.build()
                .setAudioSource(new DefaultAudioSource())
                .setSilenceStop(true, 60, 3000)
                .setOnSilenceListener(new OnAudioSilenceTriggeredListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSilence(long silenceTime) {
                        Toast.makeText(MainActivity.this,
                                String.format("超过%d秒没有声音，录音停止", silenceTime / 1000),
                                Toast.LENGTH_SHORT).show();
                        if (mRecorder != null) {
                            mRecorder.stop();
                        }
                    }
                })
                .setOnRecordingStateChangeListener(
                        new OnRecordingStateChangeListener.SimpleAdapter() {
                            @Override
                            public void onAudioDataBlockGet(IAudioBlock audioBlock) {
                                displayVolume(audioBlock.maxVolume());
                            }

                            @Override
                            public void onAudioSaved(File file) {
                                T.quick(MainActivity.this,
                                        String.format("录音文件保存在：%s", file.toString()));
                                FlacHelper.getInstance()
                                        .convertFlac(MainActivity.this, file,
                                                new IConvertCallback() {
                                                    @Override
                                                    public void onSuccess(File file) {
                                                        T.quick(MainActivity.this,
                                                                String.format("转换录音文件至 Flac 格式：%s",
                                                                        file.toString()));
                                                        refreshAudioList();
                                                    }

                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        T.quick(MainActivity.this, "转换失败");
                                                        e.printStackTrace();
                                                        refreshAudioList();
                                                    }
                                                });
                            }

                            @Override
                            public void onRecordStart() {
                                mLlIndicator.setVisibility(View.VISIBLE);
                                mIvIcon.setImageResource(R.drawable.ic_stop_white_48dp);
                            }

                            @Override
                            public void onRecordStop() {
                                mLlIndicator.setVisibility(View.INVISIBLE);
                                mIvIcon.setImageResource(R.drawable.ic_settings_voice_white_48dp);
                            }
                        })
                .create();
    }

    private void displayVolume(double volume) {
        Log.i("Volume", volume + "");
        //30-90
        double absVolume = Math.abs(volume - 30);
        int volumeCount = Math.min((int) (absVolume / 60d * 10), 10);
        for (int i = 0; i < 10; i++) {
            if (mLlIndicator.getChildAt(i) != null) {
                mLlIndicator.getChildAt(i)
                        .setVisibility(i < volumeCount ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    private void refreshAudioList() {
        List<File> files =
                FileUtils.listFiles(Environment.getExternalStorageDirectory(), "wav", false);
        List<File> flacFile =
                FileUtils.listFiles(Environment.getExternalStorageDirectory(), "flac", false);
        files.addAll(flacFile);
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (int) (o2.lastModified() - o1.lastModified());
            }
        });
        mAdapter.setData(files);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionGet = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionGet = false;
                break;
            }
        }

        if (permissionGet) {
            initComponent();
            initRecorder();
        } else {
            new AlertDialog.Builder(MainActivity.this).setMessage("应用缺少关键性权限，强制退出")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }
}