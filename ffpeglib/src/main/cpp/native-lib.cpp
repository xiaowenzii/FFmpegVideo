#include <jni.h>
#include <string>
#include "WlFFmpeg.h"
#include "WlPlaystatus.h"

extern "C"
{
#include <libavformat/avformat.h>
}

/*extern "C"
JNIEXPORT jstring
JNICALL
Java_com_xiaowenzi_ffpeglib_Test_stringFromJNI(
        JNIEnv *env,
        jobject) {
    std::string hello = "Lib C++: Hello";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_Test_testFFmpeg(JNIEnv *env, jobject instance) {
    av_register_all();
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                LOGD("[Video]:%s", c_temp->name);
                break;
            case AVMEDIA_TYPE_AUDIO:
                LOGD("[Audio]:%s", c_temp->name);
                break;
            default:
                LOGD("[Other]:%s", c_temp->name);
                break;
        }
        c_temp = c_temp->next;
    }
}*/

_JavaVM *javaVM = NULL;
WlCallJava *callJava = NULL;
WlFFmpeg *fFmpeg = NULL;
WlPlaystatus *playstatus = NULL;
bool nexit = true;
pthread_t thread_start;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {

        return result;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_player_WlPlayer_n_1parpared(JNIEnv *env, jobject instance,
                                                        jstring source_) {
    const char *source = env->GetStringUTFChars(source_, 0);

    if (fFmpeg == NULL) {
        if (callJava == NULL) {
            callJava = new WlCallJava(javaVM, env, &instance);
        }
        callJava->onCallLoad(MAIN_THREAD, true);
        playstatus = new WlPlaystatus();
        fFmpeg = new WlFFmpeg(playstatus, callJava, source);
        fFmpeg->parpared();
    }
}

void *startCallBack(void *data) {
    WlFFmpeg *fFmpeg = (WlFFmpeg *) data;
    fFmpeg->start();
    //pthread_exit(&thread_start);
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_player_WlPlayer_n_1start(JNIEnv *env, jobject instance) {
    if (fFmpeg != NULL) {
        pthread_create(&thread_start, NULL, startCallBack, fFmpeg);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_player_WlPlayer_n_1pause(JNIEnv *env, jobject instance) {
    if (fFmpeg != NULL) {
        fFmpeg->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_player_WlPlayer_n_1resume(JNIEnv *env, jobject instance) {
    if (fFmpeg != NULL) {
        fFmpeg->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_player_WlPlayer_n_1stop(JNIEnv *env, jobject instance) {
    if (!nexit) {
        return;
    }
    //播放下一首
    jclass jlz = env->GetObjectClass(instance);
    jmethodID jmid_next = env->GetMethodID(jlz, "onCallNext", "()V");

    nexit = false;
    if (fFmpeg != NULL) {
        fFmpeg->release();
        pthread_join(thread_start, NULL);
        delete (fFmpeg);
        fFmpeg = NULL;
        if (callJava != NULL) {
            delete (callJava);
            callJava = NULL;
        }
        if (playstatus != NULL) {
            delete (playstatus);
            playstatus = NULL;
        }
    }
    nexit = true;
    env->CallVoidMethod(instance, jmid_next);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xiaowenzi_ffpeglib_player_WlPlayer_n_1seek(JNIEnv *env, jobject instance, jint secds) {
    if (fFmpeg != NULL) {
        fFmpeg->seek(secds);
    }
}