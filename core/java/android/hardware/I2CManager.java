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

import android.annotation.SystemService;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.IOException;

/**
 * @hide
 */
@SystemService(Context.I2C_SERVICE)
public class I2CManager {
    private static final String TAG = "I2CManager";

    private final Context mContext;
    private final II2CManager mService;

    /**
     * {@hide}
     */
    public I2CManager(Context context, II2CManager service) {
        mContext = context;
        mService = service;
    }

    /**
     * Returns a string array containing the names of available serial ports
     *
     * @return names of available serial ports
     */
    public String[] getI2CDevices() {
        try {
            return mService.getI2CDevices();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Opens and returns the {@link android.hardware.I2CDevice} with the given name.
     *
     * @param name of the I2C device
     * @param addr slace address of I2C device
     * @return the serial port
     */
    public I2CDevice openI2CDevice(String name, int addr) throws IOException {
        try {
            ParcelFileDescriptor pfd = mService.openI2CDevice(name);
            if (pfd != null) {
                I2CDevice port = new I2CDevice(name);
                port.open(pfd, addr);
                return port;
            } else {
                throw new IOException("Could not open I2C device " + name);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
