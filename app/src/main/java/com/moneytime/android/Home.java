package com.moneytime.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.moneytime.android.account.Gift;
import com.moneytime.android.account.Login;
import com.moneytime.android.account.Profile;
import com.moneytime.android.account.Refs;
import com.moneytime.android.account.Settings;
import com.moneytime.android.chatsupp.Chat;
import com.moneytime.android.games.GameList;
import com.moneytime.android.games.Lotto;
import com.moneytime.android.games.ScratcherCat;
import com.moneytime.android.games.Slot;
import com.moneytime.android.games.Wheel;
import com.moneytime.android.helper.BaseAppCompat;
import com.moneytime.android.helper.GlobalMsg;
import com.moneytime.android.helper.PopupNotif;
import com.moneytime.android.helper.PushMsg;
import com.moneytime.android.helper.Variables;
import com.moneytime.android.helper.arAdapter;
import com.moneytime.android.offers.Offers;
import com.squareup.picasso.Picasso;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.mintsoft.mintlib.GetAuth;
import org.mintsoft.mintlib.GetGame;
import org.mintsoft.mintlib.GetNet;
import org.mintsoft.mintlib.onResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Home extends BaseAppCompat {
    public static SharedPreferences spf;
    public static String currency, balance, interstitialUnit, rewardedUnit, uName;
    private int adLoading, requestCode, delay;
    private arAdapter aradapter;
    private boolean backClick, unityIntReady;
    public static int fab_iv = 10000;
    public static boolean isExternal, adMobInitialized, fab, checkNotif,
            sAdv, showInterstitial = false, confettiAds = true;
    public static InterstitialAd interstitialAd;
    private ImageView avatarView;
    private Toast exitToast;
    private Animation blink;
    private TextView notifCountView, nameView, balView;
    private ActivityResultLauncher<Intent> activityForResult;
    private ReviewManager manager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.home);
        currency = "Coin";
        balance = "0";
        spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        nameView = findViewById(R.id.home_nameView);
        avatarView = findViewById(R.id.home_avatarView);
        balView = findViewById(R.id.home_ptsView);
        ImageView notifIcon = findViewById(R.id.home_notifView);
        blink = AnimationUtils.loadAnimation(Home.this, R.anim.blink);
        isExternal = spf.getBoolean("ex_surf", false);
        currency = spf.getString("currency", "Coin");
        balView.setText("--- " + currency.toLowerCase() + "s");
        notifCountView = findViewById(R.id.home_notif_count);
        GetAuth.userinfo(this, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data) {
                delay = spf.getInt("interval", 10) * 1000;
                uName = data.get("name");
                nameView.setText(uName);
                Picasso.get().load(data.get("avatar"))
                        .placeholder(R.drawable.anim_loading)
                        .error(R.drawable.avatar).into(avatarView);
                checkBal();
                checkNotifCount();
            }
        });
        balView.setOnClickListener(v -> checkBal());
        activityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (requestCode == 99) {
                        if (resultCode == 0) {
                            notifCountView.setVisibility(View.GONE);
                            notifCountView.clearAnimation();
                        } else if (resultCode > 9) {
                            notifCountView.setText("9+");
                            notifCountView.clearAnimation();
                            notifCountView.startAnimation(blink);
                        } else {
                            notifCountView.setText(String.valueOf(resultCode));
                            notifCountView.clearAnimation();
                            notifCountView.startAnimation(blink);
                        }
                    } else if (requestCode == 96) {
                        if (resultCode == 9) {
                            Variables.reset();
                            GetAuth.removeCred(this);
                            startActivity(new Intent(this, Login.class));
                            finish();
                        } else if (resultCode == 7) {
                            startActivity(new Intent(this, Splash.class));
                            finish();
                        }
                    }
                });

        findViewById(R.id.home_go_earn).setOnClickListener(v -> startActivity(new Intent(Home.this, Offers.class)));
        findViewById(R.id.home_go_offerwalls).setOnClickListener(v -> startActivity(new Intent(Home.this, Offers.class)));
        findViewById(R.id.home_go_games).setOnClickListener(v -> startActivity(new Intent(Home.this, GameList.class)));

        findViewById(R.id.home_go_redeem).setOnClickListener(v -> startActivity(new Intent(Home.this, Gift.class)));
        findViewById(R.id.home_go_invite).setOnClickListener(v -> startActivity(new Intent(Home.this, Refs.class)));
        findViewById(R.id.home_go_leaderboard).setOnClickListener(v -> startActivity(new Intent(Home.this, Leaderboard.class)));
        findViewById(R.id.home_go_scratcher).setOnClickListener(v -> startActivity(new Intent(Home.this, ScratcherCat.class)));
        findViewById(R.id.home_go_slot).setOnClickListener(v -> startActivity(new Intent(Home.this, Slot.class)));
        findViewById(R.id.home_go_spin).setOnClickListener(v -> startActivity(new Intent(Home.this, Wheel.class)));
        findViewById(R.id.home_go_lotto).setOnClickListener(v -> startActivity(new Intent(Home.this, Lotto.class)));
        findViewById(R.id.home_go_faq).setOnClickListener(v -> startActivity(new Intent(Home.this, Faq.class)));
        findViewById(R.id.home_go_support).setOnClickListener(v -> startActivity(new Intent(Home.this, Support.class)));
        findViewById(R.id.home_go_chat).setOnClickListener(v -> startActivity(new Intent(Home.this, Chat.class)));
        findViewById(R.id.home_go_settings).setOnClickListener(v -> {
            requestCode = 96;
            activityForResult.launch(new Intent(Home.this, Settings.class));
        });
        findViewById(R.id.home_go_instagram).setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse("http://instagram.com/_u/" + getString(R.string.instagram_url));
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("com.instagram.android");
                startActivity(likeIng);
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + getString(R.string.instagram_url))));
            }
        });
        findViewById(R.id.home_go_youtube).setOnClickListener(v -> {
            try {
                Intent appIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("vnd.youtube.com/channel/" + getString(R.string.youtube_url)));
                startActivity(appIntent);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/channel/" + getString(R.string.youtube_url)));
                startActivity(intent);
            }
        });
        avatarView.setOnClickListener(v -> {
            requestCode = 96;
            Intent intent = new Intent(Home.this, Profile.class);
            activityForResult.launch(intent);
        });
        findViewById(R.id.home_go_rateus).setOnClickListener(v -> {
            runOnUiThread(() -> {
                if (manager == null) manager = ReviewManagerFactory.create(Home.this);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    try {
                        if (task.isSuccessful()) {
                            ReviewInfo reviewInfo = task.getResult();
                            Task<Void> flow = manager.launchReviewFlow(Home.this, reviewInfo);
                            flow.addOnCompleteListener(task1 -> {
                                //todo:: after given rating
                            });
                        } else {
                            Toast.makeText(Home.this, "" + Objects
                                    .requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(Home.this, "Exception from openReview():"
                                + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                request.addOnFailureListener(e -> Toast.makeText(Home.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        });
        notifIcon.setOnClickListener(view -> {
            requestCode = 99;
            activityForResult.launch(new Intent(Home.this, PopupNotif.class));
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkGlobalMsg();
        checkActivityReward();
        preload();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onResume() {
        super.onResume();
        if (showInterstitial || spf.getBoolean("show_ad", false)) {
            showInterstitial = false;
            if (sAdv && unityIntReady) {
                spf.edit().putBoolean("show_ad", false).apply();
                showUnityAds();
            } else {
                if (interstitialAd != null) {
                    spf.edit().putBoolean("show_ad", false).apply();
                    interstitialAd.show(this);
                } else {
                    loadAd();
                }
            }
        }
        if (checkNotif) {
            checkNotif = false;
            checkNotifCount();
        }
        long cTime = System.currentTimeMillis();
        long sT = spf.getLong("r_time", cTime);
        if (sT <= cTime) {
            spf.edit().putLong("r_time", cTime + delay).commit();
            balView.setText("--- " + currency.toLowerCase() + "s");
            checkBal();
        }
    }

    @Override
    public void onBackPressed() {
        if (backClick) {
            super.onBackPressed();
        } else {
            backClick = true;
            if (exitToast == null) {
                exitToast = Toast.makeText(this, getString(R.string.double_back), Toast.LENGTH_SHORT);
            }
            exitToast.show();
            new Handler().postDelayed(() -> backClick = false, 2000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exitToast != null) exitToast.cancel();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 98) {
            if (resultCode == 8) {
                aradapter.done();
                aradapter.getImageView().setImageResource(R.drawable.ar_past);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkBal() {
        GetAuth.balance(Home.this, spf, new onResponse() {
            @Override
            public void onSuccessHashMap(HashMap<String, String> data1) {
                balance = data1.get("balance");
                balView.setText(balance + " " + currency.toLowerCase() + "s");
                uName = data1.get("name");
                nameView.setText(uName);
            }

            @Override
            public void onSuccess(String success) {
                checkNotifCount();
            }
        });
    }

    private void checkNotifCount() {
        int msgCount = GetNet.messageCount(Home.this);
        if (msgCount == 0) {
            notifCountView.setVisibility(View.GONE);
            notifCountView.clearAnimation();
        } else {
            if (msgCount > 9) {
                notifCountView.setText("9+");
            } else {
                notifCountView.setText(String.valueOf(msgCount));
            }
            notifCountView.setVisibility(View.VISIBLE);
            notifCountView.clearAnimation();
            notifCountView.startAnimation(blink);

        }
    }

    private void checkActivityReward() {
        final RecyclerView arView = findViewById(R.id.home_ar_recyclerView);
        final TextView arViewTitle = findViewById(R.id.home_ar_titleView);
        arView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        GetGame.activityReward(this, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> list) {
                int activeReward = Integer.parseInt(Objects.requireNonNull(list.get(0).get("current")));
                int isDone = Integer.parseInt(Objects.requireNonNull(list.get(0).get("is_done")));
                list.remove(0);
                list.remove(1);
                if (activeReward + isDone >= list.size()) {
                    arViewTitle.setVisibility(View.GONE);
                    arView.setVisibility(View.GONE);
                } else {
                    arViewTitle.setVisibility(View.VISIBLE);
                    arView.setVisibility(View.VISIBLE);
                    aradapter = new arAdapter(Home.this, list, activeReward, isDone);
                    arView.setAdapter(aradapter);
                }
            }

            @Override
            public void onError(int errorCode, String error) {
                Toast.makeText(Home.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkGlobalMsg() {
        new Handler().postDelayed(() -> {
            if (spf.getStringSet("push_msg", null) != null) {
                startActivity(new Intent(Home.this, PushMsg.class));
            } else if (spf.getBoolean("g_msg", true)) {
                String title = spf.getString("g_title", "");
                if (!title.isEmpty()) {
                    Intent intent = new Intent(Home.this, GlobalMsg.class);
                    intent.putExtra("id", spf.getString("gmid", "none"));
                    intent.putExtra("title", title);
                    intent.putExtra("info", spf.getString("g_desc", "Empty message body."));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    startActivity(intent);
                }
            }
        }, 2000);
    }

    private void startTransition(Intent intent, View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    Home.this, v, v.getTransitionName());
            startActivity(intent, activityOptionsCompat.toBundle());
        } else {
            startActivity(intent);
        }
    }

    public void preload() {
        try {
            HashMap<String, String> unityData = GetNet.sdkInfo("infos_cpv",
                    "unityads", new String[]{"active", "game_id", "unit_id_i",
                            "unit_id_r", "fab", "fab_iv", "confetti"});
            if (unityData.containsKey("active")) {
                String active = unityData.get("active");
                sAdv = active != null && active.equals("yes");
            }
            if (sAdv) {
                String i_slot = unityData.get("unit_id_i");
                if (i_slot != null && !i_slot.isEmpty()) {
                    interstitialUnit = i_slot;
                }
                String r_slot = unityData.get("unit_id_r");
                if (r_slot != null && !r_slot.isEmpty()) {
                    rewardedUnit = r_slot;
                }
                String fa = unityData.get("fab");
                if (fa != null && fa.equals("yes")) fab = true;
                String fiv = unityData.get("fab_iv");
                if (fiv != null) fab_iv = Integer.parseInt(fiv) * 1000;
                UnityAds.setDebugMode(false);
                UnityAds.initialize(getApplicationContext(), unityData.get("game_id"),
                        false, new IUnityAdsInitializationListener() {
                            @Override
                            public void onInitializationComplete() {
                                loadUnityAds();
                            }

                            @Override
                            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                                unityIntReady = false;
                            }
                        });
                String cf = unityData.get("confetti");
                if (cf != null && !cf.equals("yes")) {
                    confettiAds = false;
                }
            } else {
                HashMap<String, String> admobData = GetNet.sdkInfo("infos_cpv",
                        "admob", new String[]{"app_id", "interstitial_slot", "rewarded_slot",
                                "fab", "fab_iv", "confetti"});
                String app_id = admobData.get("app_id");
                if (app_id != null) {
                    ApplicationInfo ai = getPackageManager()
                            .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                    ai.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", app_id);
                }
                String i_slot = admobData.get("interstitial_slot");
                if (i_slot != null && !i_slot.isEmpty()) {
                    interstitialUnit = i_slot;
                }
                String r_slot = admobData.get("rewarded_slot");
                if (r_slot != null && !r_slot.isEmpty()) {
                    rewardedUnit = r_slot;
                }
                String fa = admobData.get("fab");
                if (fa != null && fa.equals("yes")) fab = true;
                MobileAds.initialize(getApplicationContext(), initializationStatus -> {
                    adMobInitialized = true;
                    loadAd();
                });
                String fiv = admobData.get("fab_iv");
                if (fiv != null) fab_iv = Integer.parseInt(fiv) * 1000;
                String cf = unityData.get("confetti");
                if (cf != null && !cf.equals("yes")) {
                    confettiAds = false;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void loadAd() {
        if (adLoading == 1 || interstitialAd != null || interstitialUnit == null) return;
        adLoading = 1;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getApplicationContext(), interstitialUnit, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitial) {
                        adLoading = 0;
                        interstitialAd = interstitial;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        interstitialAd = null;
                                        loadAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        interstitialAd = null;
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        loadAd();
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        adLoading = 0;
                        interstitialAd = null;
                    }
                });
    }

    private void loadUnityAds() {
        UnityAds.load(interstitialUnit, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                unityIntReady = true;
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                unityIntReady = false;
            }
        });
    }

    private void showUnityAds() {
        UnityAds.show(this, interstitialUnit, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                unityIntReady = false;
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {

            }

            @Override
            public void onUnityAdsShowClick(String placementId) {

            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                loadUnityAds();
            }
        });
    }
}