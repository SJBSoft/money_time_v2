package com.moneytime.android.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.moneytime.android.Splash;

public abstract class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Variables.isLive) {
            if (Variables.locale != null) Misc.setLocale(this, Variables.locale);
        } else {
            startActivity(new Intent(this, Splash.class));
            finish();
        }
    }
}
