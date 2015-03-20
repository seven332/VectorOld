package com.hippo.vectorold.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hippo.vectorold.VectorContext;
import com.hippo.vectorold.demo.progress.ProgressActivity;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{
                        getString(R.string.activity_progress)
                }));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Class aClass;
                switch (position) {
                    default:
                    case 0:
                        aClass = ProgressActivity.class;
                        break;
                }

                Intent intent = new Intent(MainActivity.this, aClass);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(VectorContext.wrapContext(newBase));
    }
}
