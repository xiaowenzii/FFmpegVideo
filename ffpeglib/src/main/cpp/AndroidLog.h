#ifndef MYMUSIC_ANDROIDLOG_H
#define MYMUSIC_ANDROIDLOG_H

#endif //MYMUSIC_ANDROIDLOG_H

#include "android/log.h"

#define LOG_DEBUG true

#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"xiaowenzi",FORMAT,##__VA_ARGS__);
