package com.moneytime.android.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.moneytime.android.helper.Misc;

import java.util.HashMap;

public class chartboost extends Activity {
    private ProgressDialog dialog;
    private ChartboostDelegate delegate;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            Chartboost.startWithAppId(getApplicationContext(), data.get("app_id"), data.get("app_signature"));
            Chartboost.setCustomId(user);
            Chartboost.showRewardedVideo(CBLocation.LOCATION_DEFAULT);
            delegate = new ChartboostDelegate() {
                @Override
                public boolean shouldDisplayRewardedVideo(String location) {
                    super.shouldDisplayRewardedVideo(location);
                    if (dialog.isShowing()) dialog.dismiss();
                    return true;
                }

                @Override
                public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
                    super.didFailToLoadRewardedVideo(location, error);
                    uiToast("" + error);
                    finish();
                }

                @Override
                public void didCloseRewardedVideo(String location) {
                    super.didCloseRewardedVideo(location);
                    finish();
                }
            };
            Chartboost.setDelegate(delegate);
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(chartboost.this, toast, Toast.LENGTH_LONG).show());
    }

    protected void onDestroy() {
        delegate = null;
        super.onDestroy();
    }
}