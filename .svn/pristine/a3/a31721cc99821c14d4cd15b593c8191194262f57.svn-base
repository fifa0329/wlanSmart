#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include "rc4.h"
#define LOG_TAG "WLANSelector"

static unsigned short index_size;
static unsigned short *index_data;
static unsigned short gsm_size;
static unsigned short *gsm_data;
static unsigned short td_size;
static unsigned short *td_data;

static jboolean quickSearchData(unsigned short *data, unsigned short size, int ci) {
    int low = 0;
    int high = size - 1;
    while (low <= high) {
        int mid = (low + high) / 2;
        if (data[mid] < ci) {
            low = mid + 1;
        } else if (data[mid] > ci) {
            high = mid - 1;
        } else {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

static int quickSearchIndex(int lac) {
    int low = 0;
    int high = index_size - 1;
    while (low <= high) {
        int mid = (low + high) / 2;
        if (index_data[mid * 3 + 1] < lac) {
            low = mid + 1;
        } else if (index_data[mid * 3] > lac) {
            high = mid - 1;
        } else {
            return index_data[mid * 3 + 2];
        }
    }
    
    return 0;
}

static void prepare_rc4_key(JNIEnv *env, jobject obj, rc4_key *pkey) {
    jclass classMethodAccess = (*env)->FindClass(env, "java/lang/Class");
    jmethodID getNameMethod = (*env)->GetMethodID(env, classMethodAccess, "getName", "()Ljava/lang/String;");
    jstring nameString = (*env)->CallObjectMethod(env, (*env)->GetObjectClass(env, obj), getNameMethod);
    const jbyte *name = (*env)->GetStringUTFChars(env, nameString, JNI_FALSE);
    prepare_key((unsigned char*)name, strlen(name), pkey);
    (*env)->ReleaseStringUTFChars(env, nameString, name);
}

/*
 * Class:     com_enice_wlan_selector_trigger_CellTrigger
 * Method:    getCityIndex
 * Signature: (ZI)I
 */
JNIEXPORT jint JNICALL Java_com_enice_wlan_selector_trigger_CellTrigger_getCityIndex
  (JNIEnv *env, jobject obj, jboolean isTD, jint lac) {
    static int first_time = 1;
    if (first_time) {
        first_time = 0;
        
        rc4_key real_key;
        prepare_rc4_key(env, obj, &real_key);
        
        FILE* file = fopen("/sdcard/WLANSelector/index.dat", "rb");
        fread(&index_size, 2, 1, file);
        rc4((unsigned char*)&index_size, 2, &real_key);
        index_data = malloc(index_size * 2 * 3);
        fread(index_data, 2 * 3, index_size, file);
        rc4((unsigned char*)index_data, index_size * 2 * 3, &real_key);
        fclose(file);
    }
    
    int result = quickSearchIndex(lac);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "getCityIndex: lac %d, result %d\n", lac, result);
    return result;
}

/*
 * Class:     com_enice_wlan_selector_trigger_CellTrigger
 * Method:    loadCityData
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_enice_wlan_selector_trigger_CellTrigger_loadCityData
  (JNIEnv *env, jobject obj, jstring cityData) {
    rc4_key real_key;
    prepare_rc4_key(env, obj, &real_key);
    
    const jbyte *city_data_path = (*env)->GetStringUTFChars(env, cityData, JNI_FALSE);
    FILE* file = fopen(city_data_path, "rb");
    (*env)->ReleaseStringUTFChars(env, cityData, city_data_path);
    
    fread(&gsm_size, 2, 1, file);
    rc4((unsigned char*)&gsm_size, 2, &real_key);
    gsm_data = malloc(gsm_size * 2);
    fread(gsm_data, 2, gsm_size, file);
    rc4((unsigned char*)gsm_data, gsm_size * 2, &real_key);
    
    fread(&td_size, 2, 1, file);
    rc4((unsigned char*)&td_size, 2, &real_key);
    td_data = malloc(td_size * 2);
    fread(td_data, 2, td_size, file);
    rc4((unsigned char*)td_data, td_size * 2, &real_key);
    
    fclose(file);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "loadCityData: gsm %d, td %d\n", gsm_size ,td_size);
}

/*
 * Class:     com_enice_wlan_selector_trigger_CellTrigger
 * Method:    wifiCovered
 * Signature: (ZII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_enice_wlan_selector_trigger_CellTrigger_wifiCovered
  (JNIEnv *env, jobject obj, jboolean isTD, jint lac, jint cellId) {
    jboolean result = quickSearchData(isTD ? td_data : gsm_data, isTD ? td_size : gsm_size, cellId);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "wifiCovered: isTD %d, ci %d, result %d\n", isTD, cellId, result);
    return result;
}