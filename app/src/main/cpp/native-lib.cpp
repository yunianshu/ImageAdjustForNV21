#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>
#include <cstring>
#include <cstdlib>
#include <cmath>

#define TAG "ImageFilter"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型

#define max(a,b) ((a)>(b)?(a):(b))
#define min(a,b) ((a)>(b)?(b):(a))

extern "C" JNIEXPORT jstring JNICALL
Java_com_yunianshu_gpuapplication_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


/** Y代表亮度信息，直接调整y的值来改变亮度 */
extern "C"
JNIEXPORT void JNICALL Java_com_yunianshu_gpuapplication_ImageFilterEngine_processBrightness
        (JNIEnv *env, jclass jcls, jbyteArray jnv21Data, jint width, jint height, jint progress){

    LOGI("########## processBrightness start #############\n");

    unsigned char* src_data_ref = (unsigned char*)env->GetByteArrayElements(jnv21Data, 0);
    int yLen = width * height;
    int yValue = 0;
    for(int i = 0; i < yLen; i++) {
        yValue = src_data_ref[i] + progress;
        if (yValue > 255) {
            yValue = 255;
        } else if (yValue < 0) {
            yValue = 0;
        }
        src_data_ref[i] = yValue;
    }
    if (src_data_ref != NULL) {
        env->ReleaseByteArrayElements(jnv21Data, (jbyte*)src_data_ref, 0);
    }

    LOGI("########## processBrightness end #############\n");
}

/** 以128为原点，同比修改uv实现饱和度调整 */
extern "C"
JNIEXPORT void JNICALL Java_com_yunianshu_gpuapplication_ImageFilterEngine_processSaturation
        (JNIEnv *env, jclass jcls, jbyteArray jnv21Data, jint width, jint height, jint progress){

    LOGI("########## processSaturation start #############\n");

    unsigned char* src_data_ref = (unsigned char*)env->GetByteArrayElements(jnv21Data, 0);
    int startIdx = width * height;
    int end = startIdx + (width * height>>1);
    int vuValue = 0;
    float c = progress / 100.0f;//0~2
    LOGI("processSaturation c=%f", c);
    float v1, u1;
    int v, u;
    for(int i = startIdx; i < end; i+=2) {
        v1 = src_data_ref[i] - 128;
        u1 = src_data_ref[i+1] - 128;

        u = (int)(u1*c + 128);
        v = (int)(v1*c + 128);

        u = min(255, max(0, u));
        v = min(255, max(0, v));

        src_data_ref[i] = v;
        src_data_ref[i+1] = u;
    }
    if (src_data_ref != NULL) {
        env->ReleaseByteArrayElements(jnv21Data, (jbyte*)src_data_ref, 0);
    }

    LOGI("########## processSaturation end #############\n");
}

/** 以128为阈值，等比增加或减小Y的值实现对比度的调整 */
extern "C"
JNIEXPORT void JNICALL Java_com_yunianshu_gpuapplication_ImageFilterEngine_processContrast
        (JNIEnv *env, jclass jcls, jbyteArray jnv21Data, jint width, jint height, jint progress){

    LOGI("########## processContrast start #############\n");

    int brightness = 128;
    float contract=(100 + progress)/100.0f;
    unsigned char cTable[256]; //<nipper>
    for(int i=0; i<256; i++){
        cTable[i] = (unsigned char)max(0, min(255,(int)(( i-128)*contract + brightness + 0.5f)));
    }

    unsigned char* src_data_ref = (unsigned char*)env->GetByteArrayElements(jnv21Data, 0);
    int yLen = width * height;
    for(int i = 0; i < yLen; i++) {
        src_data_ref[i] = cTable[src_data_ref[i]];
    }
    if (src_data_ref != NULL) {
        env->ReleaseByteArrayElements(jnv21Data, (jbyte*)src_data_ref, 0);
    }

    LOGI("########## processContrast end #############\n");
}


/**
综合修改uv实现色调的调整，
v' = v*cosQ - u*sinQ,
u' = u*cosQ + v*sinQ.
 */
extern "C"
JNIEXPORT void JNICALL Java_com_yunianshu_gpuapplication_ImageFilterEngine_processColorTone
        (JNIEnv *env, jclass jcls, jbyteArray jnv21Data, jint width, jint height, jint progress){

    LOGI("########## processColorTone start #############\n");

    unsigned char* src_data_ref = (unsigned char*)env->GetByteArrayElements(jnv21Data, 0);
    int startIdx = width * height;
    int end = startIdx + (width * height>>1);
    int vuValue = 0;
    float c = progress / 100.0f;
    float s = sqrt(1 - c*c);
    bool flag = c < 1.0f;
    LOGI("processColorTone c=%f", c);
    float v1, u1;
    int v, u;
    for(int i = startIdx; i < end; i+=2) {
        v1 = src_data_ref[i] - 128;
        u1 = src_data_ref[i+1] - 128;

        u = (int)(u1*s + v1*c) + 128;
        v = (int)(v1*s - u1*c) + 128;

        u = min(255, max(0, u));
        v = min(255, max(0, v));

        src_data_ref[i] = v;
        src_data_ref[i+1] = u;
    }
    if (src_data_ref != NULL) {
        env->ReleaseByteArrayElements(jnv21Data, (jbyte*)src_data_ref, 0);
    }

    LOGI("########## processColorTone end #############\n");
}

/** u:blue, r:red,
 r = r + adjustmentValue
 g = g
 b = b - adjustmentValue.
 https://blog.csdn.net/skyplain1984/article/details/78859802
 */
extern "C"
JNIEXPORT void JNICALL Java_com_yunianshu_gpuapplication_ImageFilterEngine_processColorTemperature
        (JNIEnv *env, jclass jcls, jbyteArray jnv21Data, jint width, jint height, jint progress){

    LOGI("########## processColorTemperature start #############\n");

    unsigned char* src_data_ref = (unsigned char*)env->GetByteArrayElements(jnv21Data, 0);
    int startIdx = width * height;
    int end = startIdx + (width * height>>1);
    int intensity = progress / 5;
    for(int i = startIdx; i < end; i+=2) {
        src_data_ref[i] += intensity;//v
        src_data_ref[i+1] -= intensity;//u

        src_data_ref[i] = min(255, max(0, src_data_ref[i]));
        src_data_ref[i+1] = min(255, max(0, src_data_ref[i+1]));
    }
    if (src_data_ref != NULL) {
        env->ReleaseByteArrayElements(jnv21Data, (jbyte*)src_data_ref, 0);
    }

    LOGI("########## processColorTemperature end #############\n");
}
