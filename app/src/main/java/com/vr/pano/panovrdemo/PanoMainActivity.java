package com.vr.pano.panovrdemo;

import android.app.Activity;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.widgets.pano.VrPanoramaView;

public class PanoMainActivity extends Activity {

    private VrPanoramaView panoView;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pano_main);
    }
}
