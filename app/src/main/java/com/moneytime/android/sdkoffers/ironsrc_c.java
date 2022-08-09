package com.moneytime.android.sdkoffers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.ironsource.adapters.supersonicads.SupersonicConfig;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.moneytime.android.helper.BaseActivity;
import com.moneytime.android.helper.Misc;
import com.moneytime.android.offers.Offers;

import java.util.HashMap;

public class ironsrc_c extends BaseActivity implements OfferwallListener {
    private boolean available;
    private ProgressDialog dialog;
    private HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        data = Misc.convertToHashMap(intent, "info");
        String user = intent.getStringExtra("user");
        if (data != null && user != null) {
            dialog = Misc.customProgress(this);
            dialog.show();
            IronSource.setUserId(user);
            SupersonicConfig.getConfigObj().setClientSideCallbacks(false);
            IronSource.setOfferwallListener(this);
            IronSource.init(ironsrc_c.this, data.get("app_key"), IronSource.AD_UNIT.OFFERWALL);
            new Handler().postDelayed(() -> {
                if (!available) {
                    dialog.dismiss();
                    finish();
                }
            }, 15000);
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    @Override
    public void onOfferwallAvailable(boolean b) {
        if (b && !available) {
            available = true;
            if (dialog.isShowing()) dialog.dismiss();
            IronSource.showOfferwall(data.get("offerwall_placement"));
        }
    }

    @Override
    public void onOfferwallOpened() {

    }

    @Override
    public void onOfferwallShowFailed(IronSourceError ironSourceError) {
        if (dialog.isShowing()) dialog.dismiss();
        Toast.makeText(this, "Error: " + ironSourceError.getErrorMessage(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onOfferwallAdCredited(int i, int i1, boolean b) {
        Offers.checkBalance = true;
        return false;
    }

    @Override
    public void onGetOfferwallCreditsFailed(IronSourceError ironSourceError) {

    }

    @Override
    public void onOfferwallClosed() {
        finish();
    }
}