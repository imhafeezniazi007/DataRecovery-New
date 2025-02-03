package com.example.datarecovery.utils

import com.example.datarecovery.BuildConfig

object AdIds {
    private const val admobBannerId = "ca-app-pub-8431988213576616/7315803720"
    private const val admobBannerTestId = "ca-app-pub-3940256099942544/6300978111"
    private const val admobInterstitialId = "ca-app-pub-8431988213576616/3655760314"
    private const val admobInterstitialTestId = "ca-app-pub-3940256099942544/1033173712"

    fun admobBannerId(): String {
        return if (BuildConfig.DEBUG) {
            admobBannerTestId
        } else admobBannerId
    }

    fun admobInterstitialId(): String {
        return if (BuildConfig.DEBUG) {
            admobInterstitialTestId
        } else admobInterstitialId
    }
}