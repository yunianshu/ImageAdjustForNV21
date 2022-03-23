package com.yunianshu.gpuapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private byte[] mNV21Buf;
    private byte[] mPreviewNV21Buf;

    private int mBmpW;
    private int mBmpH;

    private Bitmap mSrcBitmap;
    private Bitmap mDstBitmap;

    private ImageView mSrcImageView;
    private ImageView mDstImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }

        load();

        mSrcImageView = (ImageView) findViewById(R.id.iv_src);
        mSrcImageView.setImageBitmap(mSrcBitmap);
        mDstImageView = (ImageView) findViewById(R.id.iv_dst);
        mDstImageView.setImageBitmap(mSrcBitmap);

        ((SeekBar) findViewById(R.id.contract_sb)).setOnSeekBarChangeListener(mSeekBarListener);
        ((SeekBar) findViewById(R.id.contract_sb)).setMax(200);
        ((SeekBar) findViewById(R.id.contract_sb)).setProgress(100);

        ((SeekBar) findViewById(R.id.saturation_sb)).setOnSeekBarChangeListener(mSeekBarListener);
        ((SeekBar) findViewById(R.id.saturation_sb)).setMax(300);
        ((SeekBar) findViewById(R.id.saturation_sb)).setProgress(100);

        ((SeekBar) findViewById(R.id.bright_sb)).setOnSeekBarChangeListener(mSeekBarListener);
        ((SeekBar) findViewById(R.id.bright_sb)).setMax(200);
        ((SeekBar) findViewById(R.id.bright_sb)).setProgress(100);

        ((SeekBar) findViewById(R.id.temperature_sb)).setOnSeekBarChangeListener(mSeekBarListener);
        ((SeekBar) findViewById(R.id.temperature_sb)).setMax(200);
        ((SeekBar) findViewById(R.id.temperature_sb)).setProgress(100);

        ((SeekBar) findViewById(R.id.colorTone_sb)).setOnSeekBarChangeListener(mSeekBarListener);
        ((SeekBar) findViewById(R.id.colorTone_sb)).setMax(200);
        ((SeekBar) findViewById(R.id.colorTone_sb)).setProgress(100);
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == R.id.contract_sb) {
                contrastProgress = progress - 100;
                Log.i("test", "onProgressChanged contrastProgress=" + contrastProgress);
                System.arraycopy(mNV21Buf, 0, mPreviewNV21Buf, 0, mNV21Buf.length);
                ImageFilterEngine.processContrast(mPreviewNV21Buf, mBmpW, mBmpH, contrastProgress);

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(mPreviewNV21Buf, ImageFormat.NV21, mBmpW, mBmpH, null);
                yuvImage.compressToJpeg(new Rect(0, 0, mBmpW, mBmpH), 100, bao);

                byte[] buf = bao.toByteArray();
                Bitmap dstBmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                mDstImageView.setImageBitmap(dstBmp);
            } else if (seekBar.getId() == R.id.saturation_sb) {
                saturationProgress = progress;
                Log.i("test", "onProgressChanged saturationProgress=" + saturationProgress);
                System.arraycopy(mNV21Buf, 0, mPreviewNV21Buf, 0, mNV21Buf.length);
                ImageFilterEngine.processSaturation(mPreviewNV21Buf, mBmpW, mBmpH, saturationProgress);

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(mPreviewNV21Buf, ImageFormat.NV21, mBmpW, mBmpH, null);
                yuvImage.compressToJpeg(new Rect(0, 0, mBmpW, mBmpH), 100, bao);

                byte[] buf = bao.toByteArray();
                Bitmap dstBmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                mDstImageView.setImageBitmap(dstBmp);
            } else if (seekBar.getId() == R.id.bright_sb) {
                brightProgress = progress - 100;
                Log.i("test", "onProgressChanged brightProgress=" + brightProgress);
                System.arraycopy(mNV21Buf, 0, mPreviewNV21Buf, 0, mNV21Buf.length);
                ImageFilterEngine.processBrightness(mPreviewNV21Buf, mBmpW, mBmpH, brightProgress);

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(mPreviewNV21Buf, ImageFormat.NV21, mBmpW, mBmpH, null);
                yuvImage.compressToJpeg(new Rect(0, 0, mBmpW, mBmpH), 100, bao);

                byte[] buf = bao.toByteArray();
                Bitmap dstBmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                mDstImageView.setImageBitmap(dstBmp);
            } else if (seekBar.getId() == R.id.temperature_sb) {
                temperatureProgress = progress - 100;
                Log.i("test", "onProgressChanged temperatureProgress=" + temperatureProgress);
                System.arraycopy(mNV21Buf, 0, mPreviewNV21Buf, 0, mNV21Buf.length);
                ImageFilterEngine.processColorTemperature(mPreviewNV21Buf, mBmpW, mBmpH, temperatureProgress);

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(mPreviewNV21Buf, ImageFormat.NV21, mBmpW, mBmpH, null);
                yuvImage.compressToJpeg(new Rect(0, 0, mBmpW, mBmpH), 100, bao);

                byte[] buf = bao.toByteArray();
                Bitmap dstBmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                mDstImageView.setImageBitmap(dstBmp);
            } else if (seekBar.getId() == R.id.colorTone_sb) {
                colorToneProgress = progress - 100;
                Log.i("test", "onProgressChanged colorToneProgress=" + colorToneProgress);
                System.arraycopy(mNV21Buf, 0, mPreviewNV21Buf, 0, mNV21Buf.length);
                ImageFilterEngine.processColorTone(mPreviewNV21Buf, mBmpW, mBmpH, colorToneProgress);

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(mPreviewNV21Buf, ImageFormat.NV21, mBmpW, mBmpH, null);
                yuvImage.compressToJpeg(new Rect(0, 0, mBmpW, mBmpH), 100, bao);

                byte[] buf = bao.toByteArray();
                Bitmap dstBmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
                mDstImageView.setImageBitmap(dstBmp);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("test", "onRequestPermissionsResult");
    }

    private void load() {
        try {
            InputStream is = getApplicationContext().getAssets().open("test.jpg");
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            mSrcBitmap = BitmapFactory.decodeStream(is, null, op);
            mBmpW = mSrcBitmap.getWidth();
            mBmpH = mSrcBitmap.getHeight();
            Log.i("test", " load() image size:" + mBmpW + "x" + mBmpH);

            int nv21Len = mBmpW * mBmpH * 3 >> 1;
            mNV21Buf = new byte[nv21Len];
            InputStream nvIs = getApplicationContext().getAssets().open("testNV21.nv21");
            int nvLen = nvIs.available();
            Log.i("test", " load() nv21Len:" + nv21Len + " nvLen:" + nvLen);
            int read = nvIs.read(mNV21Buf);
            nvIs.close();
            Log.i("test", " load() nv21 read byte:" + read);
            mPreviewNV21Buf = new byte[nv21Len];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int contrastProgress = 0;
    private int brightProgress = 0;
    private int saturationProgress = 0;
    private int temperatureProgress = 0;
    private int colorToneProgress = 0;
}
