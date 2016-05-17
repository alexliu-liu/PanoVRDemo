package com.vr.pano.panovrdemo;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaEventListener;
import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PanoMainActivity extends Activity {
    private String TAG = PanoMainActivity.class.getSimpleName();
    private VrPanoramaView panoView;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();

    private ImageLoaderTask backgroundImageLoaderTask;
    private boolean loadImageSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pano_main);

        panoView = (VrPanoramaView) findViewById(R.id.pano_view);
        panoView.setEventListener(new ActivityEventListener());

        int iresource = 0;
        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
        Log.i(TAG, "Options.inputType = " + panoOptions.inputType);

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(iresource, panoOptions));
    }

    @Override
    protected void onPause() {
        panoView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Integer, VrPanoramaView.Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Integer, VrPanoramaView.Options>... fileInformation) {
            VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                panoOptions = new VrPanoramaView.Options();
                panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
            } else {
                panoOptions = fileInformation[0].second;
            }

            panoView.loadImageFromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.stereo0), panoOptions);

            return true;
        }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    PanoMainActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }
}
