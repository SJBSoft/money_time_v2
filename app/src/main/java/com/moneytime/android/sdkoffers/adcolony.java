package com.moneytime.android.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.moneytime.android.helper.BaseActivity;
import com.moneytime.android.helper.Misc;
import com.moneytime.android.offers.Offers;

import java.util.HashMap;

public class adcolony extends BaseActivity {
    private ProgressDialog dialog;
    private AdColonyInterstitialListener listener;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            AdColonyAppOptions appOptions = new AdColonyAppOptions().setUserID(user).setGDPRConsentString("1")
                    .setKeepScreenOn(true).setGDPRRequired(true);
            AdColony.configure(this, appOptions, data.get("app_id"), data.get("zone_id"));
            AdColonyAdOptions options = new AdColonyAdOptions().enableConfirmationDialog(false).enableResultsDialog(false);
            listener = new AdColonyInterstitialListener() {
                @Override
                public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
                    if (dialog.isShowing()) dialog.dismiss();
                    adColonyInterstitial.show();
                    Offers.checkBalance = true;
                }

                @Override
                public void onRequestNotFilled(AdColonyZone zone) {
                    if (dialog.isShowing()) dialog.dismiss();
                    Toast.makeText(adcolony.this, "No fill", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onClosed(AdColonyInterstitial ad) {
                    finish();
                }
            };
            AdColony.requestInterstitial(data.get("zone_id"), listener, options);
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    protected void onDestroy() {
        listener = null;
        super.onDestroy();
    }
}
