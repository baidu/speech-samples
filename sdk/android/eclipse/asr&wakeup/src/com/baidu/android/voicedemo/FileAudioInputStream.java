package com.baidu.android.voicedemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileAudioInputStream extends InputStream {

    FileInputStream in;

    public static final float SPEED = 6;

    FileAudioInputStream(String file) throws FileNotFoundException {
        in = new FileInputStream(file);
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    long firstRead = System.currentTimeMillis();
    long returnCount;

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        long limit = (long) ((System.currentTimeMillis() - firstRead) * 32 * SPEED);
        long count = Math.min((limit - returnCount), byteCount);
        if (count <= 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            int r = in.read(buffer, byteOffset, (int) count);
            if (r >= 0) {
                returnCount += r;
            }
            return r;
        }
    }

    @Override
    public void close() throws IOException {
        if (null != in) {
            in.close();
        }
    }
}
