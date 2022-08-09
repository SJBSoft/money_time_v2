package com.moneytime.android.sdkoffers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.moneytime.android.Home;
import com.moneytime.android.helper.Misc;
import com.moneytime.android.offers.Offers;

import java.util.HashMap;
import java.util.Objects;

public class admob extends Activity {
    private ProgressDialog dialog;
    private String slot, user;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        user = intent.getStringExtra("user");
        if (data != null && user != null) {
            dialog = Misc.customProgress(this);
            dialog.show();
            slot = data.get("rewarded_slot");
            if (!Home.adMobInitialized) {
                MobileAds.initialize(getApplicationContext(), initializationStatus -> {
                    Home.adMobInitialized = true;
                    loadAds();
                });
            } else {
                loadAds();
            }
        } else {
            finish();
        }
    }

    private void loadAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, Objects.requireNonNull(slot),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        dialog.dismiss();
                        uiToast("" + loadAdError.getMessage());
                        finish();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        dialog.dismiss();
                        ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder().setUserId(user).build();
                        ad.setServerSideVerificationOptions(options);
                        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                uiToast("" + adError.getMessage());
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                finish();
                            }
                        });
                        ad.show(admob.this, rewardItem -> {
                            Offers.checkBalance = true;
                        });
                    }
                });
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(admob.this, toast, Toast.LENGTH_LONG).show());
    }
}