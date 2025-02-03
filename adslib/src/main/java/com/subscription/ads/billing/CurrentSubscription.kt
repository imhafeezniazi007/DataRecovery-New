package com.subscription.ads.billing

import com.android.billingclient.api.ProductDetails

data class CurrentSubscription(var isRenewable : Boolean? , var productDetail : ProductDetails)