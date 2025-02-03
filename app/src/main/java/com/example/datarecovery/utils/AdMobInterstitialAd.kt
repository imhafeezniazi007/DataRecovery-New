package com.example.datarecovery.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.datarecovery.interfaces.DataListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobInterstitialAd {

    private val TAG = "AdmobAds"
    private var mInterstitialAd: InterstitialAd? = null

    companion object {
        var IS_INTERSTIAL_AD_SHOWING = false

    }

    fun loadInterStialAd(
        context: Context,
        addId: String,
        refcallback: (loadedOrNot: Boolean) -> Unit = {}
    ) {

        Log.e(TAG, "loadInterStialAd: " + context.applicationInfo.className)
        val adRequest = AdRequest.Builder().build()
        if (mInterstitialAd == null) {
            InterstitialAd.load(
                context,
                addId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG + "fil", "onAdFailedToLoad: InterStatil" + adError.message)
                        mInterstitialAd = null
                        refcallback.invoke(false)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(
                            "MyLogs",
                            "Banner adapter class name:" + interstitialAd.responseInfo.mediationAdapterClassName
                        )
                        mInterstitialAd = interstitialAd
                        refcallback.invoke(true)
                    }
                })
        } else {
            refcallback.invoke(false)
        }
    }

    fun show_Interstial_Ad(activity: Activity, dataListener: DataListener) {
        if (mInterstitialAd != null) {

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()


                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    mInterstitialAd = null
                    dataListener.onRecieve(false)
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    IS_INTERSTIAL_AD_SHOWING = true

                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    dataListener.onRecieve(true)

                    IS_INTERSTIAL_AD_SHOWING = false
                    mInterstitialAd = null

                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            dataListener.onRecieve(false)
        }

    }

}