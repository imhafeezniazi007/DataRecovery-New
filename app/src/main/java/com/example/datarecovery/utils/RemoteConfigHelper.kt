package com.example.datarecovery.utils

import android.content.Context
import com.example.datarecovery.BuildConfig
import com.example.datarecovery.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigHelper {

    fun init(context: Context) {
        val sharedPref by lazy { SharedPrefsHelper(context) }
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val res =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else remoteConfig.getString(
                        "rewardedAdId"
                    )
                val keyAppOpen =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else remoteConfig.getString(
                        "appOpenAdId"
                    )
                val keyNativeLarge =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else remoteConfig.getString(
                        "keyNativeLarge"
                    )
                val interstitialHighRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialHighRate"
                    )
                val interstitialMediumRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialMediumRate"
                    )


                val resNtvEn = remoteConfig.getBoolean("isNativeSavedEnabled")
                val resNtvDup = remoteConfig.getBoolean("isNativeDuplicateEnabled")
                val resHme = remoteConfig.getBoolean("isHomeNativeEnabled")
                val resHmeBnr = remoteConfig.getBoolean("isHomeBannerEnabled")
                val useBnr = remoteConfig.getBoolean("isBannerUseEnabled")
                val showPremium = remoteConfig.getBoolean("showPremium")
                val isSavedAudioNativeEnabled = remoteConfig.getBoolean("isSavedAudioNativeEnabled")
                val isSavedFilesNativeEnabled = remoteConfig.getBoolean("isSavedFilesNativeEnabled")
                val isScanAudiosNativeEnabled = remoteConfig.getBoolean("isScanAudiosNativeEnabled")
                val isScanFilesNativeEnabled = remoteConfig.getBoolean("isScanFilesNativeEnabled")
                val isSavedImagesNativeEnabled =
                    remoteConfig.getBoolean("isSavedImagesNativeEnabled")
                val isSavedVideosNativeEnabled =
                    remoteConfig.getBoolean("isSavedVideosNativeEnabled")
                val isScanImagesNativeEnabled = remoteConfig.getBoolean("isScanImagesNativeEnabled")
                val isScanVideosNativeEnabled = remoteConfig.getBoolean("isScanVideosNativeEnabled")
                val isAppOpenEnabled = remoteConfig.getBoolean("isAppOpenEnabled")
                val isNativeDuplicateFragmentEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateFragmentEnabled")
                val isNativeSavedFragmentEnabled =
                    remoteConfig.getBoolean("isNativeSavedFragmentEnabled")
                val isNativeDuplicateDialogEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateDialogEnabled")
                val isNativeScanningDialogEnabled =
                    remoteConfig.getBoolean("isNativeScanningDialogEnabled")
                val isNativeExitEnabled =
                    remoteConfig.getBoolean("isNativeExitEnabled")
                val isNativeUseEnabled =
                    remoteConfig.getBoolean("isNativeUseEnabled")
                val isBannerSideNavEnabled =
                    remoteConfig.getBoolean("isBannerSideNavEnabled")
                val isPremiumHowToUseEnabled =
                    remoteConfig.getBoolean("isPremiumHowToUseEnabled")
                val isPremiumMonthlyEnabled =
                    remoteConfig.getBoolean("isPremiumMonthlyEnabled")
                val isScanFilesInterstitialEnabled =
                    remoteConfig.getBoolean("isScanFilesInterstitialEnabled")
                val isDuplicateActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateActivityInterstitialEnabled")
                val isScanAudiosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanAudiosActivityInterstitialEnabled")
                val isScanImagesActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanImagesActivityInterstitialEnabled")
                val isScanVideosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanVideosActivityInterstitialEnabled")
                val isDuplicateFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateFragmentInterstitialEnabled")
                val isHomeFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isHomeFragmentInterstitialEnabled")
                val isSavedFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isSavedFragmentInterstitialEnabled")

                sharedPref.setAdId(res)
                sharedPref.setIsNativeSavedEnabled(resNtvEn)
                sharedPref.setIsNativeDuplicateEnabled(resNtvDup)
                sharedPref.setIsNativeHomeEnabled(resHme)
                sharedPref.setIsBannerHomeEnabled(resHmeBnr)
                sharedPref.setIsBannerUseEnabled(useBnr)
                sharedPref.setIsPremiumEnabled(showPremium)
                sharedPref.setIsSavedAudioNativeEnabled(isSavedAudioNativeEnabled)
                sharedPref.setIsSavedFilesNativeEnabled(isSavedFilesNativeEnabled)
                sharedPref.setIsScanAudiosNativeEnabled(isScanAudiosNativeEnabled)
                sharedPref.setIsScanFilesNativeEnabled(isScanFilesNativeEnabled)
                sharedPref.setIsSavedImagesNativeEnabled(isSavedImagesNativeEnabled)
                sharedPref.setIsSavedVideosNativeEnabled(isSavedVideosNativeEnabled)
                sharedPref.setIsScanImagesNativeEnabled(isScanImagesNativeEnabled)
                sharedPref.setIsScanVideosNativeEnabled(isScanVideosNativeEnabled)
                sharedPref.setIsAppOpenEnabled(isAppOpenEnabled)
                sharedPref.setAppOpenAdId(keyAppOpen)
                sharedPref.setNativeLargeId(keyNativeLarge)
                sharedPref.setNativeDuplicateFragmentEnabled(isNativeDuplicateFragmentEnabled)
                sharedPref.setNativeSavedFragmentEnabled(isNativeSavedFragmentEnabled)
                sharedPref.setInterstitialHighId(interstitialHighRate)
                sharedPref.setInterstitialMediumId(interstitialMediumRate)
                sharedPref.setIsNativeDuplicateDialogEnabled(isNativeDuplicateDialogEnabled)
                sharedPref.setIsNativeScanningDialogEnabled(isNativeScanningDialogEnabled)
                sharedPref.setIsNativeExitEnabled(isNativeExitEnabled)
                sharedPref.setIsNativeUseEnabled(isNativeUseEnabled)
                sharedPref.setIsBannerSideNavEnabled(isBannerSideNavEnabled)
                sharedPref.setIsPremiumHowToUseEnabled(isPremiumHowToUseEnabled)
                sharedPref.setIsPremiumMonthlyEnabled(isPremiumMonthlyEnabled)
                sharedPref.setIsScanFilesInterstitialEnabled(isScanFilesInterstitialEnabled)
                sharedPref.setIsDuplicateActivityInterstitialEnabled(
                    isDuplicateActivityInterstitialEnabled
                )
                sharedPref.setIsScanAudiosActivityInterstitialEnabled(
                    isScanAudiosActivityInterstitialEnabled
                )
                sharedPref.setIsScanImagesActivityInterstitialEnabled(
                    isScanImagesActivityInterstitialEnabled
                )
                sharedPref.setIsScanVideosActivityInterstitialEnabled(
                    isScanVideosActivityInterstitialEnabled
                )
                sharedPref.setIsDuplicateFragmentInterstitialEnabled(
                    isDuplicateFragmentInterstitialEnabled
                )
                sharedPref.setIsHomeFragmentInterstitialEnabled(isHomeFragmentInterstitialEnabled)
                sharedPref.setIsSavedFragmentInterstitialEnabled(isSavedFragmentInterstitialEnabled)
            } else {
                remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

                val res =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else remoteConfig.getString(
                        "rewardedAdId"
                    )
                val keyAppOpen =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9257395921" else remoteConfig.getString(
                        "appOpenAdId"
                    )
                val keyNativeLarge =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else remoteConfig.getString(
                        "keyNativeLarge"
                    )
                val interstitialHighRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialHighRate"
                    )
                val interstitialMediumRate =
                    if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712\n" else remoteConfig.getString(
                        "interstitialMediumRate"
                    )


                val resNtvEn = remoteConfig.getBoolean("isNativeSavedEnabled")
                val resNtvDup = remoteConfig.getBoolean("isNativeDuplicateEnabled")
                val resHme = remoteConfig.getBoolean("isHomeNativeEnabled")
                val resHmeBnr = remoteConfig.getBoolean("isHomeBannerEnabled")
                val useBnr = remoteConfig.getBoolean("isBannerUseEnabled")
                val showPremium = remoteConfig.getBoolean("showPremium")
                val isSavedAudioNativeEnabled = remoteConfig.getBoolean("isSavedAudioNativeEnabled")
                val isSavedFilesNativeEnabled = remoteConfig.getBoolean("isSavedFilesNativeEnabled")
                val isScanAudiosNativeEnabled = remoteConfig.getBoolean("isScanAudiosNativeEnabled")
                val isScanFilesNativeEnabled = remoteConfig.getBoolean("isScanFilesNativeEnabled")
                val isSavedImagesNativeEnabled =
                    remoteConfig.getBoolean("isSavedImagesNativeEnabled")
                val isSavedVideosNativeEnabled =
                    remoteConfig.getBoolean("isSavedVideosNativeEnabled")
                val isScanImagesNativeEnabled = remoteConfig.getBoolean("isScanImagesNativeEnabled")
                val isScanVideosNativeEnabled = remoteConfig.getBoolean("isScanVideosNativeEnabled")
                val isAppOpenEnabled = remoteConfig.getBoolean("isAppOpenEnabled")
                val isNativeDuplicateFragmentEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateFragmentEnabled")
                val isNativeSavedFragmentEnabled =
                    remoteConfig.getBoolean("isNativeSavedFragmentEnabled")
                val isNativeDuplicateDialogEnabled =
                    remoteConfig.getBoolean("isNativeDuplicateDialogEnabled")
                val isNativeScanningDialogEnabled =
                    remoteConfig.getBoolean("isNativeScanningDialogEnabled")
                val isNativeExitEnabled =
                    remoteConfig.getBoolean("isNativeExitEnabled")
                val isNativeUseEnabled =
                    remoteConfig.getBoolean("isNativeUseEnabled")
                val isBannerSideNavEnabled =
                    remoteConfig.getBoolean("isBannerSideNavEnabled")
                val isPremiumHowToUseEnabled =
                    remoteConfig.getBoolean("isPremiumHowToUseEnabled")
                val isPremiumMonthlyEnabled =
                    remoteConfig.getBoolean("isPremiumMonthlyEnabled")
                val isScanFilesInterstitialEnabled =
                    remoteConfig.getBoolean("isScanFilesInterstitialEnabled")
                val isDuplicateActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateActivityInterstitialEnabled")
                val isScanAudiosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanAudiosActivityInterstitialEnabled")
                val isScanImagesActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanImagesActivityInterstitialEnabled")
                val isScanVideosActivityInterstitialEnabled =
                    remoteConfig.getBoolean("isScanVideosActivityInterstitialEnabled")
                val isDuplicateFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isDuplicateFragmentInterstitialEnabled")
                val isHomeFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isHomeFragmentInterstitialEnabled")
                val isSavedFragmentInterstitialEnabled =
                    remoteConfig.getBoolean("isSavedFragmentInterstitialEnabled")

                sharedPref.setAdId(res)
                sharedPref.setIsNativeSavedEnabled(resNtvEn)
                sharedPref.setIsNativeDuplicateEnabled(resNtvDup)
                sharedPref.setIsNativeHomeEnabled(resHme)
                sharedPref.setIsBannerHomeEnabled(resHmeBnr)
                sharedPref.setIsBannerUseEnabled(useBnr)
                sharedPref.setIsPremiumEnabled(showPremium)
                sharedPref.setIsSavedAudioNativeEnabled(isSavedAudioNativeEnabled)
                sharedPref.setIsSavedFilesNativeEnabled(isSavedFilesNativeEnabled)
                sharedPref.setIsScanAudiosNativeEnabled(isScanAudiosNativeEnabled)
                sharedPref.setIsScanFilesNativeEnabled(isScanFilesNativeEnabled)
                sharedPref.setIsSavedImagesNativeEnabled(isSavedImagesNativeEnabled)
                sharedPref.setIsSavedVideosNativeEnabled(isSavedVideosNativeEnabled)
                sharedPref.setIsScanImagesNativeEnabled(isScanImagesNativeEnabled)
                sharedPref.setIsScanVideosNativeEnabled(isScanVideosNativeEnabled)
                sharedPref.setIsAppOpenEnabled(isAppOpenEnabled)
                sharedPref.setAppOpenAdId(keyAppOpen)
                sharedPref.setNativeLargeId(keyNativeLarge)
                sharedPref.setNativeDuplicateFragmentEnabled(isNativeDuplicateFragmentEnabled)
                sharedPref.setNativeSavedFragmentEnabled(isNativeSavedFragmentEnabled)
                sharedPref.setInterstitialHighId(interstitialHighRate)
                sharedPref.setInterstitialMediumId(interstitialMediumRate)
                sharedPref.setIsNativeDuplicateDialogEnabled(isNativeDuplicateDialogEnabled)
                sharedPref.setIsNativeScanningDialogEnabled(isNativeScanningDialogEnabled)
                sharedPref.setIsNativeExitEnabled(isNativeExitEnabled)
                sharedPref.setIsNativeUseEnabled(isNativeUseEnabled)
                sharedPref.setIsBannerSideNavEnabled(isBannerSideNavEnabled)
                sharedPref.setIsPremiumHowToUseEnabled(isPremiumHowToUseEnabled)
                sharedPref.setIsPremiumMonthlyEnabled(isPremiumMonthlyEnabled)
                sharedPref.setIsScanFilesInterstitialEnabled(isScanFilesInterstitialEnabled)
                sharedPref.setIsDuplicateActivityInterstitialEnabled(
                    isDuplicateActivityInterstitialEnabled
                )
                sharedPref.setIsScanAudiosActivityInterstitialEnabled(
                    isScanAudiosActivityInterstitialEnabled
                )
                sharedPref.setIsScanImagesActivityInterstitialEnabled(
                    isScanImagesActivityInterstitialEnabled
                )
                sharedPref.setIsScanVideosActivityInterstitialEnabled(
                    isScanVideosActivityInterstitialEnabled
                )
                sharedPref.setIsDuplicateFragmentInterstitialEnabled(
                    isDuplicateFragmentInterstitialEnabled
                )
                sharedPref.setIsHomeFragmentInterstitialEnabled(isHomeFragmentInterstitialEnabled)
                sharedPref.setIsSavedFragmentInterstitialEnabled(isSavedFragmentInterstitialEnabled)
            }
        }
    }
}