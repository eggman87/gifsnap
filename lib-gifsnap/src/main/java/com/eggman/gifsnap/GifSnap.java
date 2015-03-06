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

    private String storageDirectory;

    /**
     * Initializes the GifSnap
     *
     * Default storage location is in external storage directory.
     *
     * @param viewToScreenshot the view you wish to create a gif of.
     */
    public GifSnap(View viewToScreenshot) {
        this.viewToScreenshot = viewToScreenshot;
    }

    /**
     * Initializes the GifSnap
     * @param viewToScreenshot the view you wish to create a gif of.
     * @param storageDirectoryPath the directory you wish to store the .gif.
     */
    public GifSnap(View viewToScreenshot, String storageDirectoryPath) {
        this.viewToScreenshot = viewToScreenshot;
        this.storageDirectory = storageDirectoryPath;
    }

    /**
     * Records a gif of the view supplied into the constructor.
     * @param gifName the name of the gif you would like to create (will overwrite if already exists).
     * @param numberOfFrames the number of frames to record.
     * @return the full file path of the gif.
     */
    public String recordGif(String gifName, int numberOfFrames) {
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

        return gifPath;
    }

    private void takeScreenShot(View view, String path) {
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
        return getStorageDirectory()  + "/" + String.valueOf(frame) + ".png";
    }

    private String getGifPathForName(String gifName) {
        return   getStorageDirectory() +  "/" + gifName + ".gif";
    }

    private String getStorageDirectory() {
        return storageDirectory == null ? Environment.getExternalStorageDirectory().toString():storageDirectory;
    }
}
