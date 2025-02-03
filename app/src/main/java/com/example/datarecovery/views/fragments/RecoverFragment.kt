package com.example.datarecovery.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.datarecovery.AppController
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentRecoverBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant
import com.example.datarecovery.utils.SharedPrefsHelper
import com.example.datarecovery.views.activities.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import java.io.File
import java.text.DecimalFormat


class RecoverFragment : Fragment() {
    lateinit var binding: FragmentRecoverBinding
    val path = Environment.getExternalStorageDirectory().absolutePath
    var mContext: Context? = null
    var mIntent: Intent? = null
    var TAG = RecoverFragment::class.java.simpleName
    var interstitialAdd: InterstitialAd? = null
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecoverBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickListeners()
        calculateMemory()

        if (isPremiumEnabled()) {
            binding.premiumLayout.layout.visibility = View.VISIBLE
        }

        if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
            if (isNativeHomeEnabled()) {
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

    private fun isNativeHomeEnabled(): Boolean {
        return sharedPrefsHelper.getIsNativeHomeEnabled()
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
                Log.d("de_atag", it.toString())
            }
        }
    }


    private fun calculateMemory() {
        val externalMemoryAvailable = externalMemoryAvailable()
        Log.d("TAG", "onViewCreated: externalMemoryAvailable   $externalMemoryAvailable")
        val getAvailableInternalMemorySize = getAvailableInternalMemorySize()
        Log.d(
            "TAG",
            "onViewCreated: getAvailableInternalMemorySize   $getAvailableInternalMemorySize"
        )
        val getTotalInternalMemorySize = getTotalInternalMemorySize()
        Log.d("TAG", "onViewCreated: getTotalInternalMemorySize   $getTotalInternalMemorySize")
        val getAvailableExternalMemorySize = getAvailableExternalMemorySize()
        Log.d(
            "TAG",
            "onViewCreated: getAvailableExternalMemorySize   $getAvailableExternalMemorySize"
        )
        val getTotalExternalMemorySize = getTotalExternalMemorySize()
        Log.d("TAG", "onViewCreated: getTotalExternalMemorySize   $getTotalExternalMemorySize")
        var totalMemeory = getTotalInternalMemorySize()
        var usedMemory: Long = 0
        var freeMemory = getAvailableInternalMemorySize
        usedMemory = totalMemeory - freeMemory


        binding.memoryTv.text =
            "${formatSize(usedMemory)?.toUpperCase()} / ${formatSize(totalMemeory)?.toUpperCase()}"
//        val percentage = (usedMemory / totalMemeory) * 100
        val percentage = (usedMemory.toFloat() / totalMemeory.toFloat()) * 100
        Log.d("TAG", "calculateMemory: ${totalMemeory}")
        Log.d("TAG", "calculateMemory: ${usedMemory}")
        Log.d("TAG", "calculateMemory: ${percentage.toInt()}")
        binding.progressBar.progress = percentage.toInt()
//        binding.memoryTv.text = usedMemory.toString()+" GB / ${totalMemeory.toString()} GB"
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
            is ScanActivity -> {
                val intentAct = Intent(requireActivity(), ScanActivity::class.java)
                startActivity(intentAct)
            }

            is VedioActivity -> {
                val intentAct = Intent(requireActivity(), VedioActivity::class.java)
                startActivity(intentAct)
            }

            is AudiosActivity -> {
                val intentAct = Intent(requireActivity(), AudiosActivity::class.java)
                startActivity(intentAct)
            }

            is DocumentsActivity -> {
                val intentAct = Intent(requireActivity(), DocumentsActivity::class.java)
                startActivity(intentAct)
            }
        }
    }


    private fun clickListeners() {
        binding.imagesCV.setOnClickListener {
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                binding.adloadingscreen.visibility = View.VISIBLE
                mIntent = Intent(requireActivity(), ScanActivity::class.java)
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//                        } else {
//                            startActivity(Intent(requireActivity(), ScanActivity::class.java))
//                        }
//                    }
//                })
                showHighECPM(ScanActivity())
            } else {
                if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    Constant.clickedCount = Constant.clickedCount + 1
                }
                if (Constant.clickedCount < 4) {
                    startActivity(Intent(requireActivity(), ScanActivity::class.java))
                } else {
                    Constant.clickedCount = 0
                    mIntent = Intent(requireActivity(), ScanActivity::class.java)
                    val intent = Intent(requireActivity(), ProActivity::class.java)
                    intent.putExtra("from", "recover")
                    resultLauncher.launch(intent)
                }
            }
        }
        binding.videosCV.setOnClickListener {
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                mIntent = Intent(requireActivity(), VedioActivity::class.java)

                showHighECPM(VedioActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//
//                        } else {
//                            startActivity(Intent(requireActivity(), VedioActivity::class.java))
//                        }
//                    }
//                })
                /*(requireActivity() as MainActivity).loadingAdProgress(true)
                (requireActivity() as MainActivity).showInterstitial(requireActivity(),
                    object : DataListener {
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                (requireActivity() as MainActivity).loadingAdProgress(false)
                                Constant.userClickHomePageFirstTiem = false
                                startActivity(Intent(requireActivity(), VedioActivity::class.java))
                            }
                        }
                    })*/
            } else {
                if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    Constant.clickedCount = Constant.clickedCount + 1
                }
                if (Constant.clickedCount < 4) {
                    startActivity(Intent(requireActivity(), VedioActivity::class.java))
                } else {
                    Constant.clickedCount = 0
                    mIntent = Intent(requireActivity(), VedioActivity::class.java)
                    val intent = Intent(requireActivity(), ProActivity::class.java)
                    intent.putExtra("from", "recover")
                    resultLauncher.launch(intent)
                }
            }
            /*if(!Constant.vediosList.isEmpty()) {
                startActivity(Intent(requireActivity(), VedioActivity::class.java))
            }else{
                Toast.makeText(requireContext(), "Please Start Scanning", Toast.LENGTH_SHORT).show()
            }*/

        }
        binding.audiosCV.setOnClickListener {
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                mIntent = Intent(requireActivity(), AudiosActivity::class.java)
                showHighECPM(AudiosActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//
//                        } else {
//                            startActivity(Intent(requireActivity(), AudiosActivity::class.java))
//                        }
//                    }
//                })
                /*(requireActivity() as MainActivity).loadingAdProgress(true)
                (requireActivity() as MainActivity).showInterstitial(requireActivity(),
                    object : DataListener {
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                (requireActivity() as MainActivity).loadingAdProgress(false)
                                Constant.userClickHomePageFirstTiem = false
                                startActivity(Intent(requireActivity(), AudiosActivity::class.java))
                            }
                        }
                    })*/
            } else {
                if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    Constant.clickedCount = Constant.clickedCount + 1
                }
                if (Constant.clickedCount < 4) {
                    startActivity(Intent(requireActivity(), AudiosActivity::class.java))
                } else {
                    Constant.clickedCount = 0
                    mIntent = Intent(requireActivity(), AudiosActivity::class.java)
                    val intent = Intent(requireActivity(), ProActivity::class.java)
                    intent.putExtra("from", "recover")
                    resultLauncher.launch(intent)
                }
            }
            /*if(!Constant.audiosList.isEmpty()) {
                startActivity(Intent(requireActivity(), AudiosActivity::class.java))
            }else{
                Toast.makeText(requireContext(), "Please Start Scanning", Toast.LENGTH_SHORT).show()
            }*/
        }
        binding.documentsCV.setOnClickListener {
            if (Constant.userClickHomePageFirstTiem && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
                mIntent = Intent(requireActivity(), DocumentsActivity::class.java)
                showHighECPM(DocumentsActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            nextScreen()
//
//                        } else {
//                            startActivity(Intent(requireActivity(), DocumentsActivity::class.java))
//                        }
//                    }
//                })
                /* (requireActivity() as MainActivity).loadingAdProgress(true)
                 (requireActivity() as MainActivity).showInterstitial(requireActivity(),
                     object : DataListener {
                         override fun onRecieve(any: Any) {
                             if(any as Boolean){
                                 (requireActivity() as MainActivity).loadingAdProgress(false)
                                 Constant.userClickHomePageFirstTiem = false
                                 startActivity(Intent(requireActivity(), DocumentsActivity::class.java))
                             }
                         }
                     })*/
            } else {
                if (!AppPreferences.getInstance(requireContext()).isAppPurchased) {
                    Constant.clickedCount = Constant.clickedCount + 1
                }
                if (Constant.clickedCount < 4) {
                    startActivity(Intent(requireActivity(), DocumentsActivity::class.java))
                } else {
                    Constant.clickedCount = 0
                    mIntent = Intent(requireActivity(), DocumentsActivity::class.java)
                    val intent = Intent(requireActivity(), ProActivity::class.java)
                    intent.putExtra("from", "recover")
                    resultLauncher.launch(intent)
                }
            }

            /*if(!Constant.documentsList.isEmpty()) {
                startActivity(Intent(requireActivity(), DocumentsActivity::class.java))
            }else{
                Toast.makeText(requireContext(), "Please Start Scanning", Toast.LENGTH_SHORT).show()
            }*/
        }
        binding.scanningayout.setOnClickListener {
            /* Constant.imagesList.clear()
             Constant.audiosList.clear()
             Constant.vediosList.clear()
             Constant.documentsList.clear()*/
//            fetchData()
        }
        binding.premiumLayout.layout.setOnClickListener {
            val intent = Intent(requireActivity(), ProActivity::class.java)
            intent.putExtra("from", "main")
            startActivity(intent)
        }
    }

    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    fun getAvailableInternalMemorySize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.getPath())
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return (availableBlocks * blockSize)
//        return formatSize(availableBlocks * blockSize)
    }

    fun getTotalInternalMemorySize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.getPath())
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return (totalBlocks * blockSize)
//        return formatSize(totalBlocks * blockSize)
    }

    fun getAvailableExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.getPath())
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            (availableBlocks * blockSize)
//            formatSize(availableBlocks * blockSize)
        } else {
            0
        }
    }

    fun getTotalExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path: File = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.getPath())
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            (totalBlocks * blockSize)
        } else {
            0
        }
    }

    fun formatSize(size: Long): String? {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return df.format(size / sizeKb)
            .toString() + " Kb" else if (size < sizeGb) return df.format(size / sizeMb)
            .toString() + " Mb" else if (size < sizeTerra) return df.format(size / sizeGb)
            .toString() + " Gb"
        return ""
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
//            doSomeOperations()
            }
        }

    fun nextScreen() {
        startActivity(mIntent)
    }
}