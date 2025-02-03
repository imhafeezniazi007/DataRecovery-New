package com.example.datarecovery.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.android.billingclient.api.*
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityProBinding
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.collectLatestLifeCycleFlow
import com.subscription.ads.billing.SubscriptionViewModel
import com.subscription.ads.billing.SubscriptionsConstants


class ProActivity : BaseActivity() {
    lateinit var binding: ActivityProBinding
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    val TAG = ProActivity::class.java.simpleName

    private var productDetail: ProductDetails? = null

    var from: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setmTheme()

        from = intent.getStringExtra("from")

        binding.btn.setOnClickListener {
            productDetail?.let {
                subscriptionViewModel.buy(
                    it,
                    null,
                    this,
                    it.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                )
            }
        }
        binding.cancelIV.setOnClickListener {
            if (from.equals("recover")) {
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else if (from.equals("main")) {
                finish()
            } else {
                startActivity(Intent(this@ProActivity, MainActivity::class.java))
                finish()
            }
        }

        observerViewModel()
    }


    private fun observerViewModel() {
        subscriptionViewModel.billingConnectionState.observe(this) {
            Log.d(TAG, "observerViewModel: billing client status $it")
//            showHideProgress(it)
        }

        collectLatestLifeCycleFlow(subscriptionViewModel.productsForSaleFlows) {
            Log.d(TAG, "observerViewModel: productsForSaleFlows $it")
            setUpSubscriptionPrice(it)
        }

        collectLatestLifeCycleFlow(subscriptionViewModel.currentPurchasesForPurchaseFlow) {
            Log.d(TAG, "observerViewModel: currentPurchase $it")

        }

        subscriptionViewModel.isUserSubscribed.observe(this) {
            AppPreferences.getInstance(this).setAppPurchased(it)
            SubscriptionsConstants.isUserSubscribed = it
        }
        collectLatestLifeCycleFlow(subscriptionViewModel.isNewPurchaseAcknowledged) {
            if (it) {
                if (from.equals("recover")) {
                    val intent = Intent()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else if (from.equals("main")) {
                    finish()
                } else {
                    startActivity(Intent(this@ProActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun setUpSubscriptionPrice(it: List<ProductDetails>) {
        var price = ""
        it.firstOrNull()?.let { item ->
            productDetail = item
            item.subscriptionOfferDetails?.get(0).let { sod ->
                price = sod?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice ?: ""
            }
        }
        binding.tv1111.text = "$price/Month"

    }


    fun setmTheme() {
        var mtheme = AppPreferences.getInstance(this).getTheme
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

}