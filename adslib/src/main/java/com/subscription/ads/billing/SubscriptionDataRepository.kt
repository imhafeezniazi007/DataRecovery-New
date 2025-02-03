
package com.subscription.ads.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.subscription.ads.billing.SubscriptionsConstants.MONTHLY_SUB_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * The [SubscriptionDataRepository] processes and tranforms the [StateFlow] data received from
 * the [BillingClientWrapper] into [Flow] data available to the viewModel.
 *
 */
class SubscriptionDataRepository(billingClientWrapper: BillingClientWrapper) {

    // Set to true when a returned purchase is an auto-renewing basic subscription.
    val hasRenewableMonthly: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList ->
        purchaseList.any { purchase ->
            purchase.products.contains(MONTHLY_SUB_ID) && purchase.isAutoRenewing
        }
    }

//
//    // Set to true when a returned purchases is an auto-renewing premium subscription.
//    val hasRenewableWeekly: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList ->
//        purchaseList.any { purchase ->
//            purchase.products.contains(WEEKLY_SUB_ID) && purchase.isAutoRenewing
//        }
//    }
//


    // ProductDetails for the basic subscription.
    val basicProductDetails: Flow<List<ProductDetails>> = billingClientWrapper.productWithProductDetails

    // List of current purchases returned by the Google PLay Billing client library.
    val purchases: Flow<List<Purchase>> = billingClientWrapper.purchases

    // Set to true when a purchase is acknowledged.
    val isNewPurchaseAcknowledged: Flow<Boolean> = billingClientWrapper.isNewPurchaseAcknowledged

}
