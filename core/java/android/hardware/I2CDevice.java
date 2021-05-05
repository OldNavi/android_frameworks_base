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

package android.hardware;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;

import java.nio.ByteBuffer;

/**
 * @hide
 */
public class I2CDevice {

    private static final String TAG = "I2CDevice";

    // used by the JNI code
    private int mNativeContext;
    private int mAddr;
    private final String mName;
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * I2CDevice should only be instantiated by SerialManager
     * @hide
     */
    public I2CDevice(String name) {
        mName = name;
    }

    /**
     * I2CDevice should only be instantiated by I2CManager
     *
     * @hide
     */
    public void open(ParcelFileDescriptor pfd, int addr) throws IOException {
        native_open(pfd.getFileDescriptor(), addr);
        mFileDescriptor = pfd;
    }

    /**
     * Closes the serial port
     */
    public void close() throws IOException {
        if (mFileDescriptor != null) {
            mFileDescriptor.close();
            mFileDescriptor = null;
        }
        native_close();
    }

    /**
     * Returns the name of the serial port
     *
     * @return the serial port's name
     */
    public String getName() {
        return mName;
    }

    /**
     * Reads data into the provided buffer.
     * Note that the value returned by {@link java.nio.Buffer#position()} on this buffer is
     * unchanged after a call to this method.
     *
     * @param buffer to read into
     * @return number of bytes read
     */
    public int read(ByteBuffer buffer) throws IOException {
        if (buffer.isDirect()) {
            return native_read_direct(buffer, buffer.remaining());
        } else if (buffer.hasArray()) {
            return native_read_array(buffer.array(), buffer.remaining());
        } else {
            throw new IllegalArgumentException("buffer is not direct and has no array");
        }
    }

    /**
     * Writes data from provided buffer.
     * Note that the value returned by {@link java.nio.Buffer#position()} on this buffer is
     * unchanged after a call to this method.
     *
     * @param buffer to write
     * @param length number of bytes to write
     */
    public void write(ByteBuffer buffer, int length) throws IOException {
        if (buffer.isDirect()) {
            native_write_direct(buffer, length);
        } else if (buffer.hasArray()) {
            native_write_array(buffer.array(), length);
        } else {
            throw new IllegalArgumentException("buffer is not direct and has no array");
        }
    }
    /**
     * Writes and reads data from provided buffers in single I2C transactio.
     * Note that the value returned by {@link java.nio.Buffer#position()} on this buffer is
     * unchanged after a call to this method.
     *
     * @param bufferIn to write
     * @param lengthIn number of bytes to write
     * @param bufferOut to read
     * @param lengthOut number of bytes to read
     */
    public void writeRead(ByteBuffer bufferIn, int lengthIn,ByteBuffer bufferOut, int lengthOut) throws IOException {
        if (bufferIn.isDirect() && bufferOut.isDirect()) {
            native_write_read_direct(bufferIn, lengthIn, bufferOut, lengthOut);
        } else {
            throw new IllegalArgumentException("buffers are not direct");
        }
    }

    public void setTimeout(int timeout) throws IOException {
        native_set_timeout(timeout);
    }

    public void setRetries(int retries) throws IOException  {
        native_set_retries(retries);
    }

    private native void native_open(FileDescriptor pfd, int addr) throws IOException;
    private native void native_close();
    private native int native_read_array(byte[] buffer, int length) throws IOException;
    private native int native_read_direct(ByteBuffer buffer, int length) throws IOException;
    private native void native_write_array(byte[] buffer, int length) throws IOException;
    private native void native_write_direct(ByteBuffer buffer, int length) throws IOException;
    private native void native_set_timeout(int timeout) throws IOException; // Timeout in ms * 10
    private native void native_set_retries(int retries) throws IOException; // number of retries
    private native void native_write_read_direct(ByteBuffer bufferIn, int lengthIn,ByteBuffer bufferOut, int lengthOut) throws IOException;

}
