package com.legal.transcriber.subscription

import android.app.Activity
import android.app.Application
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SubscriptionManager {

    private const val ENTITLEMENT_ID = "pro"
    private const val API_KEY = "goog_YOUR_REVENUECAT_ANDROID_KEY"

    private var viewModel: TranscriptionViewModel? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentActivity: Activity? = null

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    fun configure(application: Application, viewModel: TranscriptionViewModel?) {
        this.viewModel = viewModel
        Purchases.configure(
            PurchasesConfiguration.Builder(application, API_KEY)
                .build()
        )
        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
            val wasPro = _isPro.value
            val nowPro = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
            _isPro.value = nowPro
            if (nowPro != wasPro) {
                syncToBackend(nowPro)
            }
        }
        checkSubscriptionStatus()
        fetchOfferings()
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    fun checkSubscriptionStatus() {
        scope.launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
                val wasPro = _isPro.value
                val nowPro = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPro.value = nowPro
                if (nowPro != wasPro) {
                    syncToBackend(nowPro)
                }
            } catch (_: Exception) {
                _isPro.value = false
            }
        }
    }

    fun fetchOfferings() {
        scope.launch {
            try {
                _offerings.value = Purchases.sharedInstance.awaitOfferings()
            } catch (_: Exception) {
            }
        }
    }

    fun purchase(pkg: Package, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val activity = currentActivity ?: run {
            onError("No activity available")
            return
        }
        _purchaseError.value = null
        _isLoading.value = true
        scope.launch {
            try {
                val params = PurchaseParams.Builder(activity, pkg).build()
                val result = Purchases.sharedInstance.awaitPurchase(params)
                _isLoading.value = false
                val isActive = result.customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPro.value = isActive
                if (isActive) {
                    syncToBackend(true)
                    onSuccess()
                } else {
                    onError("Purchase did not activate pro entitlement")
                }
            } catch (e: com.revenuecat.purchases.PurchasesTransactionException) {
                _isLoading.value = false
                if (!e.userCancelled) {
                    val msg = e.error?.message ?: "Purchase failed"
                    _purchaseError.value = msg
                    onError(msg)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Purchase failed"
                _purchaseError.value = msg
                onError(msg)
            }
        }
    }

    fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        scope.launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitRestore()
                _isLoading.value = false
                val isActive = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPro.value = isActive
                if (isActive) {
                    syncToBackend(true)
                    onSuccess()
                } else {
                    onError("No active subscription found")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Restore failed"
                _purchaseError.value = msg
                onError(msg)
            }
        }
    }

    private fun syncToBackend(isPro: Boolean) {
        val tier = if (isPro) "pro" else "free"
        viewModel?.updateSubscriptionTier(tier)
    }
}
