
package com.nandeesh.fileprovider;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class FileProvider extends ContentProvider {

    //Make this false to pick from local file
    private static final boolean NETWORK = false;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @TargetApi(9)
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        ParcelFileDescriptor in = getSourceFd(uri, mode);

        try {
            if (NETWORK) {
                URL url = new URL("http://107.108.57.41/logs/wallpaper_01.jpg");
                NetTransferPipe tp = new NetTransferPipe();
                tp.go(url);
                return tp.getReadFd();
            } else {
                TransferPipe tp = null;
                tp = new TransferPipe();
                tp.go(in);
                return tp.getReadFd();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public ParcelFileDescriptor getSourceFd(Uri uri, String mode) throws FileNotFoundException {

        ParcelFileDescriptor pfd = null;
        File file = getContext().getFileStreamPath("Screenshot.png");

        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            e.printStackTrace();
            return super.openFile(uri, mode);
        }
        return pfd;
    }

}
