package com.hippo.vectorold.demo.vector;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.hippo.vectorold.content.VectorContext;
import com.hippo.vectorold.demo.R;

public class VectorActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vector);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(VectorContext.wrapContext(newBase));
    }
}
