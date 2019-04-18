#ifndef MYMUSIC_WLVIDEO_H
#define MYMUSIC_WLVIDEO_H

#include "WlQueue.h"
#include "WlCallJava.h"
#include "WlAudio.h"

#define CODEC_YUV 0
#define CODEC_MEDIACODEC 1

extern "C"
{
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libavcodec/avcodec.h>
#include <libavutil/time.h>
};

class WlVideo {

public:
    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecpar = NULL;
    WlQueue *queue = NULL;
    WlPlaystatus *playstatus = NULL;
    WlCallJava *wlCallJava = NULL;
    AVRational time_base;

    pthread_t thread_play;

    //音视频同步，音频
    WlAudio *audio = NULL;
    double clock = 0;
    double delayTime = 0;
    double defaultDelayTime = 0.04;

    pthread_mutex_t codecMutex;
    int codectype = CODEC_YUV;
    AVBSFContext *abs_ctx = NULL;

public:
    WlVideo(WlPlaystatus *playstatus, WlCallJava *wlCallJava);

    ~WlVideo();

    void play();

    void release();

    //音视频同步， 获取音视频时间差
    double getFrameDiffTime(AVFrame *avFrame, AVPacket *avPacket);

    //获取延迟时间
    double getDelayTime(double diff);
};


#endif //MYMUSIC_WLVIDEO_H
