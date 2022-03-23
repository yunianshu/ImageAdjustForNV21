package com.yunianshu.gpuapplication;

import android.graphics.Bitmap;

public class ImageFilterEngine {

    static {
        System.loadLibrary("ImageFilter");
    }

    public native static void nv21ToRGBA(byte[] nv21, byte[] rgba, int w, int h);

    public native static void applyBitmap(Bitmap bitmap, byte[] rgba);

    public native static void processBrightness(byte[] nv21, int width, int height, int progress);

    public native static void processSaturation(byte[] nv21, int width, int height, int progress);

    public native static void processContrast(byte[] nv21, int width, int height, int progress);

    public native static void processColorTemperature(byte[] nv21, int width, int height, int progress);

    public native static void processColorTone(byte[] nv21, int width, int height, int progress);
}
