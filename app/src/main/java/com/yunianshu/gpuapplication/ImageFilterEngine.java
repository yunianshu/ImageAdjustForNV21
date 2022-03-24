package com.yunianshu.gpuapplication;


public class ImageFilterEngine {

    static {
        System.loadLibrary("ImageFilter");
    }

    public native static void processBrightness(byte[] nv21, int width, int height, int progress);

    public native static void processSaturation(byte[] nv21, int width, int height, int progress);

    public native static void processContrast(byte[] nv21, int width, int height, int progress);

    public native static void processColorTemperature(byte[] nv21, int width, int height, int progress);

    public native static void processColorTone(byte[] nv21, int width, int height, int progress);
}
