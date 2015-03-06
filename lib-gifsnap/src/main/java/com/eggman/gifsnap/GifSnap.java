package com.eggman.gifsnap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
 * GifSnap for recording a view and saving it as a gif.
 */
public class GifSnap {

    private View viewToScreenshot;
    private String storageDirectory;

    private static final String TAG = "GifSnap";

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
     */
    public void recordGif(final String gifName, final int numberOfFrames, final OnGifSnapListener listener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final String gifUrl = startRecording(gifName, numberOfFrames);
                //callback with gif url on the main thread.
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGifCompleted(gifUrl);
                    }
                });
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private String startRecording(String gifName, int numberOfFrames) {
        long startTime = Calendar.getInstance().getTimeInMillis();
        Log.d(TAG, "starting gif creation process for gif with name: " + gifName + " at " +startTime + "ms");

        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setRepeat(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoder.start(outputStream);

        try {
            createTempImages(numberOfFrames);
        } catch (InterruptedException e) {
            Log.e(TAG, "unable to create images, thread was interuptted", e);
            throw new IllegalStateException("failure creating temp images, something stopped background processing.");
        }

        Log.d(TAG, "finished processing all frames, starting gif encoder");

        addFramesToEncoder(numberOfFrames, encoder);

        encoder.finish();

        Log.d(TAG, "finished processing gif encoding, saving gif to disk.");

        String gifPath = writeGifToDisk(gifName, outputStream);

        long endTime = Calendar.getInstance().getTimeInMillis();

        Log.d(TAG, "finished creating gif" + gifName);
        Log.d(TAG, "created gif located at " + gifPath);
        Log.d(TAG, "total gif creation time: " + String.valueOf(endTime-startTime) + "ms");

        Log.d(TAG, "wiping out temp frames");
        destroyTempImages(numberOfFrames);
        Log.d(TAG, "clearing of temp images complete");

        return gifPath;
    }

    private void addFramesToEncoder(int numberOfFrames, AnimatedGifEncoder encoder) {
        for (int frame=0; frame <numberOfFrames; frame++) {
            Log.d(TAG, "[GifEncoder] processing frame number " + frame);
            String imagePath = getImagePathForFrame(frame);
            File imageToAdd = new File(imagePath);
            Bitmap bitmapToAdd = BitmapFactory.decodeFile(imageToAdd.getAbsolutePath());
            encoder.addFrame(bitmapToAdd);
        }
    }

    private String writeGifToDisk(String gifName, ByteArrayOutputStream outputStream) {
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
        return gifPath;
    }

    private void createTempImages(int numberOfFrames) throws InterruptedException {
        for (int frame=0; frame < numberOfFrames; frame++) {
            Log.d(TAG, "[Screenshot] processing frame number " + frame);
            String path = getImagePathForFrame(frame);
            takeScreenShot(viewToScreenshot, path);
            //take a screenshot evey x ms
            Thread.sleep(50);
        }

        //wait a bit since we are firing off new threads to process frames and the last one might not have finished yet. Lazy...I know.
        Thread.sleep(1000);
    }

    private void destroyTempImages(int numberOfFrames) {
        for (int frame=0; frame < numberOfFrames; frame++) {
            Log.d(TAG, "[Screenshot] deleting temp image frame number " + frame);
            String path = getImagePathForFrame(frame);
            File file = new File(path);
            boolean isDeleted = file.delete();
            Log.d(TAG, "deleted frame " + frame + " delete status=" + isDeleted);
        }
    }

    private void takeScreenShot(final View view, final String path) {
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                view.draw(canvas);
                //save image off UI thread....causes lag if not.
                saveScreenShotToDiskOnNewThread(path, bitmap);
            }
        });

    }

    private void saveScreenShotToDiskOnNewThread(final String path, final Bitmap bitmap) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
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
        });
        thread.start();
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
