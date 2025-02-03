package com.example.datarecovery.utils

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.isGone
import com.example.datarecovery.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView


class AdmobNativeAd {

    var haveNativeAd: NativeAd? = null
    private val TAG = "AdmobAdsNative"


    fun loadNativeAd(
        activity: Activity, adRefLayout: FrameLayout, smallnative: Boolean = false,
        nativeAdId: String,/* nativeAdIdIntKey: Int,*/
        adCallBack: ((NativeAd?) -> Unit)? = null

    ) {

        val builder = AdLoader.Builder(activity, nativeAdId)
        //  Log.e(TAG + "Nafil", "SendLoadRequest: NativeAD")

        Log.e(TAG + "Nafil", "loadNativeAd:Send Request ")
        builder.forNativeAd { nativeAd ->
            Log.e(TAG + "Nafil", "AdLoded: forNativeAd Update UI")
            if (activity.isDestroyed)
                nativeAd.destroy()
            var id = R.layout.large_native_ad_screen
            if (smallnative) {
                id = R.layout.small_native_ad_screen_tranbg
            }
            val adView = activity.layoutInflater
                .inflate(id, null) as NativeAdView
            adRefLayout.removeAllViews()
            adRefLayout.addView(adView)
            haveNativeAd = nativeAd
            populateNativeAdView(nativeAd, adView)
            adCallBack?.invoke(nativeAd)
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e(TAG + "Nafil", "AdLoded: NativeAD")
                Log.e(TAG, "onAdLoaded:Native ")
                adCallBack?.invoke(haveNativeAd)

            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                adRefLayout.removeAllViews()
                adRefLayout.isGone = true
                Log.e(TAG + "Nafil", "onAdFailedToLoad: NativeAD")
                Log.e(TAG, "onAdFailedToLoad: NativeAd" + loadAdError.message)
                adCallBack?.invoke(null)
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }


    fun loadNativeAdMedium(
        activity: Activity, adRefLayout: FrameLayout,
        nativeAdId: String,
        adCallBack: ((NativeAd?) -> Unit)? = null

    ) {

        val builder = AdLoader.Builder(activity, nativeAdId)

        Log.e(TAG + "Nafil", "loadNativeAd:Send Request ")
        builder.forNativeAd { nativeAd ->
            Log.e(TAG + "Nafil", "AdLoded: forNativeAd Update UI")
            if (activity.isDestroyed)
                nativeAd.destroy()
            val id = R.layout.medium_native_ad_screen
            val refview = activity.layoutInflater
                .inflate(id, null)

            val adView = refview.findViewById<NativeAdView>(R.id.nativeadref)
            adRefLayout.removeAllViews()
            adRefLayout.addView(refview)
            haveNativeAd = nativeAd
            populateNativeAdView(nativeAd, adView)
            adCallBack?.invoke(nativeAd)
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e(TAG + "Nafil", "AdLoded: NativeAD")
                Log.e(TAG, "onAdLoaded:Native ")
                adCallBack?.invoke(haveNativeAd)

            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                adRefLayout.removeAllViews()
                adRefLayout.isGone = true
                Log.e(TAG + "Nafil", "onAdFailedToLoad: NativeAD")
                Log.e(TAG, "onAdFailedToLoad: NativeAd" + loadAdError.message)
                adCallBack?.invoke(null)
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadPreExisingAd(
        activity: Activity, adRefLayout: FrameLayout,
        layoutid: Int, adCallBack: ((NativeAd) -> Unit)? = null
    ) {
        Log.e(TAG + "Nafil", "loadNativeAd:Send Request From Cache ")
        Log.e(TAG, "loadNativeAd:Send Request From Cache ")
        if (activity.isDestroyed)
            haveNativeAd?.destroy()
//        val id = R.layout.large_native_ad_screen

        val adView = activity.layoutInflater
            .inflate(layoutid, null) as NativeAdView
        adRefLayout.removeAllViews()
        haveNativeAd?.let {
            adRefLayout.addView(adView)
            populateNativeAdView(it, adView)
        }

    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        var isMediViewpresent = true
        try {
            adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
            if (adView.mediaView == null) isMediViewpresent = false
        } catch (e: Exception) {
            isMediViewpresent = false
        }

        // Set other ad assets.
        adView.headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        if (isMediViewpresent)
            adView.mediaView?.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        // adView.bodyView.visibility = View.INVISIBLE
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            when (adView.callToActionView) {
                is TextView -> {
                    (adView.callToActionView as TextView).text = nativeAd.callToAction
                }

                is Button -> {
                    (adView.callToActionView as Button).text = nativeAd.callToAction
                }
            }
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        if (nativeAd.mediaContent != null) {
            val vc = nativeAd.mediaContent?.videoController
            // Updates the UI to say whether or not this ad has a video asset.
            if (vc?.hasVideoContent() == true) {
                // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                // VideoController will call methods on this object when events occur in the video
                // lifecycle.
                vc?.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                }
            } else {

            }
        }
    }


}