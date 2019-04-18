package com.ffmpeg.video;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ffmpeg.audio.R;
import com.xiaowenzi.ffpeglib.bean.WlTimeInfoBean;
import com.xiaowenzi.ffpeglib.listener.WlOnCompleteListener;
import com.xiaowenzi.ffpeglib.listener.WlOnErrorListener;
import com.xiaowenzi.ffpeglib.listener.WlOnLoadListener;
import com.xiaowenzi.ffpeglib.listener.WlOnParparedListener;
import com.xiaowenzi.ffpeglib.listener.WlOnPauseResumeListener;
import com.xiaowenzi.ffpeglib.listener.WlOnTimeInfoListener;
import com.xiaowenzi.ffpeglib.log.MyLog;
import com.xiaowenzi.ffpeglib.opengl.WlGLSurfaceView;
import com.xiaowenzi.ffpeglib.player.WlPlayer;
import com.xiaowenzi.ffpeglib.util.WlTimeUtil;

public class MainActivity extends AppCompatActivity {

    private WlPlayer wlPlayer;
    private TextView tvTime;
    private WlGLSurfaceView wlGLSurfaceView;
    private SeekBar seekbar;
    private int position;
    private boolean seek = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tv_time);
        wlGLSurfaceView = findViewById(R.id.wlGLSurfaceView);
        seekbar = findViewById(R.id.seekbar);

        //实例化一个播放器
        wlPlayer = new WlPlayer();
        wlPlayer.setWlGLSurfaceView(wlGLSurfaceView);
        wlPlayer.setWlOnParparedListener(new WlOnParparedListener() {
            @Override
            public void onParpared() {
                wlPlayer.start();
            }
        });

        wlPlayer.setWlOnLoadListener(new WlOnLoadListener() {
            @Override
            public void onLoad(boolean load) {
                if (load) {
                    MyLog.e("加载中...");
                } else {
                    MyLog.e("播放中...");
                }
            }
        });

        wlPlayer.setWlOnPauseResumeListener(new WlOnPauseResumeListener() {
            @Override
            public void onPause(boolean pause) {
                if (pause) {
                    MyLog.e("暂停中...");
                } else {
                    MyLog.e("播放中...");
                }
            }
        });

        wlPlayer.setWlOnTimeInfoListener(new WlOnTimeInfoListener() {
            @Override
            public void onTimeInfo(WlTimeInfoBean timeInfoBean) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = timeInfoBean;
                handler.sendMessage(message);
            }
        });

        wlPlayer.setWlOnErrorListener(new WlOnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                MyLog.e("code:" + code + ", msg:" + msg);
            }
        });

        wlPlayer.setWlOnCompleteListener(new WlOnCompleteListener() {
            @Override
            public void onComplete() {
                MyLog.e("播放完成, 下一首");
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                position = progress * wlPlayer.getDuration() / 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
                seek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                wlPlayer.seek(position);
                seek = false;
            }
        });

        //电视测试
        //wlPlayer.setSource("http://192.168.253.33/live/0002/index.m3u8");
        //wlPlayer.parpared();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_CHANNEL_DOWN){
            wlPlayer.playNext("http://192.168.253.33/live/0001/index.m3u8");
        }else if (keyCode==KeyEvent.KEYCODE_CHANNEL_UP){
            wlPlayer.playNext("http://192.168.253.33/live/0003/index.m3u8");
        }else if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return false;
    }

    //点击按钮事件
    public void begin(View view) {
        //wlPlayer.setSource(Environment.getExternalStorageDirectory() + "/12/test2.mp4");
        wlPlayer.setSource("http://192.168.43.12/18/test1.mp4");
        wlPlayer.parpared();
    }

    public void stop(View view) {
        wlPlayer.stop();
    }

    public void pause(View view) {
        wlPlayer.pause();
    }

    public void play(View view) {
        wlPlayer.resume();
    }

    public void seekTo(View view) {
        //跳至120秒
        wlPlayer.seek(120);
    }

    public void next(View view) {
        //wlPlayer.playNext(Environment.getExternalStorageDirectory() + "/12/test1.mp4");
        wlPlayer.playNext("http://192.168.43.12/18/test1.mp4");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wlPlayer!=null){
            wlPlayer.stop();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                WlTimeInfoBean wlTimeInfoBean = (WlTimeInfoBean) msg.obj;
                tvTime.setText(WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getCurrentTime(), wlTimeInfoBean.getTotalTime())
                        + "/" + WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getTotalTime(), wlTimeInfoBean.getTotalTime()));

                if (!seek && wlTimeInfoBean.getTotalTime() > 0) {
                    seekbar.setProgress(wlTimeInfoBean.getCurrentTime() * 100 / wlTimeInfoBean.getTotalTime());
                }
            }
        }
    };
}
