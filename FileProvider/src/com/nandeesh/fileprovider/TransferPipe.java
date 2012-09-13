
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
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.URL;

/**
 * Helper for transferring data through a pipe from a client app.
 */
class TransferPipe implements Runnable {
    static final String TAG = "TransferPipe";
    static final boolean DEBUG = true;

    final Thread mThread;;
    final ParcelFileDescriptor[] mFds;

    ParcelFileDescriptor mInFd;
    long mEndTime;
    String mFailure;
    boolean mComplete;

    TransferPipe() throws IOException {
        mThread = new Thread(this, "TransferPipe");
        mFds = ParcelFileDescriptor.createPipe();
    }

    ParcelFileDescriptor getReadFd() {
        return mFds[0];
    }

    ParcelFileDescriptor getWriteFd() {
        return mFds[1];
    }


    void go(ParcelFileDescriptor in) throws IOException {
            synchronized (this) {
                mInFd = in;
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
        try {
            mInFd.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        final byte[] buffer = new byte[1024];

        final AutoCloseInputStream fis = new AutoCloseInputStream(mInFd);
        final AutoCloseOutputStream fos = new AutoCloseOutputStream(getWriteFd());

        if (DEBUG)
            Log.i(TAG, "Ready to write pipe...");

        int size;
        try {
            while ((size = fis.read(buffer)) > 0) {
                if (DEBUG)
                    Log.i(TAG, "write " + size + " bytes");

                fos.write(buffer, 0, size);

            }

        } catch (IOException e) {
            synchronized (this) {
                e.printStackTrace();
                mFailure = e.toString();
                notifyAll();
                return;
            }
        } finally {
            kill();
        }

        synchronized (this) {
            mComplete = true;
            notifyAll();
        }
    }
}
