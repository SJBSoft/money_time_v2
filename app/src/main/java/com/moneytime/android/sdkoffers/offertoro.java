package com.moneytime.android.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.moneytime.android.helper.BaseActivity;
import com.moneytime.android.helper.Misc;
import com.moneytime.android.offers.Offers;
import com.offertoro.sdk.OTOfferWallSettings;
import com.offertoro.sdk.interfaces.OfferWallListener;
import com.offertoro.sdk.sdk.OffersInit;

import java.util.HashMap;

public class offertoro extends BaseActivity {
    private ProgressDialog dialog;
    private OfferWallListener offerWallListener;
    private OffersInit offersInit;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        HashMap<String, String> data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            dialog = Misc.customProgress(this);
            dialog.show();
            try {
                OTOfferWallSettings.getInstance().configInit(data.get("app_id"), data.get("app_secret"), user);
                offersInit = OffersInit.getInstance();
                offerWallListener = new OfferWallListener() {
                    @Override
                    public void onOTOfferWallInitSuccess() {
                        if (dialog.isShowing()) dialog.dismiss();
                        offersInit.showOfferWall(offertoro.this);
                        Offers.checkBalance = true;
                    }

                    @Override
                    public void onOTOfferWallInitFail(String s) {
                        if (dialog.isShowing()) dialog.dismiss();
                        uiToast("" + s);
                        finish();
                    }

                    @Override
                    public void onOTOfferWallOpened() {

                    }

                    @Override
                    public void onOTOfferWallCredited(double v, double v1) {

                    }

                    @Override
                    public void onOTOfferWallClosed() {
                        finish();
                    }
                };
                offersInit.create(this);
                offersInit.setOfferWallListener(offerWallListener);
            } catch (Exception e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            finish();
        }
    }

    private void uiToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(offertoro.this, toast, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        offerWallListener = null;
        offersInit = null;
        super.onDestroy();
    }
}