package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecovery.BuildConfig
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityScanBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.utils.Utils
import com.example.datarecovery.viewmodels.HomeViewModel
import com.example.datarecovery.views.adapters.*
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
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


class ScanActivity : BaseActivity() {
    lateinit var binding: ActivityScanBinding
    var recoverPhotos: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var photosList: ArrayList<FilesModel> = ArrayList<FilesModel>()

    var imagesAdapter: ImagesAdapter? = null
    val path = Environment.getExternalStorageDirectory().absolutePath
    var scannedFiles = 0
    var sorting = 4
    var dataLoaded = false
    private var mItems = ArrayList<AbstractItem>()
    private var retryAttempt = 0.0
    var gridLayoutManager: GridLayoutManager? = null
    var reviewManager: ReviewManager? = null
    var reviewInfo: ReviewInfo? = null
    lateinit var mainViewModel: HomeViewModel
    var updateCount = 0
    var interstitialAdd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()
        fetchData()


        imagesAdapter = ImagesAdapter(this@ScanActivity, photosList, object : DataListener {
            override fun onRecieve(any: Any) {
                performLogic(any as FilesModel)
            }

            override fun onClick(any: Any) {
                val fileModel = any as FilesModel
                openImage(fileModel)
            }

            override fun onClickWatchAd(any: Any) {
                super.onClickWatchAd(any)
                var click = any as Boolean
                if (click) {
                    watchAdDialog(object : DataListener {
                        override fun onRecieve(any: Any) {
                            val watchAd = any as Boolean
                            binding.sortIV.visibility = View.VISIBLE
                            if (watchAd) {
                                showAdmobInterstitial(object :DataListener{
                                    override fun onRecieve(any: Any) {
                                        if(any as Boolean){
                                            imagesAdapter?.setIsWatchAd()
                                            imagesAdapter?.notifyDataSetChanged()
                                            Constant.watchedAdScannig = true
                                            binding.viewMoreLayout.visibility = View.GONE
                                        }else{
                                            Toast.makeText(
                                                this@ScanActivity,
                                                getString(R.string.something_went_wrong),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val intent = Intent(this@ScanActivity, ProActivity::class.java)
                                            intent.putExtra("from", "main")
                                            startActivity(intent)
                                        }
                                    }
                                })

                            } else {
                                Toast.makeText(
                                    this@ScanActivity,
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
                                val intent = Intent(this@ScanActivity, ProActivity::class.java)
                                intent.putExtra("from", "main")
                                startActivity(intent)
                            }
                        }
                    }

                    )
                }
            }
        })
        /*gridLayoutManager = GridLayoutManager(this,6)
        gridLayoutManager!!.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position ==13) 2 else 1
            }
        }
        binding.recylerview.layoutManager = gridLayoutManager*/
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(6, LinearLayoutManager.VERTICAL)
        binding.recylerview.setLayoutManager(staggeredGridLayoutManager)
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

    private fun clickListener() {
        binding.recoverlayout.setOnClickListener {
            imagesAdapter?.setLongClickFalse()
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            var isDeleted = true


            val iterator = recoverPhotos.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = copy(item.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@ScanActivity,
                        getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    recoverPhotos.clear()
                    break
                } else {
                    iterator.remove()
                }

            }
            if (isDeleted) {
                if (Constant.showInAppReviewImages == 10) {
                    Constant.showInAppReviewImages = 0
                    showReviewInfo()
                }
                if (Constant.showInAppReviewImages == 0 && recoverPhotos.size > 10) {
                    Constant.showInAppReviewImages = 11
                    showReviewInfo()
                }
                recoverPhotos.clear()
                /*val destinationFile = StringBuilder()
                destinationFile.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                destinationFile.append(File.separator)
                destinationFile.append(getString(R.string.app_name))
                destinationFile.append(File.separator)
                destinationFile.append("Recovery")*/

                MediaScanner(this@ScanActivity)
//                Toast.makeText(this@ScanActivity,"All files recover successfully",Toast.LENGTH_SHORT).show()
                showSnackbar()
                recoverPhotos.clear()

            }

        }
        binding.sortIV.setOnClickListener { view ->
            try {
                if (dataLoaded) {
                    if (photosList.isEmpty()) {
                        Toast.makeText(
                            this@ScanActivity,
                            getString(R.string.no_files_found),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        sortingPopup(sorting, object : DataListener {
                            override fun onRecieve(any: Any) {
                                val sort = any as Int
                                CoroutineScope(Dispatchers.IO).launch {
                                    val job = CoroutineScope(Dispatchers.IO).async {
                                        if (sort == 1) {
                                            sorting = 1
                                            val tempList = ArrayList<FilesModel>()
                                            tempList.addAll(photosList)
                                            photosList.clear()
                                            photosList.addAll(tempList.sortedBy {
                                                it.file.length()
                                            })

                                        } else if (sort == 2) {
                                            sorting = 2
                                            photosList.sortByDescending {
                                                it.file.length()
                                            }
                                        } else if (sort == 3) {
                                            sorting = 3
                                            val tempList = ArrayList<FilesModel>()
                                            tempList.addAll(photosList)
                                            photosList.clear()
                                            photosList.addAll(tempList.sortedBy {
                                                it.file.lastModified()
                                            })

                                        } else if (sort == 4) {
                                            sorting = 4
                                            photosList.sortByDescending {
                                                it.file.lastModified()
                                            }
                                        } else {

                                        }
                                    }
                                    job.await()
                                    withContext(Dispatchers.Main) {
                                        imagesAdapter?.notifyDataSetChanged()
                                    }
                                }
                            }
                        }, view)
                    }
                } else {
                    Toast.makeText(
                        this@ScanActivity,
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
                    if (watchAd) {
                        showAdmobInterstitial(object :DataListener{
                            override fun onRecieve(any: Any) {
                                if(any as Boolean){
                                    imagesAdapter?.setIsWatchAd()
                                    imagesAdapter?.notifyDataSetChanged()
                                    Constant.watchedAdScannig = true
                                    binding.viewMoreLayout.visibility = View.GONE
                                }else{
                                    Toast.makeText(
                                        this@ScanActivity,
                                        getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(this@ScanActivity, ProActivity::class.java)
                                    intent.putExtra("from", "main")
                                    startActivity(intent)
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
                            this@ScanActivity,
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
                        val intent = Intent(this@ScanActivity, ProActivity::class.java)
                        intent.putExtra("from", "main")
                        startActivity(intent)
                    }
                }
            })
        }
    }

    fun checkFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) for (i in fileArr.indices) {
            if (fileArr[i].isDirectory) {
                checkFileOfDirectory(Utils.getFileList(fileArr[i].path))
            } else {
                scannedFiles += 1
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {

                    if (!fileArr[i].path.contains("Recovery")) {
                        val file = File(fileArr[i].path)
                        photosList.add(FilesModel(file, false))
                        CoroutineScope(Dispatchers.Main).launch {
                            imagesAdapter?.notifyDataSetChanged()
                            binding.progressTV.text =
                                scannedFiles.toString() + getString(R.string.files_scanned) + " , " + photosList.size + getString(
                                    R.string.images_found
                                )
                        }
                    }

                } else {
                }
            }
        }
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverPhotos.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverPhotos.add(file)
        } else {
            val item = list.first()
            val index = findIndex(recoverPhotos, item)
            if (!file.isCheck) {
                recoverPhotos.remove(item)
            } else {
                recoverPhotos.add(item)
            }
        }
        if (recoverPhotos.isNotEmpty()) {
            binding.recoverlayout.visibility = View.VISIBLE
            binding.countLayout.visibility = View.INVISIBLE
        } else {
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
        }
    }

    fun findIndex(arr: ArrayList<FilesModel>, item: FilesModel): Int {
        return arr.indexOf(item)
    }

    fun openImage(filesModel: FilesModel) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder
        builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.image_preview_layout, null)
        val previewIV = view.findViewById<ImageView>(R.id.previewIV)
        val cancelIV = view.findViewById<ImageView>(R.id.cancelIV)
        val titleTV = view.findViewById<TextView>(R.id.titleTV)
        val modifiedTV = view.findViewById<TextView>(R.id.modifiedTV)
        val recoverBtn = view.findViewById<AppCompatButton>(R.id.recoverBtn)
        val deleteBtnn = view.findViewById<AppCompatButton>(R.id.deleteBtnn)

        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        val file = filesModel.file
        titleTV.text = file.name
        val dateFormated = SimpleDateFormat("MMM dd HH:mm a").format(file.lastModified())

        modifiedTV.text = dateFormated
        try {
            Glide.with(this@ScanActivity)
                .load("file://" + file.path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .centerCrop()
                .error(R.drawable.home)
                .into(previewIV)
        } catch (e: Exception) {
            //do nothing
            Toast.makeText(this@ScanActivity, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
        }
        recoverBtn.setOnClickListener {
            val isSucces = copy(file)
            if (!isSucces) {
                Toast.makeText(
                    this@ScanActivity,
                    getString(R.string.try_again_later),
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this@ScanActivity,
                    getString(R.string.recover_image_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                Constant.showInAppReviewImages = Constant.showInAppReviewImages + 1
                if (Constant.showInAppReviewImages > 10) {
                    Constant.showInAppReviewImages = 0
                    showReviewInfo()
                }

            }
            imageDialog.dismiss()
        }
        deleteBtnn.setOnClickListener {
            val isSucces = file.delete()
            if (!isSucces) {
                Toast.makeText(
                    this@ScanActivity, getString(R.string.try_again_later),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                photosList.remove(filesModel)
                imagesAdapter?.notifyDataSetChanged()
                Toast.makeText(
                    this@ScanActivity, getString(R.string.image_deleted_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
            imageDialog.dismiss()
        }
        cancelIV.setOnClickListener { imageDialog.dismiss() }
        if (imageDialog.getWindow() != null) imageDialog.getWindow()!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        imageDialog.setCancelable(false)
        imageDialog.show()
    }

    fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.getRecoverData()
        }

        mainViewModel.liveDataImagesList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            photosList.clear()
            photosList.addAll(it)
            updateCount++
            if(updateCount>10) {
                updateCount = 0
                imagesAdapter?.notifyDataSetChanged()
            }
        }
        mainViewModel.liveDataIsDataLoaded.observe(this) {
            if (it) {
//                photosList.addAll(mainViewModel.imagesList)
                try {
                    photosList.sortByDescending {
                        it.file.lastModified()
                    }

                    imagesAdapter?.notifyDataSetChanged()
                    imagesAdapter?.setIsDataLoaded()
                    dataLoaded = true
                    binding.progressLayout.visibility = View.GONE
                    binding.sortIV.visibility = View.VISIBLE
                    /*watchAdDialog(object :DataListener{
                        override fun onRecieve(any: Any) {
                            val watchAd = any as Boolean
                            if(watchAd) {
                                loadRewardedAd(object :DataListener{
                                    override fun onRecieve(any: Any) {

                                    }
                                })
                                imagesAdapter?.setIsWatchAd()
                                imagesAdapter?.notifyDataSetChanged()
                            }else{
                                Toast.makeText(this@ScanActivity,getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onClick(any: Any) {
                            super.onClick(any)
                            finish()
                        }
                    })*/
                    completeScanningDialog(this@ScanActivity, photosList.size,  mainViewModel.scannedFiles)
                    if (!photosList.isEmpty()) {
                        binding.countLayout.visibility = View.VISIBLE
                        binding.recoverlayout.visibility = View.INVISIBLE
                    } else {
                        binding.recoverlayout.visibility = View.GONE
                        binding.countLayout.visibility = View.GONE
                        binding.noRecordFountTv.visibility = View.VISIBLE

                    }


                } catch (e: Exception) {
                    binding.progressLayout.visibility = View.GONE
                    imagesAdapter?.setIsDataLoaded()

                    Log.e("Exception", "doInBackground: " + e.message)
                }
            }
        }

        /*//Day la tat ca thu muc trong may
        val LoadingWavy = findViewById<LoadingCircleFady>(R.id.circleProgress)
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
                    photosList.sortByDescending {
                        it.file.lastModified()
                    }
                    withContext(Dispatchers.Main) {
                        imagesAdapter?.notifyDataSetChanged()
                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded = true
                        binding.progressLayout.visibility = View.GONE
                        binding.sortIV.visibility = View.VISIBLE
                        *//*watchAdDialog(object :DataListener{
                            override fun onRecieve(any: Any) {
                                val watchAd = any as Boolean
                                if(watchAd) {
                                    loadRewardedAd(object :DataListener{
                                        override fun onRecieve(any: Any) {

                                        }
                                    })
                                    imagesAdapter?.setIsWatchAd()
                                    imagesAdapter?.notifyDataSetChanged()
                                }else{
                                    Toast.makeText(this@ScanActivity,getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show()
                                }
                            }
                            override fun onClick(any: Any) {
                                super.onClick(any)
                                finish()
                            }
                        })*//*
                        completeScanningDialog(this@ScanActivity,photosList.size, scannedFiles)
                        if (!photosList.isEmpty()) {
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
            imagesAdapter?.setIsDataLoaded()

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
                    if (photosList.size > 500 && !AppPreferences.getInstance(this@ScanActivity).isAppPurchased) {
                        binding.viewMoreLayout.visibility = View.VISIBLE
                    }

                }
            }
        })
    }

    fun createReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this@ScanActivity)
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
            var flow: Task<Void> = reviewManager!!.launchReviewFlow(this@ScanActivity, reviewInfo!!)
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
            this@ScanActivity,
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
            interstitialAdd!!.show(this@ScanActivity)
        } else {
            dataListener.onRecieve(false)
        }
    }
}