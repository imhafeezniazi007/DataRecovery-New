package com.subscription.ads.billing

import androidx.annotation.Keep
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

@Keep
data class MainState(
    val hasRenewableMonthly: Boolean? = false,
//    val hasRenewableWeekly: Boolean? = false,
    val basicProductDetails: List<ProductDetails>? = null,
    val purchases: List<Purchase>? = null,
)