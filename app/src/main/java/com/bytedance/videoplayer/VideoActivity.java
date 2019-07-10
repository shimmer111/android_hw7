package com.bytedance.videoplayer;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";
    IjkMediaPlayer player;
    SurfaceView surfaceView;
    SeekBar seekBar;
    private SurfaceHolder holder;
    boolean isPlaying = false;
    private static int UPDATE = 1;
    private static int MUSICDURATION = 2;
    private long mSeekPosition = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    seekBar.setMax((int) player.getDuration());
                    break;
                case 2:
                    seekBar.setProgress((int)(player.getCurrentPosition() * 100 /player.getDuration()) );
            }
            handler.sendEmptyMessageDelayed(2,500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: mSeekPosition -1--"+mSeekPosition);
        setContentView(R.layout.activity_video);
        Log.d(TAG, "onCreate: mSeekPosition 0--"+mSeekPosition);
        player = new IjkMediaPlayer();
        surfaceView = findViewById(R.id.surfaceView);
        Log.d(TAG, "onCreate: mSeekPosition 1--"+mSeekPosition);
        seekBar = findViewById(R.id.seekBar);

        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }

        //////////刷新seekbar
        if(mSeekPosition != 0){
            player.seekTo(mSeekPosition);
        }
        Log.d(TAG, "onCreate: mSeekPosition 2--"+mSeekPosition);
        //////////
        //播放器加载
        AssetFileDescriptor fileDescriptor = VideoActivity.this.getResources().openRawResourceFd(R.raw.yuminhong);
        RawDataSourceProvider provider = new RawDataSourceProvider(fileDescriptor);
        player.setDataSource(provider);
        holder = surfaceView.getHolder();
        holder.addCallback(new PlayerCallBack());
        Log.d(TAG, "onCreate: mSeekPosition 3--"+mSeekPosition);
        player.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                player.start();
                player.setLooping(true);
                handler.sendEmptyMessage(2);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                     //   player.seekTo((long) (progress * player.getDuration() / 100));
                        long seekTime = (long) (progress * player.getDuration() / 100);
                        if(seekTime - player.getCurrentPosition() > 700){
                            player.seekTo(seekTime);
                        }
                        if(mSeekPosition != 0){
                            player.seekTo(mSeekPosition);
                            mSeekPosition = 0;
                        }
                        Log.d(TAG, "onProgressChanged: mSeekPosition currentPositon:"+player.getCurrentPosition());
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            }
        });

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });
        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    player.pause();
                    isPlaying = false;
                }else{
                    player.start();
                    isPlaying = true;
                }
            }
        });

        player.prepareAsync();



    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player != null && player.isPlaying()){
            mSeekPosition = player.getCurrentPosition();
            Log.d(TAG, "onPause: mSeekPosition"+mSeekPosition);
            player.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: mSeekPosition"+mSeekPosition);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(player != null){
            Log.d(TAG, "onPostResume: mSeekPosition"+mSeekPosition);
            player.seekTo(mSeekPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: mSeekPosition"+mSeekPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SEEK_POSITION_KEY,mSeekPosition);
        Log.d(TAG, "onSaveInstanceState: mSeekPosition"+mSeekPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSeekPosition = savedInstanceState.getLong(SEEK_POSITION_KEY);
        Log.d(TAG, "onRestoreInstanceState: mSeekPosition"+mSeekPosition);
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
