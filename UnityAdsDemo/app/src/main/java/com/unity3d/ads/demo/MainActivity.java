package com.unity3d.ads.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IUnityAdsListener, View.OnClickListener, IUnityAdsInitializationListener {
    private static final String DEFAULT_GAME_ID = "14851";
    private static final String TAG_UNITY_ADS_DEMO = "UnityAdsDemo";
    private static final String PLACEMENT_ID_SKIPPABLE_VIDEO = "video";
    private static final String PLACEMENT_ID_REWARDED_VIDEO = "rewardedVideo";

    private ScrollView mSvContainer;
    private TextView mTvLog;
    private CheckBox mCbDebugMode;
    private CheckBox mCbTestMode;
    private EditText mEtGameId;
    private Button mBtnInit;
    private Button mBtnShowSkippableVideoAd;
    private Button mBtnShowRewardedVideoAd;

    private Handler mScrollHandler;
    private String mGameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSvContainer = (ScrollView) findViewById(R.id.sv_container);

        mTvLog = (TextView) findViewById(R.id.tv_log);
        mTvLog.setMovementMethod(ScrollingMovementMethod.getInstance());

        mScrollHandler = new Handler();

        mEtGameId = (EditText) findViewById(R.id.et_game_id);
        mEtGameId.setText(DEFAULT_GAME_ID);
        mEtGameId.setSelection(mEtGameId.getText().length());

        mCbDebugMode = (CheckBox) findViewById(R.id.cb_debug_mode);
        mCbDebugMode.setOnClickListener(this);

        mCbTestMode = (CheckBox) findViewById(R.id.cb_test_mode);
        mCbTestMode.setOnClickListener(this);

        mBtnInit = (Button) findViewById(R.id.btn_init);
        mBtnInit.setOnClickListener(this);

        mBtnShowSkippableVideoAd = (Button) findViewById(R.id.btn_show_skippable_video_ad);
        mBtnShowSkippableVideoAd.setEnabled(false);
        mBtnShowSkippableVideoAd.setText(String.format("Show \"%s\" Ad", PLACEMENT_ID_SKIPPABLE_VIDEO));
        mBtnShowSkippableVideoAd.setOnClickListener(this);

        mBtnShowRewardedVideoAd = (Button) findViewById(R.id.btn_show_rewarded_video_ad);
        mBtnShowRewardedVideoAd.setEnabled(false);
        mBtnShowRewardedVideoAd.setText(String.format("Show \"%s\" Ad", PLACEMENT_ID_REWARDED_VIDEO));
        mBtnShowRewardedVideoAd.setOnClickListener(this);

        Button btnShowInfo = (Button) findViewById(R.id.btn_show_info);
        btnShowInfo.setOnClickListener(this);
    }

    private void initUnityAds() {
        mGameId = mEtGameId.getText().toString().trim();
        if (TextUtils.isEmpty(mGameId)) {
            showLog("please input game id.", Color.RED, true);
            return;
        }

        showLog(String.format("begin to initialize. game id: %s, test mode: %s", mGameId, mCbTestMode.isChecked()), Color.GREEN, false);
        mEtGameId.setFocusable(false);
        mEtGameId.setEnabled(false);
        mCbTestMode.setEnabled(false);
        mBtnInit.setEnabled(false);
        UnityAds.setDebugMode(mCbDebugMode.isChecked());
        UnityAds.addListener(this);
        UnityAds.initialize(getApplicationContext(), mGameId, mCbTestMode.isChecked(), this);
    }

    private void showSkippableVideoAd() {
        if (!UnityAds.isReady(PLACEMENT_ID_SKIPPABLE_VIDEO)) {
            showLog("skippable video ad is not ready.", Color.RED, false);
            return;
        }
        showLog("begin to show skippable video ad.", Color.GREEN, false);
        mBtnShowSkippableVideoAd.setEnabled(false);
        mBtnShowRewardedVideoAd.setEnabled(false);
        UnityAds.show(this, PLACEMENT_ID_SKIPPABLE_VIDEO);
    }

    private void showRewardedVideoAd() {
        if (!UnityAds.isReady(PLACEMENT_ID_REWARDED_VIDEO)) {
            showLog("rewarded video ad is not ready.", Color.RED, false);
            return;
        }
        showLog("begin to show rewarded video ad.", Color.GREEN, false);
        mBtnShowSkippableVideoAd.setEnabled(false);
        mBtnShowRewardedVideoAd.setEnabled(false);
        UnityAds.show(this, PLACEMENT_ID_REWARDED_VIDEO);
    }

    private void showInfo() {
        String info = "\ndebugMode:\t" + UnityAds.getDebugMode() +
                "\ntestMode:\t" + mCbTestMode.isChecked() +
                "\ngameId:\t\t" + mGameId +
                "\nisInitialized:\t" + UnityAds.isInitialized() +
                "\nisSupported:\t" + UnityAds.isSupported() +
                "\nversion:\t\t" + UnityAds.getVersion() +
                "\nPlacement State: " + PLACEMENT_ID_SKIPPABLE_VIDEO + ": " + UnityAds.getPlacementState(PLACEMENT_ID_SKIPPABLE_VIDEO) +
                "\nPlacement State: " + PLACEMENT_ID_REWARDED_VIDEO + ": " + UnityAds.getPlacementState(PLACEMENT_ID_REWARDED_VIDEO) + "\n";
        showLog(info, Color.GREEN, false);
    }

    private void showLog(String log, int color, boolean showToast) {
        Log.d(TAG_UNITY_ADS_DEMO, log);
        if (showToast) {
            Toast.makeText(this, log, Toast.LENGTH_SHORT).show();
        }

        log += "\n";
        SpannableString spanString = new SpannableString(log);
        ForegroundColorSpan redSpan = new ForegroundColorSpan(color);
        spanString.setSpan(redSpan, 0, log.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvLog.append(spanString);

        scrollToBottom();
    }

    @Override
    public void onUnityAdsReady(String placementId) {
        showLog(String.format("onUnityAdsReady. placementId: %s", placementId), Color.WHITE, false);
        switch (placementId) {
            case PLACEMENT_ID_SKIPPABLE_VIDEO:
                mBtnShowSkippableVideoAd.setEnabled(true);
                break;
            case PLACEMENT_ID_REWARDED_VIDEO:
                mBtnShowRewardedVideoAd.setEnabled(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onUnityAdsStart(String placementId) {
        showLog(String.format("onUnityAdsStart. placementId: %s", placementId), Color.WHITE, false);
    }

    @Override
    public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {
        showLog(String.format("onUnityAdsFinish. placementId: %s, finishState: %s", placementId, finishState), Color.WHITE, false);
        if (finishState == UnityAds.FinishState.COMPLETED) {
            //reward player
            showLog(">>>>>You get 100 coins!<<<<<", Color.RED, true);
        }
    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String placementId) {
        showLog(String.format("onUnityAdsError. unityAdsError: %s, placementId: %s", unityAdsError, placementId), Color.RED, false);
    }

    private void scrollToBottom() {
        mScrollHandler.post(new Runnable() {
            @Override
            public void run() {
                int offset = mTvLog.getMeasuredHeight() - mSvContainer.getMeasuredHeight();
                if (offset > 0) mSvContainer.scrollTo(0, offset);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_init:
                initUnityAds();
                break;
            case R.id.btn_show_skippable_video_ad:
                showSkippableVideoAd();
                break;
            case R.id.btn_show_rewarded_video_ad:
                showRewardedVideoAd();
                break;
            case R.id.btn_show_info:
                showInfo();
                break;
            case R.id.cb_debug_mode:
                UnityAds.setDebugMode(mCbDebugMode.isChecked());
                break;
            default:
                break;
        }
    }

    @Override
    public void onInitializationComplete() {
        final Timer AdTimer = new Timer();
        AdTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    showSkippableVideoAd();
                });
            }
        }, 5000);
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        // Actions after initialization failed
    }
}
