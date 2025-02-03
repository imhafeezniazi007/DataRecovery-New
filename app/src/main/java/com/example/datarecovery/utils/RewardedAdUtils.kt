package com.example.datarecovery.utils

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import com.example.datarecovery.interfaces.DataListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class RewardedAdUtils(var activity: Activity, private var adsId:String, var dataListener: DataListener) :OnUserEarnedRewardListener{
    var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private val TAG = RewardedAdUtils::class.java.simpleName
    private var mLastClickTime = 0L

    fun loadAd(){
        RewardedInterstitialAd.load(activity, adsId,
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    Constant.adsAlreadyShowing = true
                    dataListener.onClickWatchAd(true)
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.")

                            }

                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.")
                                rewardedInterstitialAd = null

                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.")
                                rewardedInterstitialAd = null

                            }

                            override fun onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.")

                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.")
                            }
                        }
                    rewardedInterstitialAd!!.show(
                        activity,
                        this@RewardedAdUtils

                    )

                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG,"onAdFailedToLoad"+ adError.toString())
                    rewardedInterstitialAd = null
                    dataListener.onRecieve(true)

                }
            })
    }

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        Log.d(TAG, "User earned reward.")
        // Handle the reward.
        val rewardAmount = rewardItem.amount
        val rewardType = rewardItem.type
        Log.d(TAG, "User earned the reward.")
        Constant.adsAlreadyShowing = false
        dataListener.onClick(true)
    }
    fun isMultipleCall(): Boolean {
        return if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            true
        } else {
            mLastClickTime = SystemClock.elapsedRealtime()
            false
        }
    }
}
