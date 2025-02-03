package com.example.datarecovery.views.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.datarecovery.AppController
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentDuplicateBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant
import com.example.datarecovery.utils.SharedPrefsHelper
import com.example.datarecovery.views.activities.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import java.io.File
import java.text.DecimalFormat


class DuplicateFragment : Fragment() {
    lateinit var binding: FragmentDuplicateBinding
    var mContext: Context? = null
    private var retryAttempt = 0.0
    var mIntent: Intent? = null
    var interstitialAdd: InterstitialAd? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDuplicateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickListeners()
        /*if (!Constant.watchedAdDuplicate && !AppPreferences.getInstance(requireContext()).isAppPurchased) {
            unlockDuplicateFileremoverAdDialog(object : DataListener {
                override fun onRecieve(any: Any) {
                    val watchAd = any as Boolean
                    (requireActivity() as MainActivity).loadingAdProgress(true)
                    if (watchAd) {
                        (requireActivity() as MainActivity).loadRewardedAd(AdIds.admobRewardedDuplicateId(),
                            object : DataListener {
                                override fun onRecieve(any: Any) {
                                    var flag = any as Boolean
                                    Log.d("DuplicateFragment", "onRecieve: loadRewardedAd $flag")
                                    if (flag) {
                                        (requireActivity() as MainActivity).showInterstitial(
                                            requireActivity(),
                                            object : DataListener {
                                                override fun onRecieve(any: Any) {
                                                    Log.d("DuplicateFragment", "onRecieve: showInterstitial ${any as Boolean}")
                                                    if (any as Boolean) {
                                                        (requireActivity() as MainActivity).loadingAdProgress(
                                                            false
                                                        )
                                                        Constant.watchedAdDuplicate = true
                                                    }
                                                }
                                            })
                                    }
                                }

                                override fun onClick(any: Any) {
                                    super.onClick(any)
                                    Constant.watchedAdDuplicate = true

                                }

                                override fun onClickWatchAd(any: Any) {
                                    super.onClickWatchAd(any)
                                    if (any as Boolean) {
                                        (requireActivity() as MainActivity).loadingAdProgress(
                                            false
                                        )
                                    }
                                }
                            })

                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onClick(any: Any) {
                    super.onClick(any)
                    if (any as Boolean) {
                        findNavController().navigate(R.id.action_duplicateFragment_to_recoverFragment)
                    }
                }

            })
        }*/

        progressbarColor()
        initViews()
    }

    private val sharedPrefsHelper by lazy { SharedPrefsHelper(requireContext()) }

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
                val intentAct = Intent(requireActivity(), DuplicateActivity::class.java)
                intentAct.putExtra("from", "images")
                startActivity(intentAct)
            }

            is CleanVediosActivity -> {
                val intentAct = Intent(requireActivity(), DuplicateActivity::class.java)
                intentAct.putExtra("from", "videos")
                startActivity(intentAct)
            }

            is CleanAudiosActivity -> {
                val intentAct = Intent(requireActivity(), DuplicateActivity::class.java)
                intentAct.putExtra("from", "audios")
                startActivity(intentAct)
            }

            is CleanDocumentsActivity -> {
                val intentAct = Intent(requireActivity(), DuplicateActivity::class.java)
                intentAct.putExtra("from", "contacts")
                startActivity(intentAct)
            }

            is AllDuplicateActivity -> {
                val intentAct = Intent(requireActivity(), AllDuplicateActivity::class.java)
                startActivity(intentAct)
            }
        }
    }


    private fun initViews() {
        binding.memoryTv.text = (requireActivity() as MainActivity).memoryTv
        binding.progressBar.progress = (requireActivity() as MainActivity).memoryPercentage
    }

    private fun progressbarColor() {
        val progressDrawable: Drawable = binding.progressBar.getProgressDrawable().mutate()
        progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        binding.progressBar.setProgressDrawable(progressDrawable)
    }

    private fun clickListeners() {
        binding.imagesCV.setOnClickListener {
            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), DuplicateActivity::class.java)
                mIntent!!.putExtra("from", "images")
                showHighECPM(CleanImagesActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            (requireActivity() as MainActivity).loadingAdProgress(false)
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), DuplicateActivity::class.java)
//                            intent.putExtra("from", "images")
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), DuplicateActivity::class.java)
                intent.putExtra("from", "images")
                startActivity(intent)
            }
//            startActivity(Intent(requireActivity(), DuplicateActivity::class.java))
        }
        binding.videosCV.setOnClickListener {
            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), DuplicateActivity::class.java)
                mIntent!!.putExtra("from", "videos")
                showHighECPM(CleanVediosActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            (requireActivity() as MainActivity).loadingAdProgress(false)
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), DuplicateActivity::class.java)
//                            intent.putExtra("from", "videos")
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), DuplicateActivity::class.java)
                intent.putExtra("from", "videos")
                startActivity(intent)
            }

        }
        binding.audiosCV.setOnClickListener {
            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), DuplicateActivity::class.java)
                mIntent!!.putExtra("from", "audios")
                showHighECPM(CleanAudiosActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            (requireActivity() as MainActivity).loadingAdProgress(false)
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), DuplicateActivity::class.java)
//                            intent.putExtra("from", "audios")
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                val intent = Intent(requireActivity(), DuplicateActivity::class.java)
                intent.putExtra("from", "audios")
                startActivity(intent)
            }
        }
        binding.documentsCV.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.allow_permission_for_read_contacts),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), DuplicateActivity::class.java)
                mIntent!!.putExtra("from", "contacts")
                showHighECPM(CleanDocumentsActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            (requireActivity() as MainActivity).loadingAdProgress(false)
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), DuplicateActivity::class.java)
//                            intent.putExtra("from", "contacts")
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {

                val intent = Intent(requireActivity(), DuplicateActivity::class.java)
                intent.putExtra("from", "contacts")
                startActivity(intent)
            }
        }
        binding.scanallCV.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.allow_permission_for_read_contacts),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            if (Constant.userClickDuplicateItemFirstTiem && !AppPreferences.getInstance(
                    requireContext()
                ).isAppPurchased
            ) {
                mIntent = Intent(requireActivity(), AllDuplicateActivity::class.java)
                showHighECPM(AllDuplicateActivity())
//                showAdmobInterstitial(object : DataListener {
//                    override fun onRecieve(any: Any) {
//                        if (any as Boolean) {
//                            (requireActivity() as MainActivity).loadingAdProgress(false)
//                            nextScreen()
//                        } else {
//                            val intent = Intent(requireActivity(), AllDuplicateActivity::class.java)
//                            startActivity(intent)
//                        }
//                    }
//                })
            } else {
                (requireActivity() as MainActivity).loadingAdProgress(false)
                val intent = Intent(requireActivity(), AllDuplicateActivity::class.java)
                startActivity(intent)
            }
            /*AdsManager.getInstance().LoadDuplicateAdmobInterstitial(requireContext())
            (requireActivity() as MainActivity).showInterstitial(requireActivity(),
                object : DataListener {
                    override fun onRecieve(any: Any) {
                        if(any as Boolean){
                            (requireActivity() as MainActivity).loadingAdProgress(false)
                            val intent = Intent(requireActivity(), AllDuplicateActivity::class.java)
                            startActivity(intent)
                        }
                    }
                })*/

        }
        binding.premiumLayout.layout.setOnClickListener {
            val intent = Intent(requireActivity(), ProActivity::class.java)
            intent.putExtra("from", "main")
            startActivity(intent)
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
        /*if(externalMemoryAvailable){
            totalMemeory += getTotalExternalMemorySize
            freeMemory += getAvailableExternalMemorySize
        }*/
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

    fun unlockDuplicateFileremoverAdDialog(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.unlock_duplicate_file_remover_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        imageDialog.setCancelable(false)
        val premiumLayout = view.findViewById<ConstraintLayout>(R.id.premiumLayout)
        val adIV = view.findViewById<ImageView>(R.id.adIV)
        val cancel = view.findViewById<ImageView>(R.id.cancelIV)

        adIV.setOnClickListener {
            dataListener.onRecieve(true)
            imageDialog.dismiss()
        }
        cancel.setOnClickListener {
            dataListener.onClick(true)
            imageDialog.dismiss()
        }
        premiumLayout.setOnClickListener {
            val intent = Intent(requireActivity(), ProActivity::class.java)
            intent.putExtra("from", "main")
            startActivity(intent)
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
        imageDialog.show()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mContext = null
    }


    fun LoadAdmobBanner(
        activity: Activity,
        bannerContainer: LinearLayout,
        addCallBack: BannerAddCallBack
    ) {
        bannerContainer.setGravity(Gravity.CENTER)
        val mAdmobBanner = AdView(activity)
        val adSize: AdSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.setAdUnitId(Constant.bannerId)
        val extras = Bundle()
        extras.putString("collapsible", "top")
        val adRequest11: AdRequest =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        val adRequest1: AdRequest = AdRequest.Builder().build()
        mAdmobBanner.loadAd(adRequest1)
        mAdmobBanner.setAdListener(object : AdListener() {
            override fun onAdLoaded() {

                super.onAdLoaded()
                bannerContainer.removeAllViews()
                bannerContainer.addView(mAdmobBanner)
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                addCallBack.bannerFailedToLoad()
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                addCallBack.bannerClicked(true)
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        })
    }

    private fun getAdSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels: Float = outMetrics.widthPixels.toFloat()
        val density: Float = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    interface BannerAddCallBack {
        fun bannerFailedToLoad()
        fun bannerClicked(clicked: Boolean?)
    }


    fun nextScreen() {
        startActivity(mIntent)
    }
}