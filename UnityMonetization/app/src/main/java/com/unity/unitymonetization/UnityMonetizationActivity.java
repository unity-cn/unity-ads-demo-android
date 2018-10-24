package com.unity.unitymonetization;

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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.PlayerMetaData;
import com.unity3d.services.UnityServices;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.monetization.IUnityMonetizationListener;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdListenerAdapter;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdPlacementContent;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.monetization.placementcontent.purchasing.NativePromoAdapter;
import com.unity3d.services.monetization.placementcontent.purchasing.PromoAdPlacementContent;
import com.unity3d.services.monetization.placementcontent.purchasing.PromoMetadata;
import com.unity3d.services.purchasing.UnityPurchasing;
import com.unity3d.services.purchasing.core.IPurchasingAdapter;
import com.unity3d.services.purchasing.core.IRetrieveProductsListener;
import com.unity3d.services.purchasing.core.ITransactionListener;
import com.unity3d.services.purchasing.core.Product;
import com.unity3d.services.purchasing.core.TransactionDetails;

import java.util.Arrays;
import java.util.Map;


public class UnityMonetizationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String DEFAULT_GAME_ID = "123456";
    private static final String TAG_UNITY_ADS_DEMO = "UnityMonetizationDemo";
    private static final String PLACEMENT_ID_SKIPPABLE_VIDEO = "video";
    private static final String PLACEMENT_ID_REWARDED_VIDEO = "rewardedVideo";
    private static final String PLACEMENT_ID_BANNER = "banner";
    private static final String PLACEMENT_ID_PROMO = "testIAPPromo";

    private ScrollView mSvContainer;
    private TextView mTvLog;
    private CheckBox mCbDebugMode;
    private CheckBox mCbTestMode;
    private EditText mEtGameId;
    private Button mBtnInit;
    private Button mBtnShowSkippableVideoAd;
    private Button mBtnShowRewardedVideoAd;
    private Button mBtnShowBannerAd;
    private Button mBtnShowPromoAd;

    private Handler mScrollHandler;
    private String mGameId;

    private View bannerView;


    final IUnityMonetizationListener unityMonetizationListener = new UnityMonetizationListener();
    final IUnityBannerListener unityBannerListener = new UnityBannerListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unity_monetization);

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
//        mBtnShowSkippableVideoAd.setText(String.format("Show \"%s\" Ad", PLACEMENT_ID_SKIPPABLE_VIDEO));
        mBtnShowSkippableVideoAd.setOnClickListener(this);

        mBtnShowRewardedVideoAd = (Button) findViewById(R.id.btn_show_rewarded_video_ad);
        mBtnShowRewardedVideoAd.setEnabled(false);
//        mBtnShowRewardedVideoAd.setText(String.format("Show \"%s\" Ad", PLACEMENT_ID_REWARDED_VIDEO));
        mBtnShowRewardedVideoAd.setOnClickListener(this);


        mBtnShowBannerAd = (Button) findViewById(R.id.btn_show_banner_ad);
        mBtnShowBannerAd.setEnabled(false);
//        mBtnShowBannerAd.setText(String.format("Show \"%s\" Ad", PLACEMENT_ID_BANNER));
        mBtnShowBannerAd.setOnClickListener(this);

        mBtnShowPromoAd = (Button) findViewById(R.id.btn_show_promo);
        mBtnShowPromoAd.setEnabled(false);
//        mBtnShowPromoAd.setText(String.format("Show \"%s\" Ad", PLACEMENT_ID_PROMO));
        mBtnShowPromoAd.setOnClickListener(this);

        Button btnShowInfo = (Button) findViewById(R.id.btn_show_info);
        btnShowInfo.setOnClickListener(this);

        UnityMonetization.setListener(unityMonetizationListener);
        UnityPurchasing.setAdapter(new UnityPurchasingAdapter());

        UnityBanners.setBannerListener(unityBannerListener);
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
        UnityServices.setDebugMode(mCbDebugMode.isChecked());
        UnityMonetization.initialize(this, mGameId, unityMonetizationListener, mCbTestMode.isChecked());

        mBtnShowBannerAd.setEnabled(true);
    }

    private void showSkippableVideoAd() {

        if (!UnityMonetization.isReady(PLACEMENT_ID_SKIPPABLE_VIDEO)) {
            showLog("rewarded video ad is not ready.", Color.RED, false);
            return;
        }

        PlayerMetaData playerMetaData = new PlayerMetaData(this);
        playerMetaData.setServerId("unityadssupport");
        playerMetaData.commit();

        PlacementContent placementContent = UnityMonetization.getPlacementContent(PLACEMENT_ID_SKIPPABLE_VIDEO);

        showLog("begin to show skippable video ad.", Color.GREEN, false);
        mBtnShowSkippableVideoAd.setEnabled(false);
        mBtnShowRewardedVideoAd.setEnabled(false);

        if(placementContent instanceof ShowAdPlacementContent)
        {
            ((ShowAdPlacementContent)placementContent).show(this, new ShowAdListenerAdapter() {
                @Override
                public void onAdFinished(String placementId, UnityAds.FinishState withState) {
                    showLog(String.format("onUnityAdsFinish. placementId: %s, finishState: %s", placementId, withState), Color.WHITE, false);
                    if (withState == UnityAds.FinishState.COMPLETED) {
                        //reward player
                        showLog(">>>>>You get 100 coins!<<<<<", Color.RED, true);
                    }
                }

                @Override
                public void onAdStarted(String placementId) {
                    showLog(String.format("onAdStarted. placementId: %s", placementId), Color.WHITE, false);
                }
            });
        }
    }

    private void showRewardedVideoAd() {
        if (!UnityMonetization.isReady(PLACEMENT_ID_REWARDED_VIDEO)) {
            showLog("rewarded video ad is not ready.", Color.RED, false);
            return;
        }
        showLog("begin to show rewarded video ad.", Color.GREEN, false);
        mBtnShowSkippableVideoAd.setEnabled(false);
        mBtnShowRewardedVideoAd.setEnabled(false);
        PlacementContent placementContent = UnityMonetization.getPlacementContent(PLACEMENT_ID_REWARDED_VIDEO);

        if(placementContent instanceof ShowAdPlacementContent)
        {
            ((ShowAdPlacementContent)placementContent).show(this, new ShowAdListenerAdapter() {
                @Override
                public void onAdFinished(String placementId, UnityAds.FinishState withState) {
                    showLog(String.format("onUnityAdsFinish. placementId: %s, finishState: %s", placementId, withState), Color.WHITE, false);
                    if (withState == UnityAds.FinishState.COMPLETED) {
                        //reward player
                        showLog(">>>>>You get 100 coins!<<<<<", Color.RED, true);
                    }
                }

                @Override
                public void onAdStarted(String placementId) {
                    showLog(String.format("onAdStarted. placementId: %s", placementId), Color.WHITE, false);
                }
            });
        }
    }

    private void showBannerAd()
    {
        if (bannerView == null) {
            UnityBanners.loadBanner(this, PLACEMENT_ID_BANNER);
        }else {
            UnityBanners.destroy();
        }
    }

    private void showPromoAd()
    {
        PlacementContent placementContent = UnityMonetization.getPlacementContent(PLACEMENT_ID_PROMO);
        if (placementContent instanceof PromoAdPlacementContent)
        {
            showPromo((PromoAdPlacementContent)placementContent);
        }

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
            case R.id.btn_show_banner_ad:
                showBannerAd();
                break;
            case R.id.btn_show_promo:
                showPromoAd();
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

    /* LISTENER */

    private class UnityBannerListener implements IUnityBannerListener {

        @Override
        public void onUnityBannerLoaded(String placementId, View view) {
            bannerView = view;
            ((ViewGroup)findViewById(R.id.unity_monetization_layout_root)).addView(view);
            showLog("onUnityBannerLoaded Banner Loaded", Color.GREEN, true);
        }

        @Override
        public void onUnityBannerUnloaded(String placementId) {

            showLog(String.format("onUnityBannerUnloaded %s unloaded", placementId), Color.GREEN, true);

            bannerView = null;
        }

        @Override
        public void onUnityBannerShow(String placementId) {
            showLog(String.format("onUnityBannerShow %s show", placementId), Color.GREEN, true);
        }

        @Override
        public void onUnityBannerClick(String placementId) {

            showLog(String.format("onUnityBannerShow %s click", placementId), Color.GREEN, true);
        }

        @Override
        public void onUnityBannerHide(String placementId) {
            showLog(String.format("onUnityBannerHide %s hide", placementId), Color.GREEN, true);
        }

        @Override
        public void onUnityBannerError(String message) {
            showLog(String.format("onUnityBannerError Banner error: %s", message), Color.GREEN, true);
        }
    }

    private class UnityMonetizationListener implements IUnityMonetizationListener {

        @Override
        public void onPlacementContentReady(final String placement, PlacementContent placementContent) {

            Utilities.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showLog(String.format("onPlacementContentReady Initialized: %s", placement), Color.WHITE, false);
                    // look for various default placement ids over time

                    switch (placement) {
                        case PLACEMENT_ID_SKIPPABLE_VIDEO:
                            showLog(String.format("Enable Button: %s", PLACEMENT_ID_SKIPPABLE_VIDEO), Color.WHITE, false);
                            mBtnShowSkippableVideoAd.setEnabled(true);
                            break;

                        case PLACEMENT_ID_REWARDED_VIDEO:
                            showLog(String.format("Enable Button: %s", PLACEMENT_ID_REWARDED_VIDEO), Color.WHITE, false);
                            mBtnShowRewardedVideoAd.setEnabled(true);
                            break;

                        case PLACEMENT_ID_PROMO:
                            showLog(String.format("Enable Button: %s", PLACEMENT_ID_PROMO), Color.WHITE, false);
                            mBtnShowPromoAd.setEnabled(true);
                            break;

                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public void onPlacementContentStateChange(String placementId, PlacementContent placementContent, UnityMonetization.PlacementContentState previousState, UnityMonetization.PlacementContentState newState) {

        }

        @Override
        public void onUnityServicesError(UnityServices.UnityServicesError error, String message) {

            showLog(String.format("onUnityServicesError %s - %s", error, message), Color.WHITE, true);
        }
    }

    private class UnityPurchasingAdapter implements IPurchasingAdapter {

        @Override
        public void retrieveProducts(IRetrieveProductsListener listener) {
            listener.onProductsRetrieved(Arrays.asList(Product.newBuilder()
                    .withProductId("com.unity3d.monteization.example.productID")
                    .withLocalizedTitle("productTitle")
                    .withLocalizedPriceString("$1.99")
                    .withProductType("PRIMUNM")
                    .withIsoCurrencyCode("USD")
                    .withLocalizedPrice(1.99)
                    .withLocalizedDescription("Localized Description")
                    .build()));
        }

        @Override
        public void onPurchase(String productID, ITransactionListener listener, Map<String, Object> extras) {
            showLog(String.format("Purchasing Wants to purchase %s", productID), Color.WHITE, true);


            listener.onTransactionComplete(TransactionDetails.newBuilder()
                    .withTransactionId("foobar")
                    .withReceipt("What is a receipt even?")
                    .putExtra("foo", "bar")
                    .build());
        }
    }

    private void showPromo(final PromoAdPlacementContent placementContent) {
        final NativePromoAdapter nativePromoAdapter = new NativePromoAdapter(placementContent);
        PromoMetadata metadata = placementContent.getMetadata();
        Product product = metadata.getPremiumProduct();
        String price = product == null ? "$0.99" : product.getLocalizedPriceString();
        final View root = getLayoutInflater().inflate(R.layout.unitymonetization_native_promo, (ViewGroup) findViewById(R.id.unity_monetization_layout_root));
        Button buyButton = root.findViewById(R.id.native_promo_buy_button);
        Button closeButton = root.findViewById(R.id.native_promo_close_button);
        buyButton.setText("Buy now for only " + price + "!");

        nativePromoAdapter.onShown();
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do purchase then call
                nativePromoAdapter.onClosed();
                ((ViewGroup)root).removeView(findViewById(R.id.native_promo_root));


            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativePromoAdapter.onClosed();
                ((ViewGroup)root).removeView(findViewById(R.id.native_promo_root));
            }
        });
    }
}
