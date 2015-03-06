# gifsnap
Android library for creating a gif of any view. 

This library uses Gif Encoder from https://github.com/nbadal/android-gif-encoder. Please follow any license restrictions from that code (AndroidGifEncoder). 

Example Usage, which will record a gif with 40 frames of the view contentView. 
```java
Runnable runnable = new Runnable() {
    @Override
    public void run() {
        GifSnap gifSnap = new GifSnap(contentView);
        gifSnap.recordGif("GifSnap", 40);
    }
};
Thread thread = new Thread(runnable);
thread.start();
```
