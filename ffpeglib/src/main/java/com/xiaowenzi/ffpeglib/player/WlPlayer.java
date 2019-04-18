package com.xiaowenzi.ffpeglib.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.view.Surface;

import com.xiaowenzi.ffpeglib.bean.WlTimeInfoBean;
import com.xiaowenzi.ffpeglib.listener.WlOnCompleteListener;
import com.xiaowenzi.ffpeglib.listener.WlOnErrorListener;
import com.xiaowenzi.ffpeglib.listener.WlOnLoadListener;
import com.xiaowenzi.ffpeglib.listener.WlOnParparedListener;
import com.xiaowenzi.ffpeglib.listener.WlOnPauseResumeListener;
import com.xiaowenzi.ffpeglib.listener.WlOnTimeInfoListener;
import com.xiaowenzi.ffpeglib.log.MyLog;
import com.xiaowenzi.ffpeglib.opengl.WlGLSurfaceView;
import com.xiaowenzi.ffpeglib.opengl.WlRender;
import com.xiaowenzi.ffpeglib.util.WlVideoSupportUitl;

import java.nio.ByteBuffer;

/**
 * @author JingWen.Li
 * @// TODO: 2018/7/28
 */

public class WlPlayer {

    static {
        System.loadLibrary("native-lib");

        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
    }

    //数据源
    private String source;
    private static boolean playNext = false;
    private static WlTimeInfoBean wlTimeInfoBean;
    private WlGLSurfaceView wlGLSurfaceView;
    private int duration = 0;
    //硬解码
    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private Surface surface;
    private MediaCodec.BufferInfo info;
    //接口
    private WlOnParparedListener wlOnParparedListener;
    private WlOnLoadListener wlOnLoadListener;
    private WlOnPauseResumeListener wlOnPauseResumeListener;
    private WlOnTimeInfoListener wlOnTimeInfoListener;
    private WlOnErrorListener wlOnErrorListener;
    private WlOnCompleteListener wlOnCompleteListener;

    public WlPlayer() {
    }

    /**
     * 设置数据源
     *
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    public void setWlGLSurfaceView(WlGLSurfaceView wlGLSurfaceView) {
        this.wlGLSurfaceView = wlGLSurfaceView;
        wlGLSurfaceView.getWlRender().setOnSurfaceCreateListener(new WlRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface s) {
                if (surface == null) {
                    surface = s;
                    MyLog.e("onSurfaceCreate");
                }
            }
        });
    }

    /**
     * 设置准备接口回调
     *
     * @param wlOnParparedListener
     */
    public void setWlOnParparedListener(WlOnParparedListener wlOnParparedListener) {
        this.wlOnParparedListener = wlOnParparedListener;
    }

    public void setWlOnLoadListener(WlOnLoadListener wlOnLoadListener) {
        this.wlOnLoadListener = wlOnLoadListener;
    }

    public void setWlOnPauseResumeListener(WlOnPauseResumeListener wlOnPauseResumeListener) {
        this.wlOnPauseResumeListener = wlOnPauseResumeListener;
    }

    public void setWlOnTimeInfoListener(WlOnTimeInfoListener wlOnTimeInfoListener) {
        this.wlOnTimeInfoListener = wlOnTimeInfoListener;
    }

    public void setWlOnErrorListener(WlOnErrorListener wlOnErrorListener) {
        this.wlOnErrorListener = wlOnErrorListener;
    }

    public void setWlOnCompleteListener(WlOnCompleteListener wlOnCompleteListener) {
        this.wlOnCompleteListener = wlOnCompleteListener;
    }

    /**
     * 点击方法
     */
    public void parpared() {
        if (TextUtils.isEmpty(source)) {
            MyLog.e("source not be empty");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_parpared(source);
            }
        }).start();

    }

    public void start() {
        if (TextUtils.isEmpty(source)) {
            MyLog.e("source is empty");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_start();
            }
        }).start();
    }

    public void pause() {
        n_pause();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(true);
        }
    }

    public void resume() {
        n_resume();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(false);
        }
    }

    public void stop() {
        wlTimeInfoBean = null;
        duration = 0;
        releaseMediacodec();
        new Thread(new Runnable() {
            @Override
            public void run() {
                n_stop();
            }
        }).start();
    }

    public void seek(int secds) {
        n_seek(secds);
    }

    public void playNext(String url) {
        source = url;
        playNext = true;
        stop();
    }

    public int getDuration() {
        return duration;
    }

    /**
     * c++回调java的方法
     */
    public void onCallParpared() {
        if (wlOnParparedListener != null) {
            wlOnParparedListener.onParpared();
        }
    }

    public void onCallLoad(boolean load) {
        if (wlOnLoadListener != null) {
            wlOnLoadListener.onLoad(load);
        }
    }

    public void onCallTimeInfo(int currentTime, int totalTime) {
        if (wlOnTimeInfoListener != null) {
            if (wlTimeInfoBean == null) {
                wlTimeInfoBean = new WlTimeInfoBean();
            }
            duration = totalTime;
            wlTimeInfoBean.setCurrentTime(currentTime);
            wlTimeInfoBean.setTotalTime(totalTime);
            wlOnTimeInfoListener.onTimeInfo(wlTimeInfoBean);
        }
    }

    public void onCallError(int code, String msg) {
        if (wlOnErrorListener != null) {
            stop();
            wlOnErrorListener.onError(code, msg);
        }
    }

    public void onCallComplete() {
        if (wlOnCompleteListener != null) {
            stop();
            wlOnCompleteListener.onComplete();
        }
    }

    public void onCallNext() {
        if (playNext) {
            playNext = false;
            parpared();
        }
    }

    public void onCallRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        MyLog.e("获取到视频YUV");
        if (wlGLSurfaceView != null) {
            wlGLSurfaceView.getWlRender().setRenderType(WlRender.RENDER_YUV);
            wlGLSurfaceView.setYUVData(width, height, y, u, v);
        }
    }

    public boolean onCallIsSupportMediaCodec(String ffcodecname) {
        return WlVideoSupportUitl.isSupportCodec(ffcodecname);
    }

    /**
     * 初始化MediaCodec
     *
     * @param codecName
     * @param width
     * @param height
     * @param csd_0
     * @param csd_1
     */
    public void onCallInitMediaCodec(String codecName, int width, int height, byte[] csd_0, byte[] csd_1) {
        if (surface != null) {
            try {
                //设置模式
                wlGLSurfaceView.getWlRender().setRenderType(WlRender.RENDER_MEDIACODEC);

                String mime = WlVideoSupportUitl.findVideoCodecName(codecName);
                //创建视频格式
                mediaFormat = MediaFormat.createVideoFormat(mime, width, height);
                //设置最大的数据类型
                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
                //设置Buffer类型
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd_0));
                mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(csd_1));

                //TODO 打印格式
                MyLog.e(mediaFormat.toString());

                //创建解码器
                mediaCodec = MediaCodec.createDecoderByType(mime);
                info = new MediaCodec.BufferInfo();
                mediaCodec.configure(mediaFormat, surface, null, 0);
                mediaCodec.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (wlOnErrorListener != null) {
                wlOnErrorListener.onError(2001, "surface is null");
            }
        }
    }

    /**
     * 解析AVPacket数据
     *
     * @param datasize
     * @param data
     */
    public void onCallDecodeAVPacket(int datasize, byte[] data) {
        if (surface != null && datasize > 0 && data != null && mediaCodec != null) {

            try{
                int intputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                if (intputBufferIndex >= 0) {
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffers()[intputBufferIndex];
                    byteBuffer.clear();
                    byteBuffer.put(data);
                    mediaCodec.queueInputBuffer(intputBufferIndex, 0, datasize, 0, 0);
                    //MyLog.e("1111111111111");
                }
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 0);
                while (outputBufferIndex >= 0) {
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 0);
                    //MyLog.e("2222222222222");
                }
            }catch (Exception e){
                //e.printStackTrace();
            }
        }
    }

    public void releaseMediacodec() {
        if (mediaCodec != null) {
            try {
                mediaCodec.flush();
                mediaCodec.stop();
                mediaCodec.release();
            }catch (Exception e){
                //e.printStackTrace();
            }

            mediaCodec = null;
            mediaFormat = null;
            info = null;
        }
    }


    //本地方法
    public native void n_parpared(String source);

    public native void n_start();

    private native void n_pause();

    private native void n_resume();

    private native void n_stop();

    private native void n_seek(int secds);
}
