package com.hippo.vectorold.demo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;

import com.hippo.vectorold.VectorResources;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VectorResources vr = VectorResources.getVectorResources(this);
        Drawable d = vr.getDrawable(R.drawable.vector_test);

        Log.d("TAG", d.getClass().getName());

        ImageView iv = (ImageView) findViewById(R.id.image);
        iv.setImageDrawable(d);
    }
}
