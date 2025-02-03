package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecovery.BuildConfig
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityVedioBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.*
import com.example.datarecovery.viewmodels.HomeViewModel
import com.example.datarecovery.views.adapters.ImagesAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.razzaghimahdi78.dotsloading.circle.LoadingCircleFady
import kotlinx.coroutines.*
import java.io.File


class VedioActivity : BaseActivity() {
    lateinit var binding: ActivityVedioBinding
    var recoverVedios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var vediosList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: ImagesAdapter? = null
    val path = Environment.getExternalStorageDirectory().absolutePath
    var scannedFiles = 0
    var updateUICounter = 0
    var sorting = 4
    var dataLoaded = false
    private var retryAttempt = 0.0
    var reviewManager: ReviewManager? = null
    var reviewInfo: ReviewInfo? = null
    var interstitialAdd: InterstitialAd? = null
    lateinit var mainViewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVedioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()
        fetchData()
        imagesAdapter =
            ImagesAdapter(this@VedioActivity, vediosList, object : DataListener {
                override fun onRecieve(any: Any) {
                    performLogic(any as FilesModel)
                }

                override fun onClick(any: Any) {
                    val filesModel = any as FilesModel
                    val intent = Intent(this@VedioActivity, VedioPlayerActivity::class.java)
                    intent.putExtra("path", filesModel.file.path)
                    startActivity(intent)
                }

                override fun onClickWatchAd(any: Any) {
                    super.onClickWatchAd(any)

                    var click = any as Boolean
                    if (click) {
                        watchAdDialog(object : DataListener {
                            override fun onRecieve(any: Any) {
                                val watchAd = any as Boolean
                                binding.sortIV.visibility = View.VISIBLE
                                loadingAdProgress(true)
                                if (watchAd) {
                                    loadingAdProgress(false)
                                    showAdmobInterstitial(object :DataListener{
                                        override fun onRecieve(any: Any) {
                                            if(any as Boolean){
                                                imagesAdapter?.setIsWatchAd()
                                                imagesAdapter?.notifyDataSetChanged()
                                                Constant.watchedAdScannig = true
                                                binding.viewMoreLayout.visibility = View.GONE
                                            }else{
                                            }
                                        }
                                    })
                                    /*loadRewardedAd(admobRewardedScanningId(),object : DataListener {
                                        override fun onRecieve(any: Any) {
                                            var flag = any as Boolean
                                            if (flag) {
                                                *//*showInterstitial(this@VedioActivity,
                                                    object : DataListener {
                                                        override fun onRecieve(any: Any) {
                                                            if(any as Boolean){
                                                                loadingAdProgress(false)
                                                                imagesAdapter?.setIsWatchAd()
                                                                imagesAdapter?.notifyDataSetChanged()
                                                                Constant.watchedAdScannig = true
                                                            }
                                                        }
                                                    })*//*
                                            }
                                        }
                                        override fun onClick(any: Any) {
                                            super.onClick(any)
                                            imagesAdapter?.setIsWatchAd()
                                            imagesAdapter?.notifyDataSetChanged()
                                            Constant.watchedAdScannig = true
                                        }

                                        override fun onClickWatchAd(any: Any) {
                                            super.onClickWatchAd(any)
                                            if(any as Boolean){
                                                loadingAdProgress(false)
                                            }
                                        }

                                    })*/
                                } else {
                                    Toast.makeText(
                                        this@VedioActivity,
                                        getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            override fun onClick(any: Any) {
                                super.onClick(any)
                                finish()
                            }

                            override fun onPremium(any: Any) {
                                super.onPremium(any)

                                if (any as Boolean) {
                                    val intent = Intent(this@VedioActivity, ProActivity::class.java)
                                    intent.putExtra("from", "main")
                                    startActivity(intent)
                                }
                            }
                        })
                    }
                }
            })
        binding.recylerview.adapter = imagesAdapter
        binding.backIV.setOnClickListener {
            finish()
        }
        if (Constant.watchedAdScannig) {
            imagesAdapter!!.setIsWatchAd()
        }
        clickListener()
        scrollListener()
        createReviewInfo()
        initAds()
    }

    fun checkFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) for (i in fileArr.indices) {
            if (fileArr[i].isDirectory) {
                checkFileOfDirectory(Utils.getFileList(fileArr[i].path))
            } else {
                scannedFiles += 1
                updateUICounter += 1
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {

                } else {
                    if (!fileArr[i].path.contains("Recovery")) {
                        if (fileArr[i].path.endsWith(".mkv") ||
                            fileArr[i].path.endsWith(".mp4")
                        ) {
                            val file = File(fileArr[i].path)
                            vediosList.add(FilesModel(file, false))
                            if (updateUICounter > 100) {
                                updateUICounter = 0
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.progressTV.text =
                                        scannedFiles.toString() + getString(R.string.files_scanned) + " , " + vediosList.size + getString(
                                            R.string.videos_found
                                        )
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverVedios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverVedios.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                recoverVedios.remove(item)
            } else {
                recoverVedios.add(item)
            }
        }
        if (!recoverVedios.isEmpty()) {
            binding.recoverlayout.visibility = View.VISIBLE
            binding.countLayout.visibility = View.INVISIBLE
        } else {
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
        }
    }

    private fun clickListener() {
        binding.recoverlayout.setOnClickListener {
            imagesAdapter?.setLongClickFalse()
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            var isDeleted = true
            val iterator = recoverVedios.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = copy(item.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@VedioActivity,
                        getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    recoverVedios.clear()
                    break
                } else {
                    iterator.remove()
                }
            }
            if (isDeleted) {
                if (Constant.showInAppReviewVedios == 10) {
                    Constant.showInAppReviewVedios = 0
                    showReviewInfo()
                }
                if (Constant.showInAppReviewVedios == 0 && recoverVedios.size > 10) {
                    Constant.showInAppReviewVedios = 11
                    showReviewInfo()
                }
                recoverVedios.clear()
                MediaScanner(this@VedioActivity)
                showSnackbar()
//                Toast.makeText(this@VedioActivity,"All files recover successfully",Toast.LENGTH_SHORT).show()
            }
        }
        binding.sortIV.setOnClickListener { view ->
            try {
                if (dataLoaded) {
                    if (vediosList.isEmpty()) {
                        Toast.makeText(
                            this@VedioActivity,
                            getString(R.string.no_files_found),
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                    sortingPopup(sorting, object : DataListener {
                        override fun onRecieve(any: Any) {
                            val sort = any as Int
                            CoroutineScope(Dispatchers.IO).launch {
                                if (sort == 1) {
                                    sorting = 1
                                    vediosList.sortBy {
                                        it.file.length()
                                    }
                                } else if (sort == 2) {
                                    sorting = 2
                                    vediosList.sortByDescending {
                                        it.file.length()
                                    }
                                } else if (sort == 3) {
                                    sorting = 3
                                    vediosList.sortBy {
                                        it.file.lastModified()
                                    }
                                } else if (sort == 4) {
                                    sorting = 4
                                    vediosList.sortByDescending {
                                        it.file.lastModified()
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            }
                        }
                    }, view)
                } else {
                    Toast.makeText(
                        this@VedioActivity,
                        getString(R.string.scanning_please_wait),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (ex: Exception) {
            }
        }
        binding.backIV.setOnClickListener {
            onBackPressed()
        }
        binding.viewMoreLayout.setOnClickListener {
            watchAdDialog(object : DataListener {
                override fun onRecieve(any: Any) {
                    val watchAd = any as Boolean
                    binding.sortIV.visibility = View.VISIBLE
                    loadingAdProgress(true)
                    if (watchAd) {
                        loadingAdProgress(false)
                        showAdmobInterstitial(object :DataListener{
                            override fun onRecieve(any: Any) {
                                if(any as Boolean){
                                    imagesAdapter?.setIsWatchAd()
                                    imagesAdapter?.notifyDataSetChanged()
                                    Constant.watchedAdScannig = true
                                    binding.viewMoreLayout.visibility = View.GONE
                                }else{
                                }
                            }
                        })
                        /*loadRewardedAd(admobRewardedScanningId(),object : DataListener {
                            override fun onRecieve(any: Any) {
                                var flag = any as Boolean
                                if (flag) {

                                    loadRewardedInterstitialAd(object :DataListener{
                                        override fun onRecieve(any: Any) {
                                            if(any as Boolean){
                                                loadingAdProgress(false)
                                                imagesAdapter?.setIsWatchAd()
                                                imagesAdapter?.notifyDataSetChanged()
                                                Constant.watchedAdScannig = true
                                            }else{
                                showInterstitial(this@ScanActivity,
                                                    object : DataListener {
                                                        override fun onRecieve(any: Any) {
                                                            if(any as Boolean){
                                                                loadingAdProgress(false)
                                                                imagesAdapter?.setIsWatchAd()
                                                                imagesAdapter?.notifyDataSetChanged()
                                                                Constant.watchedAdScannig = true
                                                            }
                                                        }
                                                    })
                                            }
                                        }
                                    })


                                }
                            }
                            override fun onClick(any: Any) {
                                super.onClick(any)
                                imagesAdapter?.setIsWatchAd()
                                imagesAdapter?.notifyDataSetChanged()
                                Constant.watchedAdScannig = true
                            }

                            override fun onClickWatchAd(any: Any) {
                                super.onClickWatchAd(any)
                                if(any as Boolean){
                                    loadingAdProgress(false)
                                }
                            }
                        })*/

                    } else {
                        Toast.makeText(
                            this@VedioActivity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onClick(any: Any) {
                    super.onClick(any)
                    finish()
                }

                override fun onPremium(any: Any) {
                    super.onPremium(any)

                    if (any as Boolean) {
                        val intent = Intent(this@VedioActivity, ProActivity::class.java)
                        intent.putExtra("from", "main")
                        startActivity(intent)
                    }
                }
            })
        }
    }

    fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.getRecoverData()
        }

        mainViewModel.liveDataVideosList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            vediosList.clear()
            vediosList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataIsDataLoaded.observe(this) {
            if (it) {
//                vediosList.addAll(mainViewModel.videosList)
                try {

                    vediosList.sortByDescending {
                        it.file.lastModified()
                    }

                    imagesAdapter?.setIsDataLoaded()
                    dataLoaded = true
                    binding.progressLayout.visibility = View.GONE

                    /* watchAdDialog(object : DataListener {
                         override fun onRecieve(any: Any) {
                             val watchAd = any as Boolean
                             if (watchAd) {
                                 loadRewardedAd(object : DataListener {
                                     override fun onRecieve(any: Any) {

                                     }

                                 })
                                 imagesAdapter?.setIsWatchAd()
                                 imagesAdapter?.notifyDataSetChanged()
                             } else {
                                 Toast.makeText(
                                     this@VedioActivity,
                                     getString(R.string.something_went_wrong),
                                     Toast.LENGTH_LONG
                                 ).show()
                             }
                         }
                         override fun onClick(any: Any) {
                             super.onClick(any)
                             finish()
                         }
                     })*/
                    completeScanningDialog(this@VedioActivity, vediosList.size,  mainViewModel.scannedFiles)
                    if (!vediosList.isEmpty()) {
                        binding.countLayout.visibility = View.VISIBLE
                        binding.recoverlayout.visibility = View.INVISIBLE
                    } else {
                        binding.recoverlayout.visibility = View.GONE
                        binding.countLayout.visibility = View.GONE
                        binding.noRecordFountTv.visibility = View.VISIBLE
                    }

                } catch (e: Exception) {
                    binding.progressLayout.visibility = View.GONE
                    Log.e("Exception", "doInBackground: " + e.message)
                }
            }
        }


        /*val LoadingWavy = findViewById<LoadingCircleFady>(R.id.circleProgress)
        LoadingWavy.setDuration(400)
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (Utils.getFileList(path) != null) {
                    val job = CoroutineScope(Dispatchers.IO).async {
                        checkFileOfDirectory(
                            Utils.getFileList(path)
                        )
                    }
                    job.await()
                    vediosList.sortByDescending {
                        it.file.lastModified()
                    }
                    withContext(Dispatchers.Main) {
                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded = true
                        binding.progressLayout.visibility = View.GONE

                       *//* watchAdDialog(object : DataListener {
                            override fun onRecieve(any: Any) {
                                val watchAd = any as Boolean
                                if (watchAd) {
                                    loadRewardedAd(object : DataListener {
                                        override fun onRecieve(any: Any) {

                                        }

                                    })
                                    imagesAdapter?.setIsWatchAd()
                                    imagesAdapter?.notifyDataSetChanged()
                                } else {
                                    Toast.makeText(
                                        this@VedioActivity,
                                        getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            override fun onClick(any: Any) {
                                super.onClick(any)
                                finish()
                            }
                        })*//*
                        completeScanningDialog(this@VedioActivity,vediosList.size, scannedFiles)
                        if (!vediosList.isEmpty()) {
                            binding.countLayout.visibility = View.VISIBLE
                            binding.recoverlayout.visibility = View.INVISIBLE
                        } else {
                            binding.recoverlayout.visibility = View.GONE
                            binding.countLayout.visibility = View.GONE
                            binding.noRecordFountTv.visibility = View.VISIBLE

                        }
                    }
                }
            }
        } catch (e: Exception) {
            binding.progressLayout.visibility = View.GONE
            Log.e("Exception", "doInBackground: " + e.message)
        }*/
    }

    fun setmTheme() {
        val mtheme = AppPreferences.getInstance(this).getTheme
        if (mtheme == 0) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.light_theme_bg)
        } else if (mtheme == 1) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.dark_theme_bg)
        } else if (mtheme == 2) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme1_bg)
        } else if (mtheme == 3) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme2_bg)
        } else if (mtheme == 4) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme3)
        } else if (mtheme == 5) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme4)
        } else if (mtheme == 6) {
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme5)
        }
    }

    fun scrollListener() {
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if (vediosList.size > 500 && !AppPreferences.getInstance(this@VedioActivity).isAppPurchased) {
                        binding.viewMoreLayout.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    fun createReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this@VedioActivity)
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
            val flow: Task<Void> =
                reviewManager!!.launchReviewFlow(this@VedioActivity, reviewInfo!!)
            flow.addOnCompleteListener { task ->
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this, "Review successfull", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    fun initAds(){
        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this@VedioActivity,
            AdIds.admobInterstitialId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded( interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAdd = interstitialAd
                    Log.d("InterstitialAd", "onAdLoaded: ${interstitialAd.adUnitId}")
                }

                override fun onAdFailedToLoad( loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)

                    Log.d("InterstitialAd", "onAdFailedToLoad: ${loadAdError.message}")
                }
            })
    }
    fun showAdmobInterstitial(dataListener: DataListener) {
        if (interstitialAdd != null) {
            interstitialAdd!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError!!)
                        dataListener.onRecieve(false)
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        //                    completeListener.onInterstitialDismissed(true);
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        interstitialAdd = null
                        initAds()
                        dataListener.onRecieve(true)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                    }
                }
            interstitialAdd!!.show(this@VedioActivity)
        } else {
            dataListener.onRecieve(false)
        }
    }
}