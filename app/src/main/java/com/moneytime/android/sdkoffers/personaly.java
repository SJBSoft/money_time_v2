package com.moneytime.android.sdkoffers;

import android.app.Activity;
import android.os.Bundle;

import com.moneytime.android.helper.Misc;

public class personaly extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Misc.showMessage(this, "This Ad Network has been removed due to \"Zip path traversal\" vulnerability.", true);
    }
}
