package com.example.datarecovery.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.datarecovery.AppController
import com.example.datarecovery.databinding.FragmentSavedBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.SharedPrefsHelper
import com.example.datarecovery.views.activities.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class SavedFragment : Fragment() {
    lateinit var binding: FragmentSavedBinding
    var mContext: Context? = null
    var mIntent: Intent? = null
    private var retryAttempt = 0.0
    var interstitialAdd: InterstitialAd? = null
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSavedBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (isPremiumEnabled()) {
            binding.premiumLayout.layout.visibility = View.VISIBLE
        }
        if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
            if (isNativeSavedEnabled()) {
                if (isPremiumEnabled()) {
                    showNativeAd()
                } else {
                    Log.d("de_atag", "onViewCreated: correct")
                    showNativeLargeAd()
                }
            } else {
                binding.frameNative.visibility = View.GONE
            }
        } else {
            binding.frameNative.visibility = View.GONE
        }
        clickListeners()
    }

    private fun isPremiumEnabled(): Boolean {
        return sharedPrefsHelper.getIsPremiumEnabled()
    }

    private fun showNativeLargeAd() {
        val adId = sharedPrefsHelper.getNativeLargeId()
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.frameNative,
                false,
                adId
            ) { it ->
                Log.d("de_atag", it.toString())
            }
        }
    }

    private fun isNativeSavedEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeSavedEnabled()
    }

    private fun showNativeAd() {
        val adId = sharedPrefsHelper.getNativeLargeId()
        if (adId.isNotEmpty()) {
            AppController.nativeAdRef.loadNativeAd(
                requireActivity(),
                binding.frameNative,
                true,
                adId
            ) {

            }
        }
    }

    private fun showHighECPM(intent: Activity) {
        Log.d("inter_ecpm", "showHighECPM: ")
        val adId = sharedPrefsHelper.getInterstitialHighId()

        binding.adloadingscreen.visibility = View.VISIBLE
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) { isLoaded ->
            if (isLoaded) {
                AppController.splshinterstialAd.show_Interstial_Ad(
                    requireActivity(),
                    object : DataListener {
                        override fun onRecieve(any: Any) {
                            if (any as Boolean) {
                                binding.adloadingscreen.visibility = View.GONE
                                nextScreen()
                            } else {
                                binding.adloadingscreen.visibility = View.GONE
                                showMediumECPM(intent)
                            }
                        }
                    })
            } else {
                showMediumECPM(intent)
            }
        }
    }

    private fun showMediumECPM(intent: Activity) {
        Log.d("inter_ecpm", "showMediumECPM: ")

        val adId = sharedPrefsHelper.getInterstitialMediumId()

        binding.adloadingscreen.visibility = View.VISIBLE
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) { isLoaded ->
            if (isLoaded) {
                AppController.splshinterstialAd.show_Interstial_Ad(requireActivity(),
                    object : DataListener {
                        override fun onRecieve(any: Any) {
                            if (any as Boolean) {
                                binding.adloadingscreen.visibility = View.GONE
                                nextScreen()
                            } else {
                                binding.adloadingscreen.visibility = View.GONE
                                showAutoECPM(intent)
                            }
                        }
                    })
            } else {
                showAutoECPM(intent)
            }
        }
    }

    private fun showAutoECPM(intent: Activity) {
        Log.d("inter_ecpm", "showAutoECPM: ")
        val adId = AdIds.admobInterstitialId()

        binding.adloadingscreen.visibility = View.VISIBLE
        AppController.splshinterstialAd.loadInterStialAd(
            requireContext(),
            adId
        ) {
            AppController.splshinterstialAd.show_Interstial_Ad(requireActivity(),
                object : DataListener {
                    override fun onRecieve(any: Any) {
                        if (any as Boolean) {
                            binding.adloadingscreen.visibility = View.GONE
                            nextScreen()
                        } else {
                            binding.adloadingscreen.visibility = View.GONE
                            proceed(intent)
                        }
                    }
                })
        }
    }

    private fun proceed(intent: Activity) {
        when (intent) {
            is CleanImagesActivity -> {
                val intentAct = Intent(requireActivity(), CleanImagesActivity::class.java)
                startActivity(intentAct)
            }

            is CleanVediosActivity -> {
                val intentAct = Intent(requireActivity(), CleanVediosActivity::class.java)
                startActivity(intentAct)
            }

            is CleanAudiosActivity -> {
                val intentAct = Intent(requireActivity(), CleanAudiosActivity::class.java)
                startActivity(intentAct)
            }

            is CleanDocumentsActivity -> {
                val intentAct = Intent(requireActivity(), CleanDocumentsActivity::class.java)
                startActivity(intentAct)
            }
        }
    }


    private fun clickListeners() {
        binding.imagesCV.setOnClickListener {
            if (!AppPreferences.getInstance(requireContext()).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), CleanImagesActivity::class.java)
                showHighECPM(CleanImagesActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), CleanImagesActivity::class.java)
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), CleanImagesActivity::class.java)
                startActivity(intent)
            }

            /*Constant.clickedCount = Constant.clickedCount + 1
            if (Constant.clickedCount < 4) {
                startActivity(Intent(requireActivity(), CleanImagesActivity::class.java))
            } else {
                Constant.clickedCount = 0
                mIntent =  Intent(requireActivity(), CleanImagesActivity::class.java)
                val intent  = Intent(requireActivity(), ProActivity::class.java)
                intent.putExtra("from","recover")
                resultLauncher.launch(intent)
            }*/

        }
        binding.videosCV.setOnClickListener {

            if (!AppPreferences.getInstance(requireContext()).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), CleanVediosActivity::class.java)
                showHighECPM(CleanVediosActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), CleanVediosActivity::class.java)
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), CleanVediosActivity::class.java)
                startActivity(intent)
            }
            /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.clickedCount = Constant.clickedCount + 1
            }
            if (Constant.clickedCount < 4) {
                startActivity(Intent(requireActivity(), CleanVediosActivity::class.java))
            } else {
                Constant.clickedCount = 0
                mIntent =   Intent(requireActivity(), CleanVediosActivity::class.java)
                val intent  = Intent(requireActivity(), ProActivity::class.java)
                intent.putExtra("from","recover")
                resultLauncher.launch(intent)
            }*/

        }
        binding.audiosCV.setOnClickListener {

            if (!AppPreferences.getInstance(requireContext()).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), CleanAudiosActivity::class.java)
                showHighECPM(CleanAudiosActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), CleanAudiosActivity::class.java)
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), CleanAudiosActivity::class.java)
                startActivity(intent)
            }
            /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.clickedCount = Constant.clickedCount + 1
            }
            if (Constant.clickedCount < 4) {
                startActivity(Intent(requireActivity(), CleanAudiosActivity::class.java))
            } else {
                Constant.clickedCount = 0
                mIntent =  Intent(requireActivity(), CleanAudiosActivity::class.java)
                val intent  = Intent(requireActivity(), ProActivity::class.java)
                intent.putExtra("from","recover")
                resultLauncher.launch(intent)
            }*/
            /*if (!Constant.savedAudiosList.isEmpty()) {
                startActivity(Intent(requireActivity(), CleanAudiosActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Please Start Scanning", Toast.LENGTH_SHORT).show()
            }*/
        }
        binding.documentsCV.setOnClickListener {

            if (!AppPreferences.getInstance(requireContext()).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), CleanDocumentsActivity::class.java)
                showHighECPM(CleanDocumentsActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//                        } else {
//                            val intent =
//                                Intent(requireActivity(), CleanDocumentsActivity::class.java)
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), CleanDocumentsActivity::class.java)
                startActivity(intent)
            }
            /*if(!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                Constant.clickedCount = Constant.clickedCount + 1
            }
            if (Constant.clickedCount < 4) {
                startActivity(Intent(requireActivity(), CleanDocumentsActivity::class.java))
            } else {
                Constant.clickedCount = 0
                mIntent =   Intent(requireActivity(), CleanDocumentsActivity::class.java)
                val intent  = Intent(requireActivity(), ProActivity::class.java)
                intent.putExtra("from","recover")
                resultLauncher.launch(intent)
            }*/

            /*if (!Constant.savedDocumentsList.isEmpty()) {
                startActivity(Intent(requireActivity(), CleanDocumentsActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Please Start Scanning", Toast.LENGTH_SHORT).show()
            }*/
        }
        binding.scanningayout.setOnClickListener {

            /*Constant.savedImagesList.clear()
            Constant.savedAudiosList.clear()
            Constant.savedVediosList.clear()
            Constant.savedDocumentsList.clear()
            fetchData()*/
        }
        binding.premiumLayout.layout.setOnClickListener {
            val intent = Intent(requireActivity(), ProActivity::class.java)
            intent.putExtra("from", "main")
            startActivity(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mContext = null
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                startActivity(mIntent)
            }
        }


    fun nextScreen() {
        startActivity(mIntent)
    }

}