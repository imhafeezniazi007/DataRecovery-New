package com.example.datarecovery

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityMainBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.SharedPrefsHelper
import com.example.datarecovery.views.activities.AboutUsActivity
import com.example.datarecovery.views.activities.HowToUseActivity
import com.example.datarecovery.views.activities.ProActivity
import com.example.datarecovery.views.activities.RootActivity
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import java.io.File
import java.text.DecimalFormat


class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    var selectedTextColor: Int = 0
    var reviewManager: ReviewManager? = null
    var reviewInfo: ReviewInfo? = null
    var memoryTv = ""
    var memoryPercentage = 0
    private val sharedPrefsHelper by lazy { SharedPrefsHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavigationController()
        initDrawer()
        setupSpinner()
        clickListener()
        if (AppPreferences.getInstance(this).isAppPurchased) {
            binding.adView.visibility = View.GONE
            binding.adLoadingTv.visibility = View.GONE
            binding.border.visibility = View.GONE
        } else {
            if (isBannerHomeEnabled()) {
                loadAdmobBanner(this@MainActivity, binding.adView)
            } else {
                binding.adView.visibility = View.GONE
                binding.adLoadingTv.visibility = View.GONE
                binding.border.visibility = View.GONE
            }
        }

        if (sharedPrefsHelper.getIsBannerSideNavEnabled()) {
            initBannerAd()
        } else {
            binding.adViewSide.visibility = View.GONE
            binding.adLoadingTvSide.visibility = View.GONE
        }

        createReviewInfo()
        calculateMemory()
    }


    private fun initBannerAd() {
        if (AppPreferences.getInstance(this).isAppPurchased) {
            binding.adViewSide.visibility = View.GONE
            binding.adLoadingTvSide.visibility = View.GONE
            binding.borderSide.visibility = View.GONE
        } else {
            loadAdmobBannerSide(this@MainActivity, binding.adViewSide)
        }
    }

    private fun isBannerHomeEnabled(): Boolean {
        return sharedPrefsHelper.getIsBannerHomeEnabled()
    }

    private fun clickListener() {
        binding.aboutLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            startActivity(Intent(this@MainActivity, AboutUsActivity::class.java))
        }
        binding.rootLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            startActivity(Intent(this@MainActivity, RootActivity::class.java))
        }
        binding.htuLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            startActivity(Intent(this@MainActivity, HowToUseActivity::class.java))
        }
        binding.premiumLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            val intent = Intent(this@MainActivity, ProActivity::class.java)
            intent.putExtra("from", "main")
            startActivity(intent)
        }
        binding.iv.setOnClickListener {

        }
        binding.shareLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)

        }
        binding.rateusLayout.setOnClickListener {
            launchMarket()
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            /*val manager = ReviewManagerFactory.create(this@MainActivity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(this@MainActivity, reviewInfo)
                    flow.addOnCompleteListener {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        Log.d("Review", "clickListener: In-app review returned.")
                        // matter the result, we continue our app flow.
                    }.addOnFailureListener {
                        Log.d("Review", "clickListener: ${it.message}")
                    }
                } else {
                    // There was some problem, continue regardless of the result.

                }
            }*/
        }
        binding.adIV.setOnClickListener {
        }
        binding.privacyLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://sites.google.com/view/apps-yard/home")
            )
            startActivity(browserIntent)

        }
        binding.appsLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            val uri = Uri.parse("https://play.google.com/store/apps/developer?id=Apps+Yard+Inc")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            startActivity(intent)

        }
        binding.shareLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)

        }
        binding.reportLayout.setOnClickListener {
            binding.drawerLayout.closeDrawer(Gravity.START, false)
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "appsyardinc@gmail.com", null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))

        }
    }

    private fun setupSpinner() {
        val typedValue = TypedValue()
        val theme: Resources.Theme = getTheme()
        theme.resolveAttribute(R.attr.text_color, typedValue, true)
        @ColorInt val color: Int = typedValue.data
        val spinner: Spinner = findViewById<View>(R.id.spinner) as Spinner
        spinner.getBackground()
            .setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        val languages = ArrayList<String>()
        languages.add("Languages")
        languages.add("Afrikaans")
        languages.add("Arabic")
        languages.add("Chinese")
        languages.add("Danish")
        languages.add("Dutch")
        languages.add("English")
        languages.add("French")
        languages.add("German")
        languages.add("Hindi")
        languages.add("Italian")
        languages.add("Japanese")
        languages.add("Korean")
        languages.add("Malay")
        languages.add("Norwegian")
        languages.add("Portuguese")
        languages.add("Russian")
        languages.add("Spanish")
        languages.add("Swedish")
        languages.add("Thai")
        languages.add("Turkish")
        languages.add("Ukrainian")
        languages.add("Vietnamese")
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //Change the selected item's text color
                Log.d("TAG", "onItemSelected: $position")

//                (view as TextView).setTextColor(selectedTextColor)
                if (position != 0) {
                    if (position == 1) {
                        setLanguage("af")
                    } else if (position == 2) {
                        setLanguage("ar")
                    } else if (position == 3) {
                        setLanguage("zh")
                    } else if (position == 4) {
                        setLanguage("da")
                    } else if (position == 5) {
                        setLanguage("nl")
                    } else if (position == 6) {
                        setLanguage("en")
                    } else if (position == 7) {
                        setLanguage("fr")
                    } else if (position == 8) {
                        setLanguage("de")
                    } else if (position == 9) {
                        setLanguage("hi")
                    } else if (position == 10) {
                        setLanguage("it")
                    } else if (position == 11) {
                        setLanguage("ja")
                    } else if (position == 12) {
                        setLanguage("ko")
                    } else if (position == 13) {
                        setLanguage("ms")
                    } else if (position == 14) {
                        setLanguage("no")
                    } else if (position == 15) {
                        setLanguage("pt")
                    } else if (position == 16) {
                        setLanguage("ru")
                    } else if (position == 17) {
                        setLanguage("es")
                    } else if (position == 18) {
                        setLanguage("sv")
                    } else if (position == 19) {
                        setLanguage("th")
                    } else if (position == 20) {
                        setLanguage("tr")
                    } else if (position == 21) {
                        setLanguage("uk")
                    } else if (position == 22) {
                        setLanguage("vi")
                    }
                }

            }

        }

        binding.spinnerView.setItems(languages)
        binding.spinnerView.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, position, newItem ->
            // do something
            Log.d("TAG", "setupSpinner: $newItem is selected! (previous: $oldItem)")
            Log.d("TAG", "setupSpinner: $newItem is selected! (newIndex: $position)")

            if (position != 0) {
                if (position == 1) {
                    setLanguage("af")
                } else if (position == 2) {
                    setLanguage("ar")
                } else if (position == 3) {
                    setLanguage("zh")
                } else if (position == 4) {
                    setLanguage("da")
                } else if (position == 5) {
                    setLanguage("nl")
                } else if (position == 6) {
                    setLanguage("en")
                } else if (position == 7) {
                    setLanguage("fr")
                } else if (position == 8) {
                    setLanguage("de")
                } else if (position == 9) {
                    setLanguage("hi")
                } else if (position == 10) {
                    setLanguage("it")
                } else if (position == 11) {
                    setLanguage("ja")
                } else if (position == 12) {
                    setLanguage("ko")
                } else if (position == 13) {
                    setLanguage("ms")
                } else if (position == 14) {
                    setLanguage("no")
                } else if (position == 15) {
                    setLanguage("pt")
                } else if (position == 16) {
                    setLanguage("ru")
                } else if (position == 17) {
                    setLanguage("es")
                } else if (position == 18) {
                    setLanguage("sv")
                } else if (position == 19) {
                    setLanguage("th")
                } else if (position == 20) {
                    setLanguage("tr")
                } else if (position == 21) {
                    setLanguage("uk")
                } else if (position == 22) {
                    setLanguage("vi")
                }
            }
        }

    }

    private fun initDrawer() {
        Glide.with(this).asGif().load(R.raw.theme).into(binding.adIV);
        binding.drawerIV.setOnClickListener {
            val typedValue = TypedValue()
            val theme: Resources.Theme = getTheme()
            theme.resolveAttribute(R.attr.custom_black, typedValue, true)
            selectedTextColor = typedValue.data
            if (!binding.drawerLayout.isDrawerOpen(Gravity.START)) binding.drawerLayout.openDrawer(
                Gravity.START
            );
            else binding.drawerLayout.closeDrawer(Gravity.END);
        }
    }

    private fun setNavigationController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navController) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
    }


    override fun onBackPressed() {
        exitDialog()
        /*if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.exit_app), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)*/

    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        val headlineView = adView.findViewById<TextView>(R.id.nativeTV)
        headlineView.text = nativeAd.headline
        adView.headlineView = headlineView
        val mediaView = adView.findViewById<MediaView>(R.id.mediaView)
        adView.mediaView = mediaView
        adView.setNativeAd(nativeAd)
    }

    fun exitDialog() {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.exit_dialog, null)
        builder.setView(view)
        imageDialog = builder.create()
        val tapAgainLayout = view.findViewById<ConstraintLayout>(R.id.tapAgainLayout)


        tapAgainLayout.setOnClickListener {
            finishAffinity()
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
        if (!this@MainActivity.isFinishing) {
            imageDialog.show()
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // dismiss shown snackbar if user tapped anywhere outside snackbar
        if (binding.spinnerView.isShowing) {
            binding.spinnerView.dismiss()
        }

        // call super
        return super.dispatchTouchEvent(ev)
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {

        }
    }

    fun unlockAllThemeDialog(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.unlock_all_theme_dialog, null)
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
        imageDialog.window!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

        imageDialog.show()

    }


    fun createReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this@MainActivity)
        val resuest: Task<ReviewInfo> = reviewManager!!.requestReviewFlow()
        resuest.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            } else {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, "Review info not recieved", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun showReviewInfo() {
        if (reviewInfo != null) {
            var flow: Task<Void> = reviewManager!!.launchReviewFlow(this@MainActivity, reviewInfo!!)
            flow.addOnCompleteListener { task ->

                Toast.makeText(this, "Review successfull", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchMarket() {
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show()
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


        memoryTv =
            "${formatSize(usedMemory)?.toUpperCase()} / ${formatSize(totalMemeory)?.toUpperCase()}"
//        val percentage = (usedMemory / totalMemeory) * 100
        val percentage = (usedMemory.toFloat() / totalMemeory.toFloat()) * 100
        Log.d("TAG", "calculateMemory: ${totalMemeory}")
        Log.d("TAG", "calculateMemory: ${usedMemory}")
        Log.d("TAG", "calculateMemory: ${percentage.toInt()}")
        memoryPercentage = percentage.toInt()
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

    fun loadAdmobBannerSide(
        activity: Activity,
        bannerContainer: LinearLayout
    ) {
        bannerContainer.setGravity(Gravity.CENTER)
        val mAdmobBanner = AdView(activity)

        val adSize: AdSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.adUnitId = AdIds.admobBannerId()

        val extras = Bundle()
        extras.putString("collapsible", "bottom")
        val adRequest1: AdRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        mAdmobBanner.loadAd(adRequest1)
        mAdmobBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                bannerContainer.removeAllViews()
                bannerContainer.addView(mAdmobBanner)
                binding.adLoadingTvSide.visibility = View.GONE
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d("MyLogs", "onAdFailedToLoad: ${loadAdError.cause}")
                binding.adViewSide.visibility = View.INVISIBLE
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }
    }

    fun loadAdmobBanner(
        activity: Activity,
        bannerContainer: LinearLayout
    ) {
        bannerContainer.setGravity(Gravity.CENTER)
        val mAdmobBanner = AdView(activity)

        val adSize: AdSize = getAdSize(activity)
        mAdmobBanner.setAdSize(adSize)
        mAdmobBanner.adUnitId = AdIds.admobBannerId()

        val extras = Bundle()
        extras.putString("collapsible", "top")
        val adRequest1: AdRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        mAdmobBanner.loadAd(adRequest1)
        mAdmobBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                bannerContainer.removeAllViews()
                bannerContainer.addView(mAdmobBanner)
                binding.adLoadingTv.visibility = View.GONE
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d("MyLogs", "onAdFailedToLoad: ${loadAdError.cause}")
                binding.adView.visibility = View.INVISIBLE
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }
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
}