package com.example.datarecovery

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.akexorcist.localizationactivity.core.LanguageSetting.setLanguage
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import com.example.datarecovery.utils.AdMobInterstitialAd
import com.example.datarecovery.utils.AdmobNativeAd
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.SharedPrefsHelper
import com.example.datarecovery.utils.ThemesUtil
import com.example.datarecovery.views.activities.LoaderDialogActivity
import com.example.datarecovery.views.activities.SplashActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import java.util.*


class AppController : LocalizationApplication(),
    Application.ActivityLifecycleCallbacks,
    LifecycleObserver {


    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isAdShowing = false
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(this) }
    override fun getDefaultLanguage(context: Context) = Locale.ENGLISH

    companion object {
        val splshinterstialAd by lazy {
            AdMobInterstitialAd()
        }

        val nativeAdRef by lazy {
            AdmobNativeAd()
        }
    }

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {
            Log.d("MyLogs", "onCreate: $it")
        }

        val locale = Locale(Resources.getSystem().configuration.locale.language)
        setLanguage(this, locale)

        ThemesUtil.sTheme = AppPreferences.getInstance(this).getTheme

        FirebaseApp.initializeApp(this)

        MultiDex.install(this);


        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        if (isAppOpenEnabled()) {
            if (currentActivity is SplashActivity) {

            } else {
                if (currentActivity != null) {
                    Log.d("check!!!", "onMoveToForeground: ")
                    showLoadingDialog()
                    currentActivity?.let { loadAppOpenAd(it) }
                }
            }
        }
    }

    private fun isAppOpenEnabled(): Boolean {
        return sharedPrefsHelper.getIsAppOpenEnabled()
    }

    private fun dismissLoadingDialog() {
        currentActivity?.let { activity ->
            if (activity is LoaderDialogActivity) {
                activity.finish()
            }
        }
    }

    private fun showLoadingDialog() {
        currentActivity?.let { activity ->
            val intent = Intent(activity, LoaderDialogActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)
        }
    }

    private fun loadAppOpenAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            sharedPrefsHelper.getAppOpenAdId(),
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    dismissLoadingDialog()
                    showAdIfAvailable(activity)
                    {

                    }
                    Log.d("AppOpenAd", "Ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d("AppOpenAd", "Failed to load ad: ${error.message}")
                    dismissLoadingDialog()
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit) {
        if (isAdShowing || appOpenAd == null) {
            onAdDismissed()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                appOpenAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isAdShowing = false
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isAdShowing = true
            }
        }

        appOpenAd?.show(activity)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

}