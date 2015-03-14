package com.hippo.vectorold.demo;

import android.app.Application;
import android.content.Context;

import com.hippo.vectorold.Vector;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Vector.wrapContext(newBase));
    }
}
