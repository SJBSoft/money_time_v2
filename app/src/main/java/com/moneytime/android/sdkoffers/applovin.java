package com.moneytime.android.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;
import com.moneytime.android.helper.Misc;
import com.moneytime.android.offers.Offers;

import java.util.HashMap;

public class applovin extends Activity {
    private ProgressDialog dialog;
    private AppLovinIncentivizedInterstitial myIncent;
    private AppLovinAdDisplayListener listener;
    private AppLovinSdk.SdkInitializationListener sdklistener;
    private AppLovinSdk instance;
    private MaxRewardedAd rewardedAd;
    private String unit;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();
            String key = data.get("sdk_key");
            if (key == null) {
                finish();
                Toast.makeText(this, "Setup first", Toast.LENGTH_LONG).show();
                return;
            } else {
                String[] k = key.split(",");
                if (k.length > 1) {
                    key = k[0];
                    unit = k[1];
                } else {
                    unit = data.get("unit_id");
                }
            }
            AppLovinSdkSettings settings = new AppLovinSdkSettings(this);
            settings.setVerboseLogging(false);
            instance = AppLovinSdk.getInstance(key, settings, this);
            instance.setUserIdentifier(user);
            if (unit != null && !unit.isEmpty()) instance.setMediationProvider("max");
            sdklistener = config -> {
                if (unit == null || unit.isEmpty()) {
                    myIncent = AppLovinIncentivizedInterstitial.create(instance);
                    listener = new AppLovinAdDisplayListener() {
                        @Override
                        public void adDisplayed(AppLovinAd ad) {

                        }

                        @Override
                        public void adHidden(AppLovinAd ad) {
                            myIncent.preload(null);
                            if (dialog.isShowing()) dialog.dismiss();
                            finish();
                        }
                    };
                    myIncent.preload(new AppLovinAdLoadListener() {
                        @Override
                        public void adReceived(AppLovinAd appLovinAd) {
                            if (dialog.isShowing()) dialog.dismiss();
                            myIncent.show(appLovinAd, applovin.this, null,
                                    null, listener, null);
                            Offers.checkBalance = true;
                        }

                        @Override
                        public void failedToReceiveAd(int errorCode) {
                            if (dialog.isShowing()) dialog.dismiss();
                            uiToast("Error code: " + errorCode);
                        }
                    });
                } else {
                    rewardedAd = MaxRewardedAd.getInstance(unit, instance, applovin.this);
                    rewardedAd.setListener(new MaxRewardedAdListener() {
                        @Override
                        public void onRewardedVideoStarted(MaxAd ad) {
                        }

                        @Override
                        public void onRewardedVideoCompleted(MaxAd ad) {
                            if (dialog.isShowing()) dialog.dismiss();
                            finish();
                        }

                        @Override
                        public void onUserRewarded(MaxAd ad, MaxReward reward) {
                            Offers.checkBalance = true;
                        }

                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            if (dialog.isShowing()) dialog.dismiss();
                            rewardedAd.showAd();
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {
                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {
                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {
                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            uiToast("" + error);
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            uiToast("" + error);
                        }
                    });
                    rewardedAd.loadAd();
                }
            };
            instance.initializeSdk(sdklistener);
            new Handler().postDelayed(() -> runOnUiThread(() -> {
                if (!instance.isInitialized()) {
                    if (dialog.isShowing()) dialog.dismiss();
                    Toast.makeText(applovin.this, "Could not initialize SDK", Toast.LENGTH_LONG).show();
                    finish();
                }
            }), 5000);
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> {
            if (dialog.isShowing()) dialog.dismiss();
            Toast.makeText(applovin.this, toast, Toast.LENGTH_LONG).show();
            applovin.this.finish();
        });
    }

    protected void onDestroy() {
        listener = null;
        myIncent = null;
        sdklistener = null;
        super.onDestroy();
    }
}