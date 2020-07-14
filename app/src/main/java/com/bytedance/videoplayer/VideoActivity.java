package com.bytedance.videoplayer;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer player;
    private SeekBar seekBar;
    private Boolean stopSetSeek;
    private TextView text;
    private Button btn;
    private Boolean isPausing;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setTitle("Video");

        surfaceView = findViewById(R.id.video);
        seekBar = findViewById(R.id.seekBar);
        text = findViewById(R.id.text);
        btn = findViewById(R.id.btn);
        player = new MediaPlayer();

        stopSetSeek = true;

        Runnable runnable = new Runnable() {
            public void run() {
                if(stopSetSeek) return ;
                updateTimeView();
                seekBar.setProgress(player.getCurrentPosition());
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 1000, 500, TimeUnit.MILLISECONDS);

        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));

            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setLooping(true);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(player.getDuration());
                    player.start();
                    stopSetSeek = false;
                    updateTimeView();
                    btn.setText(getString(R.string.pause));
                    isPausing = false;
                }
            });
            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    changeVideoSize(mp);
                }
            });
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.d("qq123qq","percent : " + percent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(!stopSetSeek) return;
                player.seekTo(i);
                updateTimeView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSetSeek = true;
                if(!isPausing) player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
                updateTimeView();
                if(!isPausing) player.start();
                stopSetSeek = false;
            }
        });

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = btn.getText().toString();
                if(str.equals(getString(R.string.play))){
                    player.start();
                    isPausing = false;
                    btn.setText(getString(R.string.pause));
                }else if(str.equals(getString(R.string.pause))){
                    player.pause();
                    isPausing = true;
                    btn.setText(getString(R.string.play));
                }
            }
        });
    }

    public void updateTimeView(){
        int playTime = player.getCurrentPosition() / 1000;
        int totalTime = player.getDuration() / 1000;
        String str = String.format("%02d:%02d/%02d:%02d",playTime/60,playTime%60,totalTime/60,totalTime%60);
        text.setText(str);
    }

    public void changeVideoSize(MediaPlayer mediaPlayer) {
        int surfaceWidth = surfaceView.getWidth();
        int surfaceHeight = surfaceView.getHeight();

        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
        } else {
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth / (float) surfaceHeight), (float) videoHeight / (float) surfaceWidth);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        surfaceView.setLayoutParams(new ConstraintLayout.LayoutParams(videoWidth, videoHeight));
    }
    private class PlayerCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
