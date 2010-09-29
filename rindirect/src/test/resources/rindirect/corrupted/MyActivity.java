package my.application;

import de.akquinet.android.androlog.Log;
import android.app.Activity;
import android.os.Bundle;

public class MyActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializes androlog
        // This will read the /sdcard/my.application.properties file
        Log.init(this);

        // Log a messgage
        Log.i(this, "This is a logged message");

        setContentView(R.layout.main);
    }
}