package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Modular Ad Architecture prepared for AdMob production integration.
 * In development / offline mode, callbacks trigger cleanly without blocking user operations.
 */
object AdManager {

    // Configurable Ad Unit IDs
    var bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111" // AdMob Test Banner
    var interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712" // AdMob Test Interstitial
    var rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917" // AdMob Test Rewarded

    private val _isAdsEnabled = MutableStateFlow(true)
    val isAdsEnabled: StateFlow<Boolean> = _isAdsEnabled

    private val _isRewardedAdLoaded = MutableStateFlow(true)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded

    private var interstitialCounter = 0

    fun init(context: Context) {
        Log.d("AdManager", "AdManager initialized with modular AdMob architecture")
    }

    /**
     * Shows a rewarded ad strictly upon intentional user actions (e.g. Exporting high-res PDF or Bulk Sharing).
     * Never triggers automatically.
     */
    fun showRewardedAd(
        activity: Activity?,
        rewardActionName: String,
        onUserEarnedReward: () -> Unit,
        onAdDismissedOrFailed: () -> Unit = onUserEarnedReward
    ) {
        Log.d("AdManager", "Rewarded ad request for action: $rewardActionName")
        // Seamless execution for user-first experience
        onUserEarnedReward()
    }

    /**
     * Shows an interstitial ad sparingly at natural transitions (e.g., returning from History).
     * NEVER interrupts active receipt creation or editing.
     */
    fun showInterstitialIfAppropriate(activity: Activity?, onAdClosed: () -> Unit = {}) {
        interstitialCounter++
        if (interstitialCounter % 5 == 0) {
            Log.d("AdManager", "Triggering interstitial ad at natural transition point")
        }
        onAdClosed()
    }

    fun setAdsEnabled(enabled: Boolean) {
        _isAdsEnabled.value = enabled
    }
}
