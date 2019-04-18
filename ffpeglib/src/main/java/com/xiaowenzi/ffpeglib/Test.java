package com.xiaowenzi.ffpeglib;

import android.support.v7.app.AppCompatActivity;

public class Test extends AppCompatActivity {

    // 加载“本地LIB”库
    static {
        System.loadLibrary("native-lib");

        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("postproc-54");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
    }

    /**
     * 本地方法，调用C++函数
     */
    //public native String stringFromJNI();

    /**
     * 测试FFmpeg加载
     */
    //public native void testFFmpeg();
}
