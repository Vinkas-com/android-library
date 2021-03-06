package com.vinkas.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.vinkas.util.Helper;

/**
 * Created by Vinoth on 3-5-16.
 */
public abstract class SplashActivity extends Activity {

    private static final int REQUEST_CONNECT = 1000;
    private static final int REQUEST_ANONYMOUS = 1001;
    private boolean waitingForResult = false;
    private boolean resultReceived = false;

    void authenticate() {
        FirebaseAuth fa = FirebaseAuth.getInstance();
        FirebaseUser user = fa.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, ConnectActivity.class);
            resultReceived = false;
            waitingForResult = true;
            startActivityForResult(intent, REQUEST_CONNECT);
        } else
            getHelper().setUser(user);
    }

    public boolean isReady() {
        if (getApp().isReady())
            return true;
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!waitingForResult && !resultReceived)
            initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONNECT || requestCode == REQUEST_ANONYMOUS)
            resultReceived = true;
        waitingForResult = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (savedInstanceState == null)
            getAnalytics().logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
    }

    protected void initialize() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isReady())
                        sleep(1000);
                    if (getCallingActivity() == null)
                        startMainActivity();
                    else
                        sendResult(Helper.RESULT_OK);
                } catch (InterruptedException e) {
                    Helper.onException(e);
                }
            }
        };
        thread.start();
        authenticate();
    }

    protected void startMainActivity() {
        Intent i = new Intent(this, getMainActivityClass());
        startActivity(i);
        finish();
    }

    protected abstract Class<?> getMainActivityClass();

}