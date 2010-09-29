package com.foo;

import android.app.Activity;
import android.os.Bundle;

public class MyActivity extends Activity {
    private static String TAG = "rindirect-test";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
