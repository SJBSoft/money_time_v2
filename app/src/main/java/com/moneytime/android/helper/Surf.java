package com.moneytime.android.helper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moneytime.android.R;

import java.util.HashMap;

public class Surf extends BaseActivity {
    private WebView webView;
    private TextView titleView;
    private String cred;
    private ProgressBar progressBar;
    private ImageView backwardView, forwardView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        } else {
            String url = extras.getString("url", null);
            if (url == null) {
                finish();
            } else {
                setContentView(R.layout.surf);
                if (extras.getBoolean("fullscreen", false)) {
                    findViewById(R.id.surf_full_scr_1).setVisibility(View.GONE);
                    findViewById(R.id.surf_full_scr_2).setVisibility(View.GONE);
                }
                titleView = findViewById(R.id.surf_title);
                webView = findViewById(R.id.surf_webView);
                backwardView = findViewById(R.id.surf_backward);
                forwardView = findViewById(R.id.surf_forward);
                progressBar = findViewById(R.id.surf_progressBar);
                progressBar.setIndeterminate(false);
                progressBar.setMax(100);
                cred = extras.getString("cred", null);
                initWebView(url);
                findViewById(R.id.surf_close).setOnClickListener(view -> finish());
                backwardView.setOnClickListener(view -> {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    }
                });
                forwardView.setOnClickListener(view -> {
                    if (webView.canGoForward()) {
                        webView.goForward();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        WebStorage.getInstance().deleteAllData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        }
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearSslPreferences();
        super.onDestroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String url) {
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!TextUtils.isEmpty(title)) {
                    titleView.setText(title);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("market://")) {
                    try {
                        Intent i = new Intent("android.intent.action.VIEW");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;
                    } catch (Exception e) {
                        view.loadUrl(url);
                        return false;
                    }
                }
                view.loadUrl(url);
                return false;
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (webView.canGoBack()) {
                    backwardView.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    backwardView.setColorFilter(Color.argb(150, 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                }
                if (webView.canGoForward()) {
                    forwardView.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    forwardView.setColorFilter(Color.argb(150, 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        if (cred != null) {
            try {
                String[] creds = cred.split(":");
                HashMap<String, String> headerMap = new HashMap<>();
                headerMap.put(creds[0], creds[1]);
                webView.loadUrl(url, headerMap);
            } catch (Exception e) {
                webView.loadUrl(url);
            }
        } else {
            webView.loadUrl(url);
        }
    }
}