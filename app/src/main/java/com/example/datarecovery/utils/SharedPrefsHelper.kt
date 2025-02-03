package com.example.datarecovery.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)


    companion object {
        private const val PREFERENCES_FILE_NAME = "APP.RECOVERY.PREFERENCES"
        private const val KEY_AD_ID = "KEY_AD_ID"
        private const val KEY_IS_APP_OPEN_ENABLED = "KEY_IS_APP_OPEN_ENABLED"
        private const val KEY_APP_OPEN = "KEY_APP_OPEN"
        private const val KEY_NATIVE_LARGE = "KEY_NATIVE_LARGE"
        private const val KEY_INTERSTITIAL_HIGH = "KEY_INTERSTITIAL_HIGH"
        private const val KEY_INTERSTITIAL_LOW = "KEY_INTERSTITIAL_LOW"
        private const val KEY_IS_NATIVE_SAVED_ENABLED = "KEY_IS_NATIVE_SAVED_ENABLED"
        private const val KEY_IS_NATIVE_DUPLICATE_ENABLED = "KEY_IS_NATIVE_DUPLICATE_ENABLED"
        private const val KEY_IS_NATIVE_HOME_ENABLED = "KEY_IS_NATIVE_HOME_ENABLED"
        private const val KEY_IS_BANNER_HOME_ENABLED = "KEY_IS_BANNER_HOME_ENABLED"
        private const val KEY_IS_BANNER_USE_ENABLED = "KEY_IS_BANNER_USE_ENABLED"
        private const val KEY_IS_PREMIUM_ENABLED = "KEY_IS_PREMIUM_ENABLED"
        private const val KEY_IS_SAVED_AUDIO_NATIVE_ENABLED = "KEY_IS_SAVED_AUDIO_NATIVE_ENABLED"
        private const val KEY_IS_SAVED_FILES_NATIVE_ENABLED = "KEY_IS_SAVED_FILES_NATIVE_ENABLED"
        private const val KEY_IS_SCAN_AUDIOS_NATIVE_ENABLED = "KEY_IS_SCAN_AUDIOS_NATIVE_ENABLED"
        private const val KEY_IS_SCAN_FILES_NATIVE_ENABLED = "KEY_IS_SCAN_FILES_NATIVE_ENABLED"
        private const val KEY_IS_SAVED_IMAGES_NATIVE_ENABLED = "KEY_IS_SAVED_IMAGES_NATIVE_ENABLED"
        private const val KEY_IS_SAVED_VIDEOS_NATIVE_ENABLED = "KEY_IS_SAVED_VIDEOS_NATIVE_ENABLED"
        private const val KEY_IS_SCAN_IMAGES_NATIVE_ENABLED = "KEY_IS_SCAN_IMAGES_NATIVE_ENABLED"
        private const val KEY_IS_SCAN_VIDEOS_NATIVE_ENABLED = "KEY_IS_SCAN_VIDEOS_NATIVE_ENABLED"
        private const val KEY_IS_NATIVE_DUPLICATE_FRAGMENT_ENABLED =
            "KEY_IS_NATIVE_DUPLICATE_FRAGMENT_ENABLED"
        private const val KEY_IS_NATIVE_SAVED_FRAGMENT_ENABLED =
            "KEY_IS_NATIVE_SAVED_FRAGMENT_ENABLED"
        private const val KEY_IS_NATIVE_DUPLICATE_DIALOG_ENABLED =
            "KEY_IS_NATIVE_DUPLICATE_DIALOG_ENABLED"
        private const val KEY_IS_NATIVE_SCANNING_DIALOG_ENABLED =
            "KEY_IS_NATIVE_SCANNING_DIALOG_ENABLED"
        private const val KEY_IS_NATIVE_EXIT_ENABLED =
            "KEY_IS_NATIVE_EXIT_ENABLED"
        private const val KEY_IS_NATIVE_USE_ENABLED =
            "KEY_IS_NATIVE_USE_ENABLED"
        private const val KEY_IS_BANNER_SIDE_NAV_ENABLED =
            "KEY_IS_BANNER_SIDE_NAV_ENABLED"
        private const val KEY_IS_PREMIUM_HOW_TO_USE_ENABLED =
            "KEY_IS_PREMIUM_HOW_TO_USE_ENABLED"
        private const val KEY_IS_PREMIUM_MONTHLY_ENABLED =
            "KEY_IS_PREMIUM_MONTHLY_ENABLED"
        private const val KEY_IS_SCAN_FILES_INTERSTITIAL_ENABLED =
            "KEY_IS_SCAN_FILES_INTERSTITIAL_ENABLED"
        private const val KEY_IS_DUPLICATE_ACTIVITY_INTERSTITIAL_ENABLED =
            "KEY_IS_DUPLICATE_ACTIVITY_INTERSTITIAL_ENABLED"
        private const val KEY_IS_SCAN_AUDIOS_ACTIVITY_INTERSTITIAL_ENABLED =
            "KEY_IS_SCAN_AUDIOS_ACTIVITY_INTERSTITIAL_ENABLED"
        private const val KEY_IS_SCAN_IMAGES_ACTIVITY_INTERSTITIAL_ENABLED =
            "KEY_IS_SCAN_IMAGES_ACTIVITY_INTERSTITIAL_ENABLED"
        private const val KEY_IS_SCAN_VIDEOS_ACTIVITY_INTERSTITIAL_ENABLED =
            "KEY_IS_SCAN_VIDEOS_ACTIVITY_INTERSTITIAL_ENABLED"
        private const val KEY_IS_DUPLICATE_FRAGMENT_INTERSTITIAL_ENABLED =
            "KEY_IS_DUPLICATE_FRAGMENT_INTERSTITIAL_ENABLED"
        private const val KEY_IS_HOME_FRAGMENT_INTERSTITIAL_ENABLED =
            "KEY_IS_HOME_FRAGMENT_INTERSTITIAL_ENABLED"
        private const val KEY_IS_SAVED_FRAGMENT_INTERSTITIAL_ENABLED =
            "KEY_IS_SAVED_FRAGMENT_INTERSTITIAL_ENABLED"
    }

    fun setInterstitialMediumId(key: String) {
        sharedPreferences.edit().putString(KEY_INTERSTITIAL_LOW, key).apply()
    }

    fun getInterstitialMediumId(): String {
        return sharedPreferences.getString(KEY_INTERSTITIAL_LOW, "") ?: ""
    }

    fun setInterstitialHighId(key: String) {
        sharedPreferences.edit().putString(KEY_INTERSTITIAL_HIGH, key).apply()
    }

    fun getInterstitialHighId(): String {
        return sharedPreferences.getString(KEY_INTERSTITIAL_HIGH, "") ?: ""
    }

    fun setNativeLargeId(key: String) {
        sharedPreferences.edit().putString(KEY_NATIVE_LARGE, key).apply()
    }

    fun getNativeLargeId(): String {
        return sharedPreferences.getString(KEY_NATIVE_LARGE, "") ?: ""
    }

    fun setAppOpenAdId(key: String) {
        sharedPreferences.edit().putString(KEY_APP_OPEN, key).apply()
    }

    fun getAppOpenAdId(): String {
        return sharedPreferences.getString(KEY_APP_OPEN, "") ?: ""
    }

    fun setAdId(key: String) {
        sharedPreferences.edit().putString(KEY_AD_ID, key).apply()
    }

    fun getAdId(): String {
        return sharedPreferences.getString(KEY_AD_ID, "") ?: ""
    }

    fun setIsNativeSavedEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_SAVED_ENABLED, value).apply()
    }

    fun getIsNativeSavedEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_SAVED_ENABLED, true)
    }

    fun setIsNativeDuplicateEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_DUPLICATE_ENABLED, value).apply()
    }

    fun getIsNativeDuplicateEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_DUPLICATE_ENABLED, true)
    }

    fun setIsNativeHomeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_HOME_ENABLED, value).apply()
    }

    fun getIsNativeHomeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_HOME_ENABLED, true)
    }

    fun setIsNativeExitEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_EXIT_ENABLED, value).apply()
    }

    fun getIsNativeExitEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_EXIT_ENABLED, true)
    }

    fun setIsBannerHomeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_BANNER_HOME_ENABLED, value).apply()
    }

    fun getIsBannerHomeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BANNER_HOME_ENABLED, true)
    }

    fun setIsBannerUseEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_BANNER_USE_ENABLED, value).apply()
    }

    fun getIsBannerUseEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BANNER_USE_ENABLED, true)
    }

    fun setIsPremiumEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM_ENABLED, value).apply()
    }

    fun getIsPremiumEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM_ENABLED, true)
    }

    // New methods
    fun setIsSavedAudioNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SAVED_AUDIO_NATIVE_ENABLED, value).apply()
    }

    fun getIsSavedAudioNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SAVED_AUDIO_NATIVE_ENABLED, true)
    }

    fun setIsSavedFilesNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SAVED_FILES_NATIVE_ENABLED, value).apply()
    }

    fun getIsSavedFilesNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SAVED_FILES_NATIVE_ENABLED, true)
    }

    fun setIsScanAudiosNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_AUDIOS_NATIVE_ENABLED, value).apply()
    }

    fun getIsScanAudiosNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_AUDIOS_NATIVE_ENABLED, true)
    }

    fun setIsScanFilesNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_FILES_NATIVE_ENABLED, value).apply()
    }

    fun getIsScanFilesNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_FILES_NATIVE_ENABLED, true)
    }

    fun setIsSavedImagesNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SAVED_IMAGES_NATIVE_ENABLED, value).apply()
    }

    fun getIsSavedImagesNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SAVED_IMAGES_NATIVE_ENABLED, true)
    }

    fun setIsSavedVideosNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SAVED_VIDEOS_NATIVE_ENABLED, value).apply()
    }

    fun getIsSavedVideosNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SAVED_VIDEOS_NATIVE_ENABLED, true)
    }

    fun setIsScanImagesNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_IMAGES_NATIVE_ENABLED, value).apply()
    }

    fun getIsScanImagesNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_IMAGES_NATIVE_ENABLED, true)
    }

    fun setIsScanVideosNativeEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_VIDEOS_NATIVE_ENABLED, value).apply()
    }

    fun getIsScanVideosNativeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_VIDEOS_NATIVE_ENABLED, true)
    }

    fun setIsAppOpenEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_APP_OPEN_ENABLED, value).apply()
    }

    fun getIsAppOpenEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_APP_OPEN_ENABLED, true)
    }

    fun setNativeDuplicateFragmentEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_DUPLICATE_FRAGMENT_ENABLED, value).apply()
    }

    fun getNativeDuplicateFragmentEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_DUPLICATE_FRAGMENT_ENABLED, true)
    }

    fun setNativeSavedFragmentEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_SAVED_FRAGMENT_ENABLED, value).apply()
    }

    fun getNativeSavedFragmentEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_SAVED_FRAGMENT_ENABLED, true)
    }

    fun setIsNativeDuplicateDialogEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_DUPLICATE_DIALOG_ENABLED, value).apply()
    }

    fun getIsNativeDuplicateDialogEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_DUPLICATE_DIALOG_ENABLED, true)
    }

    fun setIsNativeScanningDialogEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_SCANNING_DIALOG_ENABLED, value).apply()
    }

    fun getIsNativeScanningDialogEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_SCANNING_DIALOG_ENABLED, true)
    }


    fun setIsNativeUseEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_NATIVE_USE_ENABLED, value).apply()
    }

    fun getIsNativeUseEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_NATIVE_USE_ENABLED, true)
    }


    fun setIsBannerSideNavEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_BANNER_SIDE_NAV_ENABLED, value).apply()
    }

    fun getIsBannerSideNavEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BANNER_SIDE_NAV_ENABLED, true)
    }

    fun setIsPremiumHowToUseEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM_HOW_TO_USE_ENABLED, value).apply()
    }

    fun getIsPremiumHowToUseEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM_HOW_TO_USE_ENABLED, true)
    }

    fun setIsPremiumMonthlyEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM_MONTHLY_ENABLED, value).apply()
    }

    fun getIsPremiumMonthlyEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM_MONTHLY_ENABLED, true)
    }

    fun setIsScanFilesInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_FILES_INTERSTITIAL_ENABLED, value).apply()
    }

    fun getIsScanFilesInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_FILES_INTERSTITIAL_ENABLED, true)
    }

    fun setIsDuplicateActivityInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DUPLICATE_ACTIVITY_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsDuplicateActivityInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DUPLICATE_ACTIVITY_INTERSTITIAL_ENABLED, true)
    }

    fun setIsScanAudiosActivityInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_AUDIOS_ACTIVITY_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsScanAudiosActivityInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_AUDIOS_ACTIVITY_INTERSTITIAL_ENABLED, true)
    }

    fun setIsScanImagesActivityInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_IMAGES_ACTIVITY_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsScanImagesActivityInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_IMAGES_ACTIVITY_INTERSTITIAL_ENABLED, true)
    }

    fun setIsScanVideosActivityInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SCAN_VIDEOS_ACTIVITY_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsScanVideosActivityInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SCAN_VIDEOS_ACTIVITY_INTERSTITIAL_ENABLED, true)
    }

    fun setIsDuplicateFragmentInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_DUPLICATE_FRAGMENT_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsDuplicateFragmentInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DUPLICATE_FRAGMENT_INTERSTITIAL_ENABLED, true)
    }

    fun setIsHomeFragmentInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_HOME_FRAGMENT_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsHomeFragmentInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_HOME_FRAGMENT_INTERSTITIAL_ENABLED, true)
    }

    fun setIsSavedFragmentInterstitialEnabled(value: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_SAVED_FRAGMENT_INTERSTITIAL_ENABLED, value)
            .apply()
    }

    fun getIsSavedFragmentInterstitialEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_SAVED_FRAGMENT_INTERSTITIAL_ENABLED, true)
    }

}