# gifsnap
Android library for creating a gif of any view. 

Example Gif created from app: 

![alt tag](http://i.imgur.com/tpET5SH.gif)

This library uses Gif Encoder from https://github.com/nbadal/android-gif-encoder. Please follow any license restrictions from that code (AndroidGifEncoder). 

Example Usage, which will record a gif with 40 frames of the view contentView. Keep in mind that a lot of processing is done on background threads....callback is on UI thread. 
```java
GifSnap gifSnap = new GifSnap(findViewById(android.R.id.content));
gifSnap.recordGif("GifSnap", 10, new OnGifSnapListener() {
    @Override
    public void onGifCompleted(String gifPath) {

    }
});
```

app module is just for local testing. 

cleanup of library still needs to happen. 
