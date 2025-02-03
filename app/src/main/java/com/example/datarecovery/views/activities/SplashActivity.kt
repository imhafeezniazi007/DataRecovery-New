package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivitySplashBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.RemoteConfigHelper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.tasks.Task
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.subscription.ads.billing.SubscriptionViewModel
import com.subscription.ads.billing.SubscriptionsConstants
import java.io.IOException


class SplashActivity : BaseActivity() {
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    lateinit var binding: ActivitySplashBinding
    private var mLastClickTime: Long = 0
    private var secondsRemaining: Long = 0L
    private val COUNTER_TIME = 5L
    lateinit var billingClient: BillingClient
    private var retryAttempt = 0.0
    var interstitialAdd: InterstitialAd? = null

    val TAG = SplashActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setmTheme()

        RemoteConfigHelper.init(this)

        binding.btn.setOnClickListener {

            subscriptionViewModel.isUserSubscribed.observe(this@SplashActivity) {
                AppPreferences.getInstance(this@SplashActivity).setAppPurchased(it)
                SubscriptionsConstants.isUserSubscribed = it
                if (!AppPreferences.getInstance(this@SplashActivity).isAppPurchased) {
                    binding.adloadingscreen.visibility = View.VISIBLE
                    showAdmobInterstitial(object : DataListener {
                        override fun onRecieve(any: Any) {
                            if (any as Boolean) {
                                binding.adloadingscreen.visibility = View.GONE
                                nextScreen()
                            } else {
                                binding.adloadingscreen.visibility = View.GONE
                                nextScreen()
                            }
                        }
                    })

                } else {
                    if (AppPreferences.getInstance(this@SplashActivity).isFirstTimeUser) {
                        startActivity(Intent(this@SplashActivity, ProActivity::class.java))
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))

                    }
                }
            }


            /*if(!AppPreferences.getInstance(this@SplashActivity).isAppPurchased) {
                if (AppPreferences.getInstance(this@SplashActivity).isFirstTimeUser) {
                    startActivity(Intent(this@SplashActivity, ProActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }else{
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }*/

        }



        FirebaseInstallations.getInstance().id.addOnCompleteListener { task: Task<String?> ->
            if (task.isSuccessful) {
                val token = task.result

            }
        }
        checkSubscription()
        initAds()
    }


    override fun onResume() {
        super.onResume()

        getremoteConfigIds()
    }

    private fun getremoteConfigIds() {
        val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSetting = remoteConfigSettings {
            fetchTimeoutInSeconds = 60
            minimumFetchIntervalInSeconds = 30
        }
        remoteConfig.setConfigSettingsAsync(configSetting)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                /* Handler().postDelayed({
                     loadingProgress(false)
                     binding.btn.visibility = View.VISIBLE
                 },5000)*/
            } else {
                Log.d("remoteConfig", "remoteConfig: exception ${task.exception}")
//                loadingProgress(false)
                binding.btn.visibility = View.VISIBLE
            }

        }.addOnFailureListener {
            Log.d("remoteConfig", "remoteConfig: exception ${it}")
//            loadingProgress(false)
            binding.btn.visibility = View.VISIBLE
        }

    }

    fun setmTheme() {
        val mtheme = AppPreferences.getInstance(this).getTheme
        if (mtheme == 0) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.light_theme_bg)
        } else if (mtheme == 1) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.dark_theme_bg)
        } else if (mtheme == 2) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme1_bg)
        } else if (mtheme == 3) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme2_bg)
        } else if (mtheme == 4) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme3)
        } else if (mtheme == 5) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme4)
        } else if (mtheme == 6) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme5)
        }
    }


    fun progressBar() {
        val mProgressBar: ProgressBar
        val mCountDownTimer: CountDownTimer
        var i = 0

        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mProgressBar.setProgress(i)
        mCountDownTimer = object : CountDownTimer(5000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("Log_tag", "Tick of Progress$i$millisUntilFinished")
                i++
                mProgressBar.setProgress(i * 100 / (5000 / 100))
            }

            override fun onFinish() {
                //Do what you want
                i++
                mProgressBar.setProgress(100)
                binding.progressBar.visibility = View.GONE
                binding.btn.visibility = View.VISIBLE
            }
        }
        mCountDownTimer.start()

    }

    fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(@NonNull billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d("testOffer", list.size.toString() + " size")
                            if (list.size > 0) {
                                Log.d("TAG", "onBillingSetupFinished: activate premium feature")
                                AppPreferences.getInstance(this@SplashActivity)
                                    .setAppPurchased(true)

//                                prefs.setPremium(1) // set 1 to activate premium feature
                                var i = 0
                                for (purchase in list) {
                                    //Here you can manage each product, if you have multiple subscription
                                    Log.d(
                                        "testOffer",
                                        purchase.originalJson
                                    ) // Get to see the order information
                                    Log.d("testOffer", " index$i")
                                    i++
                                }
                            } else {
                                Log.d("TAG", "onBillingSetupFinished:de-activate premium feature")
                                AppPreferences.getInstance(this@SplashActivity)
                                    .setAppPurchased(false)
//                                prefs.setPremium(0) // set 0 to de-activate premium feature
                            }
                        }
                    }
                }
            }
        })
    }

    fun nextScreen() {
        if (AppPreferences.getInstance(this@SplashActivity).isFirstTimeUser) {
            startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))

        } else {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))

        }
    }

    fun initAds() {
        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this@SplashActivity,
            AdIds.admobInterstitialId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAdd = interstitialAd
                    Log.d("InterstitialAd", "onAdLoaded: ${interstitialAd.adUnitId}")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)

                    Log.d("InterstitialAd", "onAdFailedToLoad: ${loadAdError.message}")
                }
            })
    }

    fun showAdmobInterstitial(dataListener: DataListener) {
        if (interstitialAdd != null) {
            interstitialAdd!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError!!)
                        dataListener.onRecieve(false)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        //                    completeListener.onInterstitialDismissed(true);
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        interstitialAdd = null
                        dataListener.onRecieve(true)
                        initAds()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }
                }
            interstitialAdd!!.show(this@SplashActivity)
        } else {
            dataListener.onRecieve(false)
        }
    }

    override fun onBackPressed() {
        exitDialog()
    }

    fun exitDialog() {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@SplashActivity)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.exit_dialog, null)
        builder.setView(view)
        imageDialog = builder.create()
        val tapAgainLayout = view.findViewById<ConstraintLayout>(R.id.tapAgainLayout)


        tapAgainLayout.setOnClickListener {
            finishAffinity()
        }
        imageDialog.window!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

        val window: Window = imageDialog.window!!
        val wlp: WindowManager.LayoutParams = window.attributes

        wlp.gravity = Gravity.BOTTOM
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
        if (!this@SplashActivity.isFinishing) {
            imageDialog.show()
        }

    }

}