package net.baruchans.hizen;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class HizenActivity extends AppCompatActivity {
    private static final String TAG = HizenActivity.class.getSimpleName();

    private static Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HizenActivity.mContext = this;

        HizenApplication application = new HizenApplication();
        application.run();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "destroy");
    }

    public static Context getContext() {
        return HizenActivity.mContext;
    }
}
