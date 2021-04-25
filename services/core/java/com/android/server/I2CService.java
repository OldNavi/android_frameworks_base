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
 * See the License for the specific language governing permissions an
 * limitations under the License.
 */

package com.android.server;

import android.content.Context;
import android.hardware.II2CManager;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.util.ArrayList;

public class I2CService extends II2CManager.Stub {

    private final Context mContext;
    private final String[] mI2CPorts;

    public I2CService(Context context) {
        mContext = context;
        mI2CPorts = context.getResources().getStringArray(
                com.android.internal.R.array.config_I2CPorts);
    }

    public String[] getI2CDevices() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.I2C_PORT, null);

        ArrayList<String> ports = new ArrayList<String>();
        for (int i = 0; i < mI2CPorts.length; i++) {
            String path = mI2CPorts[i];
            if (new File(path).exists()) {
                ports.add(path);
            }
        }
        String[] result = new String[ports.size()];
        ports.toArray(result);
        return result;
    }

    public ParcelFileDescriptor openI2CDevice(String path) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.I2C_PORT, null);
        for (int i = 0; i < mI2CPorts.length; i++) {
            if (mI2CPorts[i].equals(path)) {
                return native_open(path);
            }
        }
        throw new IllegalArgumentException("Invalid I2C port " + path);
    }

    private native ParcelFileDescriptor native_open(String path);
}
