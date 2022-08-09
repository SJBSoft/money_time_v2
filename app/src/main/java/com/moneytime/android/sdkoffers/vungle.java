package com.moneytime.android.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.moneytime.android.helper.BaseActivity;
import com.moneytime.android.helper.Misc;
import com.moneytime.android.offers.Offers;
import com.vungle.warren.AdConfig;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.HashMap;

public class vungle extends BaseActivity {
    private ProgressDialog dialog;
    private InitCallback initCallback;
    private LoadAdCallback loadAdCallback;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            showDialog();

            loadAdCallback = new LoadAdCallback() {
                @Override
                public void onAdLoad(String id) {
                    AdConfig adConfig = new AdConfig();
                    adConfig.setAdOrientation(AdConfig.PORTRAIT);
                    Vungle.playAd(id, adConfig, null);
                    new Handler().postDelayed(vungle.this::finish, 1000);
                    Offers.checkBalance = true;
                }

                @Override
                public void onError(String id, VungleException exception) {
                    if (dialog.isShowing()) dialog.dismiss();
                    uiToast("Loading failed: " + exception.getMessage());
                    finish();
                }
            };
            initCallback = new InitCallback() {
                @Override
                public void onSuccess() {
                    Vungle.loadAd(data.get("placement"), loadAdCallback);
                }

                @Override
                public void onError(VungleException exception) {
                    if (dialog.isShowing()) dialog.dismiss();
                    uiToast("Initialization failed: " + exception.getMessage());
                    finish();
                }

                @Override
                public void onAutoCacheAdAvailable(String placementId) {

                }
            };
            Vungle.setIncentivizedFields(user, null, null, null, "Close");
            Vungle.init(data.get("app_id"), this.getApplicationContext(), initCallback);
        } else {
            finish();
        }
    }

    private void showDialog() {
        dialog = Misc.customProgress(this);
        dialog.show();
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(vungle.this, toast, Toast.LENGTH_LONG).show());
    }

    protected void onDestroy() {
        loadAdCallback = null;
        initCallback = null;
        super.onDestroy();
    }
}
