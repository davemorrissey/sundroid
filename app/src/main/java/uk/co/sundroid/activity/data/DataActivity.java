package uk.co.sundroid.activity.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = new Intent(this, RealDataActivity.class);
        startActivity(data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent data = new Intent(this, RealDataActivity.class);
        startActivity(data);
    }
}
