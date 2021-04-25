/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "I2CDeviceJNI"

#include "utils/Log.h"

#include "jni.h"
#include <nativehelper/JNIHelp.h>
#include "core_jni_helpers.h"

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/i2c-dev.h>

using namespace android;

static jfieldID field_context;

static void
android_hardware_I2CDevice_open(JNIEnv *env, jobject thiz, jobject fileDescriptor, jint slave_addr)
{
     int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    // duplicate the file descriptor, since ParcelFileDescriptor will eventually close its copy
    fd = dup(fd);
    if (fd < 0) {
        jniThrowException(env, "java/io/IOException", "Could not open I2C bus");
        return;
    }
    env->SetIntField(thiz, field_context, fd);
    if (ioctl(fd,  I2C_SLAVE, slave_addr) < 0)
        jniThrowException(env, "java/io/IOException", "Could not open set I2C Slave address");
}

static void
android_hardware_I2CDevice_close(JNIEnv *env, jobject thiz)
{
    int fd = env->GetIntField(thiz, field_context);
    close(fd);
    env->SetIntField(thiz, field_context, -1);
}

static jint
android_hardware_I2CDevice_read_array(JNIEnv *env, jobject thiz, jbyteArray buffer, jint length)
{
    int fd = env->GetIntField(thiz, field_context);
    jbyte* buf = (jbyte *)malloc(length);
    if (!buf) {
        jniThrowException(env, "java/lang/OutOfMemoryError", NULL);
        return -1;
    }

    int ret = read(fd, buf, length);
    if (ret > 0) {
        // copy data from native buffer to Java buffer
        env->SetByteArrayRegion(buffer, 0, ret, buf);
    }

    free(buf);
    if (ret < 0)
        jniThrowException(env, "java/io/IOException", NULL);
    return ret;
}

static jint
android_hardware_I2CDevice_read_direct(JNIEnv *env, jobject thiz, jobject buffer, jint length)
{
    int fd = env->GetIntField(thiz, field_context);

    jbyte* buf = (jbyte *)env->GetDirectBufferAddress(buffer);
    if (!buf) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "ByteBuffer not direct");
        return -1;
    }

    int ret = read(fd, buf, length);
    if (ret < 0)
        jniThrowException(env, "java/io/IOException", NULL);
    return ret;
}

static void
android_hardware_I2CDevice_write_array(JNIEnv *env, jobject thiz, jbyteArray buffer, jint length)
{
    int fd = env->GetIntField(thiz, field_context);
    jbyte* buf = (jbyte *)malloc(length);
    if (!buf) {
        jniThrowException(env, "java/lang/OutOfMemoryError", NULL);
        return;
    }
    env->GetByteArrayRegion(buffer, 0, length, buf);

    jint ret = write(fd, buf, length);
    free(buf);
    if (ret < 0)
        jniThrowException(env, "java/io/IOException", NULL);
}

static void
android_hardware_I2CDevice_write_direct(JNIEnv *env, jobject thiz, jobject buffer, jint length)
{
    int fd = env->GetIntField(thiz, field_context);

    jbyte* buf = (jbyte *)env->GetDirectBufferAddress(buffer);
    if (!buf) {
        jniThrowException(env, "java/lang/IllegalArgumentException", "ByteBuffer not direct");
        return;
    }
    int ret = write(fd, buf, length);
    if (ret < 0)
        jniThrowException(env, "java/io/IOException", NULL);
}


static const JNINativeMethod method_table[] = {
    {"native_open",             "(Ljava/io/FileDescriptor;I)V",
                                        (void *)android_hardware_I2CDevice_open},
    {"native_close",            "()V",  (void *)android_hardware_I2CDevice_close},
    {"native_read_array",       "([BI)I",
                                        (void *)android_hardware_I2CDevice_read_array},
    {"native_read_direct",      "(Ljava/nio/ByteBuffer;I)I",
                                        (void *)android_hardware_I2CDevice_read_direct},
    {"native_write_array",      "([BI)V",
                                        (void *)android_hardware_I2CDevice_write_array},
    {"native_write_direct",     "(Ljava/nio/ByteBuffer;I)V",
                                        (void *)android_hardware_I2CDevice_write_direct},
};

int register_android_hardware_I2CDevice(JNIEnv *env)
{
    jclass clazz = FindClassOrDie(env, "android/hardware/I2CDevice");
    field_context = GetFieldIDOrDie(env, clazz, "mNativeContext", "I");

    return RegisterMethodsOrDie(env, "android/hardware/I2CDevice",
            method_table, NELEM(method_table));
}
