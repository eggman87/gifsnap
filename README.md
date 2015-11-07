# gifsnap
Android library for creating a gif of any view. 

Created during a quick hour hackathon, needs optimization. 

Example gif created from app: 

![alt tag](http://i.imgur.com/tpET5SH.gif)

This library uses Gif Encoder from https://github.com/nbadal/android-gif-encoder. Please follow any license restrictions from that code (AndroidGifEncoder). 

Example Usage, which will record a gif with 40 frames of the view android.R.id.content (root view of an activity). Keep in mind that a lot of processing is done on background threads....callback is on UI thread. 
```java
GifSnap gifSnap = new GifSnap(findViewById(android.R.id.content));
gifSnap.recordGif("GifSnap", 40, new OnGifSnapListener() {
    @Override
    public void onGifCompleted(String gifPath) {

    }
});
```

Notes:

- app module is just for local testing. 
- code is a little messy...will be refactored soon. 
