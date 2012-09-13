
package com.nandeesh.fileprovider;

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

import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper for transferring data through a pipe
 */
class NetTransferPipe implements Runnable {
    static final String TAG = "NetTransferPipe";
    static final boolean DEBUG = true;

    final Thread mThread;;
    final ParcelFileDescriptor[] mFds;

    URL inURL;

    NetTransferPipe() throws IOException {
        mThread = new Thread(this, "TransferPipe");
        mFds = ParcelFileDescriptor.createPipe();
    }

    ParcelFileDescriptor getReadFd() {
        return mFds[0];
    }

    ParcelFileDescriptor getWriteFd() {
        return mFds[1];
    }

    void go(URL in) throws IOException {
        synchronized (this) {
            inURL = in;
            mThread.start();
        }
    }

    void closeFd(int num) {
        if (mFds[num] != null) {

            try {
                mFds[num].close();
            } catch (IOException e) {
            }
            mFds[num] = null;
        }
    }

    void kill() {
        closeFd(1);
    }

    
    public void run() {
        final byte[] buffer = new byte[1024];
        InputStream is = null;
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) inURL.openConnection();

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            final AutoCloseOutputStream fos = new AutoCloseOutputStream(getWriteFd());

            int size;

            while ((size = is.read(buffer)) > 0) {
                if (DEBUG)
                    Log.i(TAG, "write " + size + " bytes");

                fos.write(buffer, 0, size);

            }

        } catch (IOException e) {
            synchronized (this) {
                e.printStackTrace();
                notifyAll();
                return;
            }
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {}
            kill();
        }

        synchronized (this) {
            notifyAll();
        }
    }
}
