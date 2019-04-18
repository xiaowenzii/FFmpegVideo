#ifndef MYMUSIC_WLFFMPEG_H
#define MYMUSIC_WLFFMPEG_H

#include "WlCallJava.h"
#include "pthread.h"
#include "WlAudio.h"
#include "WlPlaystatus.h"

#include "WlVideo.h"

extern "C"
{
#include "libavformat/avformat.h"
#include <libavutil/time.h>
};


class WlFFmpeg {

public:
    WlCallJava *callJava = NULL;
    const char *url = NULL;
    pthread_t decodeThread;
    AVFormatContext *pFormatCtx = NULL;
    WlAudio *audio = NULL;
    WlPlaystatus *playstatus = NULL;
    pthread_mutex_t init_mutex;
    bool exit = false;

    int duration = 0;
    pthread_mutex_t seek_mutex;

    /* 视频 */
    WlVideo *video = NULL;
    bool supportMediacodec = false;

    const AVBitStreamFilter *bsFilter = NULL;

public:
    WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *callJava, const char *url);

    ~WlFFmpeg();

    void parpared();

    void decodeFFmpegThread();

    void start();

    void pause();

    void resume();

    void release();

    void seek(int64_t secds);

    /* 视频 */
    int getCodecContext(AVCodecParameters *codecpar, AVCodecContext **avCodecContext);
};

#endif //MYMUSIC_WLFFMPEG_H
