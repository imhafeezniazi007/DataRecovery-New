package com.example.datarecovery.views.activities

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView

import com.example.datarecovery.BuildConfig
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityDocumentsBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.DocumentFileModel
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
import com.razzaghimahdi78.dotsloading.circle.LoadingCircleFady
import kotlinx.coroutines.*
import java.io.File


class DocumentsActivity : BaseActivity() {
    lateinit var binding: ActivityDocumentsBinding
    var recoverDocumets: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var documentsList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: AudiosAdapter? = null
    val path = Environment.getExternalStorageDirectory().absolutePath
    var scannedFiles = 0
    var sorting = 4
    var dataLoaded = false
    private var retryAttempt = 0.0
    var reviewManager: ReviewManager? = null
    var reviewInfo: ReviewInfo? = null
    lateinit var mainViewModel: HomeViewModel
    var interstitialAdd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()

        fetchData()
        imagesAdapter = AudiosAdapter(this@DocumentsActivity, documentsList, object : DataListener {
            override fun onRecieve(any: Any) {
                performLogic(any as FilesModel)
            }

            override fun onClick(any: Any) {
                super.onClick(any)
                val file = any as FilesModel
                openFile(file.file)
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
                                                this@DocumentsActivity,
                                                getString(R.string.something_went_wrong),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            val intent = Intent(this@DocumentsActivity, ProActivity::class.java)
                                            intent.putExtra("from", "main")
                                            startActivity(intent)
                                        }
                                    }
                                })
                            } else {
                                Toast.makeText(
                                    this@DocumentsActivity,
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

                            if(any as Boolean){
                                val intent  = Intent(this@DocumentsActivity, ProActivity::class.java)
                                intent.putExtra("from","main")
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
        if(Constant.watchedAdScannig){
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
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {

                } else {
                    Log.d("checkFileOfDirectory", "checkFileOfDirectory: ${fileArr[i].path}")
                    if(!fileArr[i].path.contains("Recovery")) {
                        if (fileArr[i].path.endsWith(".pdf") ||
                            fileArr[i].path.endsWith(".docx") ||
                            fileArr[i].path.endsWith(".doc")||
                            fileArr[i].path.endsWith(".txt")
                        ) {
                            val file = File(fileArr[i].path)
                            documentsList.add(FilesModel(file, false))
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progressTV.text =   scannedFiles.toString()+getString(R.string.files_scanned) + " , " + documentsList.size + getString(R.string.documents_found)
                                imagesAdapter?.notifyDataSetChanged()

                            }

                        }
                    }


                }
            }
        }

    }

    fun openFile(file: File) {
        // Get URI and MIME type of file
        val uri: Uri =
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        val mime = contentResolver.getType(uri)

        // Open file with user selected app
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun performLogic(file: FilesModel) {
        val list = recoverDocumets.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            recoverDocumets.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                recoverDocumets.remove(item)
            } else {
                recoverDocumets.add(item)
            }
        }
        if (!recoverDocumets.isEmpty()) {
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

            val iterator = recoverDocumets.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = copy(item.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@DocumentsActivity,
                        getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    recoverDocumets.clear()
                    break
                } else {
                    iterator.remove()
                }

            }
            if (isDeleted) {
                if(Constant.showInAppReviewDocuments == 10){
                    Constant.showInAppReviewDocuments = 0
                    showReviewInfo()
                }
                if(Constant.showInAppReviewDocuments == 0 && recoverDocumets.size > 10){
                    Constant.showInAppReviewDocuments =11
                    showReviewInfo()
                }
                recoverDocumets.clear()
                MediaScanner(this@DocumentsActivity)
                showSnackbar()
            }

        }
        binding.sortIV.setOnClickListener { view ->
            if (dataLoaded) {
                if(documentsList.isEmpty()) {
                    Toast.makeText(
                        this@DocumentsActivity,
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
                                documentsList.sortBy {
                                    it.file.length()
                                }
                            } else if (sort == 2) {
                                sorting = 2
                                documentsList.sortByDescending {
                                    it.file.length()
                                }
                            } else if (sort == 3) {
                                sorting = 3
                                documentsList.sortBy {
                                    it.file.lastModified()
                                }
                            } else if (sort == 4) {
                                sorting = 4
                                documentsList.sortByDescending {
                                    it.file.lastModified()
                                }
                            }
                            withContext(Dispatchers.Main){
                                imagesAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }, view)
            } else {
                Toast.makeText(
                    this@DocumentsActivity,
                    getString(R.string.scanning_please_wait),
                    Toast.LENGTH_LONG
                ).show()
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
                                        this@DocumentsActivity,
                                        getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(this@DocumentsActivity, ProActivity::class.java)
                                    intent.putExtra("from", "main")
                                    startActivity(intent)
                                }
                            }
                        })
                    } else {
                        Toast.makeText(
                            this@DocumentsActivity,
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

                    if(any as Boolean){
                        val intent  = Intent(this@DocumentsActivity, ProActivity::class.java)
                        intent.putExtra("from","main")
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

        mainViewModel.liveDataDocumentsList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
//            imagesAdapter.updateItems(it)

            documentsList.clear()
            documentsList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataIsDataLoaded.observe(this) {
            if (it) {
//                documentsList.addAll(mainViewModel.documentsList)
                try {
                            documentsList.sortByDescending {
                                it.file.lastModified()
                            }

                                imagesAdapter?.setIsDataLoaded()
                                dataLoaded = true
                                binding.progressLayout.visibility = View.GONE

                                completeScanningDialog(this@DocumentsActivity,documentsList.size, mainViewModel.scannedFiles)
                                if(!documentsList.isEmpty()){
                                    binding.countLayout.visibility =View.VISIBLE
                                    binding.recoverlayout.visibility =View.INVISIBLE
                                }else{
                                    binding.recoverlayout.visibility =View.GONE
                                    binding.countLayout.visibility =View.GONE
                                    binding.noRecordFountTv.visibility =View.VISIBLE
                                }


                } catch (e: Exception) {
                    binding.progressLayout.visibility = View.GONE
                    Log.e("Exception", "doInBackground: " + e.message)
                }
            }
        }

       /* val LoadingWavy = findViewById<LoadingCircleFady>(R.id.circleProgress)
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
                    documentsList.sortByDescending {
                        it.file.lastModified()
                    }
                    withContext(Dispatchers.Main) {
                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded = true
                        binding.progressLayout.visibility = View.GONE

                        completeScanningDialog(this@DocumentsActivity,documentsList.size,scannedFiles)
                        if(!documentsList.isEmpty()){
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
    fun setmTheme(){
        val mtheme = AppPreferences.getInstance(this).getTheme
        if(mtheme == 0){
            binding.mainLayout.background = resources.getDrawable(R.drawable.light_theme_bg)
        }else if(mtheme == 1){
            binding.mainLayout.background = resources.getDrawable(R.drawable.dark_theme_bg)
        }else if(mtheme == 2){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme1_bg)
        }else if(mtheme == 3){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme2_bg)
        }else if(mtheme == 4){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme3)
        }else if(mtheme == 5){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme4)
        }else if(mtheme == 6){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme5)
        }
    }

     fun getDocuments(context: Context): List<File> {
         try {

             val fileList = mutableListOf<DocumentFileModel>()
             val files = mutableListOf<File>()
             val collection = MediaStore.Files.getContentUri("external");

             val projection = arrayOf(
                 MediaStore.Audio.Media._ID,
                 MediaStore.Audio.Media.DISPLAY_NAME,
                 MediaStore.Audio.Media.DATA,
                 MediaStore.Audio.Media.SIZE
             )


// exclude media files, they would be here also.

// exclude media files, they would be here also.
             val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
             val mimeType2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk")
             val mimeType3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")

             val pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
             val doc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")
             val docx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
             val xlsx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")
             val txt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
             val ppt = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")
             val pptx = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx")


             val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + " IN(?,?,?,?,?,?,?)"

             val selectionArgsPdf = arrayOf(pdf, doc, docx, xlsx, txt, ppt, pptx)
             val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

             val query = contentResolver.query(
                 collection,
                 projection,
                 selectionMimeType,
                 selectionArgsPdf,
                 sortOrder
             )

             query?.use { cursor ->
                 // Cache column indices.
                 val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                 val nameColumn =
                     cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

                 val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                 val data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                 while (cursor.moveToNext()) {
                     try {// Get values of columns for a given video.
                         val id = cursor.getLong(idColumn)
                         val name = cursor.getString(nameColumn)
                         val size = cursor.getInt(sizeColumn)
                         val data = cursor.getString(data)
//                val contentUri: Uri = ContentUris.withAppendedId(
//                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                    id
//                )

                         // Stores column values and the contentUri in a local object
                         // that represents the media file.

                         //val fileLink = FileUtil.getFilePath( context,contentUri )
                         Log.d("TAG", "getDocuments: ${name}")
                         files += File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)))
                         fileList += DocumentFileModel(data, name, size)
                     } catch (e: java.lang.Exception) {
                     }
                 }
                 //    done.value=true
             }
             return files
         }catch (ex:Exception){
             Log.d("TAG", "getDocuments: ${ex.message}")
             val files = mutableListOf<File>()
             return files

         }
    }
    fun scrollListener(){
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if(documentsList.size>500 && !AppPreferences.getInstance(this@DocumentsActivity).isAppPurchased){
                        binding.viewMoreLayout.visibility = View.VISIBLE
                    }

                }
            }
        })
    }

    fun createReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this@DocumentsActivity)
        val resuest: Task<ReviewInfo> = reviewManager!!.requestReviewFlow()
        resuest.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            }else{
                if(BuildConfig.DEBUG){
                    Toast.makeText(this,"Review info not recieved",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun showReviewInfo(){
        if(reviewInfo != null){
            var flow : Task<Void> = reviewManager!!.launchReviewFlow(this@DocumentsActivity,reviewInfo!!)
            flow.addOnCompleteListener {task ->
                if(BuildConfig.DEBUG) {
                    Toast.makeText(this, "Review successfull", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun initAds(){
        val adRequest: AdRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this@DocumentsActivity,
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
            interstitialAdd!!.show(this@DocumentsActivity)
        } else {
            dataListener.onRecieve(false)
        }
    }
}