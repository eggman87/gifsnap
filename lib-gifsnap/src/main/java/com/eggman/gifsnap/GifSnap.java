package com.eggman.gifsnap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Created by mharris on 3/4/15.
 *
 */
public class GifSnap {

    private View viewToScreenshot;
    private static final String TAG = "GifSnap";

    public GifSnap(View viewToScreenshot) {
        this.viewToScreenshot = viewToScreenshot;
    }

    public void recordGif(String gifName, int numberOfFrames) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        Log.d(TAG, "starting gif creation process for gif with name: " + gifName + " at " +startTime + "ms");

        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setRepeat(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoder.start(outputStream);

        for (int frame=0; frame < numberOfFrames; frame++) {
            Log.d(TAG, "[Screenshot] processing frame number " + frame);
            String path = getImagePathForFrame(frame);
            takeScreenShot(viewToScreenshot, path);
        }

        Log.d(TAG, "finished processing all frames, starting gif encoder");

        for (int frame=0; frame <numberOfFrames; frame++) {
            Log.d(TAG, "[GifEncoder] processing frame number " + frame);
            String imagePath = getImagePathForFrame(frame);
            File imageToAdd = new File(imagePath);
            Bitmap bitmapToAdd = BitmapFactory.decodeFile(imageToAdd.getAbsolutePath());
            encoder.addFrame(bitmapToAdd);
        }
        encoder.finish();

        Log.d(TAG, "finished processing gif encoding, saving gif to disk.");

        String gifPath = getGifPathForName(gifName);
        FileOutputStream gifOutput;

        try {
            gifOutput = new FileOutputStream(gifPath);
            gifOutput.write(outputStream.toByteArray());
            gifOutput.flush();
            gifOutput.close();
        } catch (IOException e) {
            Log.e(TAG, "error while saving gif to disk", e);
        }

        long endTime = Calendar.getInstance().getTimeInMillis();

        Log.d(TAG, "finished creating gif" + gifName);
        Log.d(TAG, "created gif located at " + gifPath);
        Log.d(TAG, "total gif creation time: " + String.valueOf(endTime-startTime) + "ms");

    }


    public void takeScreenShot(View view, String path) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        OutputStream output;
        File imageFile = new File(path);

        try {
            output = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, output);
            output.flush();
            output.close();

        } catch (IOException e) {
            Log.e(TAG, "error while trying to capture snapshot of view", e);
        }
    }

    private String getImagePathForFrame(int frame) {
        return Environment.getExternalStorageDirectory().toString() + "/" + String.valueOf(frame) + ".png";
    }

    private String getGifPathForName(String gifName) {
       return  Environment.getExternalStorageDirectory().toString() + "/" + gifName + ".gif";
    }
}
