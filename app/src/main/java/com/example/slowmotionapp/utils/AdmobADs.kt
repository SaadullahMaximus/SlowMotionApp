package com.example.slowmotionapp.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.slowmotionapp.R
import com.google.android.exoplayer2.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdmobADs {

    private const val TAG = "InterstitialAd"

    fun Context.loadInterstitialAd3(wantToShowAD: Boolean, adID: String, listener: () -> Unit) {

        if (wantToShowAD && adID.isNotEmpty() && isNetworkAvailable()) {

            val progressDialog = ProgressDialog(this, R.style.CustomDialog)
            progressDialog.setMessage("Ad is Loading")
            progressDialog.setCancelable(false)
            progressDialog.isIndeterminate = false
            progressDialog.show()

            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                this,
                adID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.toString())
                        progressDialog.dismiss()
                        listener.invoke()
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        showInterstitialAd3(
                            interstitialAd,
                            listener,
                            progressDialog,
                            this@loadInterstitialAd3
                        )
                    }
                })
        } else {
            listener.invoke()
        }

    }

    fun showInterstitialAd3(
        ad: InterstitialAd,
        listener: () -> Unit,
        progressDialog: ProgressDialog,
        context: Context
    ) {

        ad.show(context as Activity)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                progressDialog.dismiss()
                listener.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                progressDialog.dismiss()
                listener.invoke()
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }

    fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }
}