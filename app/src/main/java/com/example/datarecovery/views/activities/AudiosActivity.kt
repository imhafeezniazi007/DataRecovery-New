package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.datarecovery.BuildConfig
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityAudiosBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AdIds
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.utils.Utils
import com.example.datarecovery.viewmodels.HomeViewModel
import com.example.datarecovery.views.adapters.AudiosAdapter
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
import kotlinx.coroutines.*
import java.io.File


class AudiosActivity : BaseActivity() {
    lateinit var binding: ActivityAudiosBinding
    var recoverAudios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var audiosList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: AudiosAdapter? = null
    val path = Environment.getExternalStorageDirectory().absolutePath
    var scannedFiles = 0
    var seekbar: SeekBar? = null
    private var startTime = 0.0
    private val finalTime = 0.0
    val mediaPlayer = MediaPlayer()
    private var currentIndex = -1
    var sorting = 4
    var dataLoaded = false
    private var retryAttempt = 0.0
    var reviewManager: ReviewManager? = null
    var reviewInfo: ReviewInfo? = null
    lateinit var mainViewModel: HomeViewModel
    var interstitialAdd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudiosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()

        fetchData()
        imagesAdapter = AudiosAdapter(this@AudiosActivity, audiosList, object : DataListener {
            override fun onRecieve(any: Any) {
                performLogic(any as FilesModel)
            }

            override fun onClick(any: Any) {
                val file = any as FilesModel
                currentIndex = findFileIndex(audiosList, file)
                playAudioDialog(file.file)
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
                                                this@AudiosActivity,
                                                getString(R.string.something_went_wrong),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val intent = Intent(this@AudiosActivity, ProActivity::class.java)
                                            intent.putExtra("from", "main")
                                            startActivity(intent)
                                        }
                                    }
                                })

                            } else {
                                Toast.makeText(
                                    this@AudiosActivity,
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
                                val intent = Intent(this@AudiosActivity, ProActivity::class.java)
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

    private fun clickListener() {
        binding.recoverlayout.setOnClickListener {
            imagesAdapter?.setLongClickFalse()
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            var isDeleted = true
            val iterator = recoverAudios.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = copy(item.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@AudiosActivity,
                        getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                    recoverAudios.clear()
                    break
                } else {
                    iterator.remove()
                }

            }
            if (isDeleted) {
                if (Constant.showInAppReviewAudios == 10) {
                    Constant.showInAppReviewAudios = 0
                    showReviewInfo()
                }
                if (Constant.showInAppReviewAudios == 0 && recoverAudios.size > 10) {
                    Constant.showInAppReviewAudios = 11
                    showReviewInfo()
                }
                recoverAudios.clear()
                MediaScanner(this@AudiosActivity)
                showSnackbar()
//                Toast.makeText(this@AudiosActivity,"All files recover successfully",Toast.LENGTH_SHORT).show()
            }

        }
        binding.sortIV.setOnClickListener { view ->
            try {
                if (dataLoaded) {
                    if (audiosList.isEmpty()) {
                        Toast.makeText(
                            this@AudiosActivity,
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
                                    audiosList.sortBy {
                                        it.file.length()
                                    }
                                    withContext(Dispatchers.Main) {
                                        imagesAdapter?.notifyDataSetChanged()
                                    }
                                } else if (sort == 2) {
                                    sorting = 2
                                    audiosList.sortByDescending {
                                        it.file.length()
                                    }
                                    withContext(Dispatchers.Main) {
                                        imagesAdapter?.notifyDataSetChanged()
                                    }
                                } else if (sort == 3) {
                                    sorting = 3
                                    audiosList.sortBy {
                                        it.file.lastModified()
                                    }
                                    withContext(Dispatchers.Main) {
                                        imagesAdapter?.notifyDataSetChanged()
                                    }

                                } else if (sort == 4) {
                                    sorting = 4
                                    audiosList.sortByDescending {
                                        it.file.lastModified()
                                    }
                                    withContext(Dispatchers.Main) {
                                        imagesAdapter?.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }, view)
                } else {
                    Toast.makeText(
                        this@AudiosActivity,
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
                                        this@AudiosActivity,
                                        getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(this@AudiosActivity, ProActivity::class.java)
                                    intent.putExtra("from", "main")
                                    startActivity(intent)
                                }
                            }
                        })

                    } else {
                        Toast.makeText(
                            this@AudiosActivity,
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
                        val intent = Intent(this@AudiosActivity, ProActivity::class.java)
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

                } else {
                    if (!fileArr[i].path.contains("Recovery")) {
                        if (fileArr[i].path.endsWith(".opus") ||
                            fileArr[i].path.endsWith(".mp3") ||
                            fileArr[i].path.endsWith(".aac") ||
                            fileArr[i].path.endsWith(".m4a")
                        ) {
                            val file = File(fileArr[i].path)
                            audiosList.add(FilesModel(file, false))
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progressTV.text =
                                    scannedFiles.toString() + getString(R.string.files_scanned) + ", " + audiosList.size + getString(
                                        R.string.audios_found
                                    )
                                imagesAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverAudios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverAudios.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                recoverAudios.remove(item)
            } else {
                recoverAudios.add(item)
            }
        }
        if (!recoverAudios.isEmpty()) {
            binding.recoverlayout.visibility = View.VISIBLE
            binding.countLayout.visibility = View.INVISIBLE
        } else {
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
        }
    }

    fun fetchData() {

        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.getRecoverData()
        }

        mainViewModel.liveDataAudiosList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            audiosList.clear()
            audiosList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataIsDataLoaded.observe(this) {
            if (it) {
//                audiosList.addAll(mainViewModel.audiosList)
                try {
                    audiosList.sortByDescending {
                        it.file.lastModified()
                    }

                    imagesAdapter?.setIsDataLoaded()
                    dataLoaded = true
                    binding.progressLayout.visibility = View.GONE

                    completeScanningDialog(this@AudiosActivity, audiosList.size,  mainViewModel.scannedFiles)
                    if (!audiosList.isEmpty()) {
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
                if (Utils.getFileList(path) != null){
                    val job = CoroutineScope(Dispatchers.IO).async {
                        checkFileOfDirectory(
                            Utils.getFileList(path)
                        )
                    }
                    job.await()
                    audiosList.sortByDescending {
                        it.file.lastModified()
                    }
                    withContext(Dispatchers.Main){
                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded = true
                        binding.progressLayout.visibility = View.GONE

                        completeScanningDialog(this@AudiosActivity,audiosList.size,scannedFiles)
                        if(!audiosList.isEmpty()){
                            binding.countLayout.visibility =View.VISIBLE
                            binding.recoverlayout.visibility =View.INVISIBLE
                        }else{
                            binding.recoverlayout.visibility =View.GONE
                            binding.countLayout.visibility =View.GONE
                            binding.noRecordFountTv.visibility =View.VISIBLE

                        }
                    }
                }
            }
        } catch (e: Exception) {
            binding.progressLayout.visibility = View.GONE
            Log.e("Exception", "doInBackground: " + e.message)
        }*/
    }

    fun playAudioDialog(file: File) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.audio_player_layout, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(true)
        val name = view.findViewById<TextView>(R.id.titleTV)
        val next = view.findViewById<ImageView>(R.id.nextIV)
        val previous = view.findViewById<ImageView>(R.id.previousIV)
        val rewind = view.findViewById<ImageView>(R.id.minusIV)
        val forward = view.findViewById<ImageView>(R.id.plusIV)
        val play = view.findViewById<ImageView>(R.id.playIV)
        val seekForwardTime = 5000; // 5000 milliseconds
        val seekBackwardTime = 5000; // 5000 milliseconds
        seekbar = view.findViewById<SeekBar>(R.id.seekBar)
        name.text = file.name
        try {
            mediaPlayer.setDataSource(file.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        val finalTime = mediaPlayer.duration;
        startTime = mediaPlayer.currentPosition.toDouble();
        seekbar!!.progress = startTime.toInt()
        seekbar!!.max = finalTime / 1000
        val mHandler = Handler()
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val mCurrentPosition: Int = mediaPlayer.currentPosition / 1000
                    seekbar!!.progress = mCurrentPosition
                }
                mHandler.postDelayed(this, 100)
            }
        })
        seekbar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000)
                }
            }
        })
        imageDialog.window!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.seekTo(0)
            play.setImageResource(R.drawable.ic_play)
        }
        next.setOnClickListener {
            // get current song position
            // check if next song is there or not
            if (currentIndex < (audiosList.size - 1)) {
                currentIndex += 1;
                val nextFile = audiosList[currentIndex].file
                try {
                    name.text = nextFile.name
                    play.setImageResource(R.drawable.ic_play)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(nextFile.path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

        }
        previous.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex -= 1;
                val previousFile = audiosList[currentIndex].file
                try {
                    name.text = previousFile.name
                    play.setImageResource(R.drawable.ic_play)
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(previousFile.path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        rewind.setOnClickListener {
            val currentPosition: Int = mediaPlayer.currentPosition
            // check if seekBackward time is greater than 0 sec
            // check if seekBackward time is greater than 0 sec
            if (currentPosition - seekBackwardTime >= 0) {
                // forward song
                mediaPlayer.seekTo(currentPosition - seekBackwardTime)
            } else {
                // backward to starting position
                mediaPlayer.seekTo(0)
            }

        }
        forward.setOnClickListener {
            val currentPosition: Int = mediaPlayer.currentPosition
            if (currentPosition + seekForwardTime <= mediaPlayer.duration) {
                // forward song
                mediaPlayer.seekTo(currentPosition + seekForwardTime)
            } else {
                // forward to end position
                mediaPlayer.seekTo(mediaPlayer.duration)
            }

        }
        play.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause();
                play.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start();
                play.setImageResource(R.drawable.ic_pause)
            }
        }
        imageDialog.setOnDismissListener {
            mediaPlayer.stop()
        }
        imageDialog.show()

    }

    fun findFileIndex(arr: ArrayList<FilesModel>, item: FilesModel): Int {
        return arr.indexOf(item)
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
                    if (audiosList.size > 500 && !AppPreferences.getInstance(this@AudiosActivity).isAppPurchased) {
                        binding.viewMoreLayout.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    fun createReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this@AudiosActivity)
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
            var flow: Task<Void> =
                reviewManager!!.launchReviewFlow(this@AudiosActivity, reviewInfo!!)
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
            this@AudiosActivity,
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
            interstitialAdd!!.show(this@AudiosActivity)
        } else {
            dataListener.onRecieve(false)
        }
    }
}