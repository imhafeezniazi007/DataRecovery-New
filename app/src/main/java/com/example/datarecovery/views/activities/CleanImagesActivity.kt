package com.example.datarecovery.views.activities

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityCleanImagesBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.utils.Utils
import com.example.datarecovery.viewmodels.HomeViewModel
import com.example.datarecovery.views.adapters.ImagesAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.api.client.http.FileContent
import com.razzaghimahdi78.dotsloading.circle.LoadingCircleFady
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CleanImagesActivity : BaseActivity() {
    lateinit var binding: ActivityCleanImagesBinding
    var deletePhotos: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var cleanPhotos: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var savedImagesList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: ImagesAdapter? = null
    var path = ""
    var fromClean = false
    var scannedFiles = 0
    var sorting = 4
    var dataLoaded = false
    lateinit var mainViewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCleanImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()
        val from = intent.getStringExtra("from")
        if (from.equals("clean")) {
            fromClean = true
            binding.titleTV.text = getString(R.string.clean_up_photos)
//            fetchData()
            imagesAdapter =
                ImagesAdapter(this@CleanImagesActivity, cleanPhotos, object :
                    DataListener {
                    override fun onRecieve(any: Any) {
                        performLogic(any as FilesModel)

                    }

                    override fun onClick(any: Any) {
                        val filesModel = any as FilesModel
                        openImage(filesModel, true)
                    }
                })
        } else {
            binding.titleTV.text = getString(R.string.saved_images)
            fetchData()
            imagesAdapter = ImagesAdapter(this@CleanImagesActivity, savedImagesList, object :
                DataListener {
                override fun onRecieve(any: Any) {
                    performLogic(any as FilesModel)

                }

                override fun onClick(any: Any) {
                    val filesModel = any as FilesModel
                    openImage(filesModel, false)
                }
            })
        }
        binding.recylerview.adapter = imagesAdapter


        clickListener()

    }

    private fun clickListener() {
        binding.backIV.setOnClickListener {
            onBackPressed()
        }
        binding.recoverlayout.setOnClickListener {
            imagesAdapter?.setLongClickFalse()
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            var isDeleted = true


            val iterator = deletePhotos.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = copy(item.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@CleanImagesActivity, getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    deletePhotos.clear()
                    break
                } else {
                    iterator.remove()
                }

            }
            if (isDeleted) {
                deletePhotos.clear()
                val dst = File(
                    destinationDirectory.toString()
                )
                MediaScanner(this@CleanImagesActivity)
                Toast.makeText(
                    this@CleanImagesActivity,
                    getString(R.string.all_files_recover_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        binding.deleteIV.setOnClickListener {
            if (deletePhotos.isEmpty()) {
                Toast.makeText(
                    this@CleanImagesActivity,
                    getString(R.string.no_item_selected),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                imagesAdapter?.setLongClickFalse()
                var isDeleted = true
                val iterator = deletePhotos.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }

                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@CleanImagesActivity, getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        deletePhotos.clear()
                        break
                    } else {
                        if (fromClean) {
                            cleanPhotos.remove(item)
                            imagesAdapter?.notifyDataSetChanged()
                        } else {
                            savedImagesList.remove(item)
                            imagesAdapter?.notifyDataSetChanged()
                        }
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    deletePhotos.clear()
                    MediaScanner(this@CleanImagesActivity)
                    Toast.makeText(
                        this@CleanImagesActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.driveIV.setOnClickListener {
            if (deletePhotos.isEmpty()) {
                Toast.makeText(
                    this@CleanImagesActivity,
                    getString(R.string.no_image_selected),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                imagesAdapter?.setLongClickFalse()
                uploadFilesToGDrive()
            }
        }
        binding.sortIV.setOnClickListener { view ->
            if (dataLoaded) {
                if (fromClean) {
                    if (cleanPhotos.isEmpty()) {
                        Toast.makeText(
                            this@CleanImagesActivity,
                            getString(R.string.no_files_found),
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                } else {
                    if (savedImagesList.isEmpty()) {
                        Toast.makeText(
                            this@CleanImagesActivity,
                            getString(R.string.no_files_found),
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                }
                sortingPopup(sorting, object : DataListener {
                    override fun onRecieve(any: Any) {
                        val sort = any as Int
                        CoroutineScope(Dispatchers.IO).launch {
                            if (sort == 1) {
                                sorting = 1
                                if (fromClean) {
                                    cleanPhotos.sortBy {
                                        it.file.length()
                                    }
                                } else {
                                    savedImagesList.sortBy {
                                        it.file.length()
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }


                            } else if (sort == 2) {
                                sorting = 2
                                if (fromClean) {
                                    cleanPhotos.sortByDescending {
                                        it.file.length()
                                    }
                                } else {
                                    savedImagesList.sortByDescending {
                                        it.file.length()
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 3) {
                                sorting = 3
                                if (fromClean) {
                                    cleanPhotos.sortBy {
                                        it.file.lastModified()
                                    }
                                } else {
                                    savedImagesList.sortBy {
                                        it.file.lastModified()
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 4) {
                                sorting = 4
                                if (fromClean) {
                                    cleanPhotos.sortByDescending {
                                        it.file.lastModified()
                                    }
                                } else {
                                    savedImagesList.sortByDescending {
                                        it.file.lastModified()
                                    }
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
                    this@CleanImagesActivity,
                    getString(R.string.scanning_please_wait),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.backIV.setOnClickListener {
            finish()
        }
    }


    private fun performLogic(file: FilesModel) {
        val list = deletePhotos.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            deletePhotos.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                deletePhotos.remove(item)
            } else {
                deletePhotos.add(item)
            }
        }

    }

    fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.scannedFiles = 0
            mainViewModel.getSavedData()
        }

        mainViewModel.liveDataSavedImagesList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            savedImagesList.clear()
            savedImagesList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataSavedIsDataLoaded.observe(this) {
            if (it) {
                try {
                    if (fromClean) {
                        cleanPhotos.sortByDescending {
                            it.file.lastModified()
                        }
                    } else {
                        savedImagesList.sortByDescending {
                            it.file.lastModified()
                        }

                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded = true
                        imagesAdapter?.setIsWatchAd()
                        binding.progressLayout.visibility = View.GONE
                        if (fromClean) {
                            cleanCompleteScanningDialog(
                                cleanPhotos.size,
                                scannedFiles,
                                object : DataListener {
                                    override fun onRecieve(any: Any) {
                                        val flag = any as Boolean
                                        if (flag) {
                                        }
                                    }

                                })

                            if (!cleanPhotos.isEmpty()) {
                                binding.countLayout.visibility = View.VISIBLE
                                binding.recoverlayout.visibility = View.INVISIBLE
                            } else {
                                binding.recoverlayout.visibility = View.GONE
                                binding.countLayout.visibility = View.GONE
                                binding.noRecordFountTv.visibility = View.VISIBLE

                            }
                        } else {
                            completeScanningDialog(
                                this@CleanImagesActivity,
                                savedImagesList.size,
                                mainViewModel.scannedFiles
                            )
                            if (!savedImagesList.isEmpty()) {
                                binding.countLayout.visibility = View.VISIBLE
                                binding.recoverlayout.visibility = View.INVISIBLE
                            } else {
                                binding.recoverlayout.visibility = View.GONE
                                binding.countLayout.visibility = View.GONE
                                binding.noRecordFountTv.visibility = View.VISIBLE

                            }
                        }
                    }

                } catch (e: Exception) {
                    binding.progressLayout.visibility = View.GONE
                    Log.e("Exception", "doInBackground: " + e.message)
                }
            }
        }

        /*if(fromClean){
            path = Environment.getExternalStorageDirectory().absolutePath
        }else{
//            path = File(Environment.getExternalStorageDirectory().toString() + "/Recovery").toString()
            path =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Recovery").toString()

        }
        val LoadingWavy = findViewById<LoadingCircleFady>(R.id.circleProgress)
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
                    if(fromClean){
                        cleanPhotos.sortByDescending {
                            it.file.lastModified()
                        }
                    }else{
                        savedImagesList.sortByDescending {
                            it.file.lastModified()
                        }
                    }
                    withContext(Dispatchers.Main){
                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded =true
                        imagesAdapter?.setIsWatchAd()
                        binding.progressLayout.visibility = View.GONE
                        if(fromClean) {
                            cleanCompleteScanningDialog(cleanPhotos.size,scannedFiles,object :DataListener{
                                override fun onRecieve(any: Any) {
                                    val flag = any as Boolean
                                    if(flag){
                                    }
                                }

                            })

                            if(!cleanPhotos.isEmpty()){
                                binding.countLayout.visibility =View.VISIBLE
                                binding.recoverlayout.visibility =View.INVISIBLE
                            }else{
                                binding.recoverlayout.visibility =View.GONE
                                binding.countLayout.visibility =View.GONE
                                binding.noRecordFountTv.visibility =View.VISIBLE

                            }
                        }else{
                            completeScanningDialog(this@CleanImagesActivity,savedImagesList.size,scannedFiles)
                            if(!savedImagesList.isEmpty()){
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

            }

        } catch (e: Exception) {
            binding.progressLayout.visibility = View.GONE
            Log.e("Exception", "doInBackground: " + e.message)
        }*/

    }

    fun checkFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) {
            for (i in fileArr.indices) {
                Log.d("TAG", "checkFileOfDirectory: ${fileArr[i].path}")
                if (fileArr[i].isDirectory) {
                    checkFileOfDirectory(Utils.getFileList(fileArr[i].path))
                } else {
                    scannedFiles += 1
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(fileArr[i].path, options)
                    if (!(options.outWidth == -1 || options.outHeight == -1)) {
                        val file = File(fileArr[i].path)
                        if (fromClean) {
                            if (!file.path.contains("Recovery")) {
                                cleanPhotos.add(FilesModel(file, false))
                            }
                        } else {
                            savedImagesList.add(FilesModel(file, false))
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            if (fromClean) {
                                binding.progressTV.text =
                                    scannedFiles.toString() + getString(R.string.files_scanned) + ", " + cleanPhotos.size + getString(
                                        R.string.images_found
                                    )
                            } else {
                                binding.progressTV.text =
                                    scannedFiles.toString() + getString(R.string.files_scanned) + " , " + savedImagesList.size + getString(
                                        R.string.images_found
                                    )
                            }
                            imagesAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    fun openImage(filesModel: FilesModel, fromClean: Boolean) {
        val imageDialog: AlertDialog?
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
        recoverBtn.visibility = View.GONE
        modifiedTV.text = dateFormated
        try {
            Glide.with(this@CleanImagesActivity)
                .load("file://" + file.path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .centerCrop()
                .error(R.drawable.home)
                .into(previewIV)
        } catch (e: Exception) {
            //do nothing
            Toast.makeText(this@CleanImagesActivity, e.message, Toast.LENGTH_SHORT).show()
        }
        recoverBtn.setOnClickListener {
            val isSucces = copy(file)
            if (!isSucces) {
                Toast.makeText(
                    this@CleanImagesActivity,
                    getString(R.string.try_again_later),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    this@CleanImagesActivity,
                    getString(R.string.recover_image_successfully),
                    Toast.LENGTH_SHORT
                ).show()

            }
            imageDialog.dismiss()
        }
        deleteBtnn.setOnClickListener {
            val isSucces = file.delete()
            if (!isSucces) {
                Toast.makeText(
                    this@CleanImagesActivity, getString(R.string.try_again_later),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                if (fromClean) {
                    cleanPhotos.remove(filesModel)
                    savedImagesList.remove(filesModel)
                } else {
                    savedImagesList.remove(filesModel)
                }
                imagesAdapter?.notifyDataSetChanged()
                Toast.makeText(
                    this@CleanImagesActivity, getString(R.string.image_deleted_successfully),
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

    fun uploadFilesToGDrive() {
        getDriveService()?.let { googleDriveService ->
            showHideProgress(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    isFolderPresent().let { id ->
                        for (item in deletePhotos) {
                            val actualFfile = item.file
                            val fileMetadata = com.google.api.services.drive.model.File()
                            fileMetadata.setName(actualFfile.name)
                            fileMetadata.setParents(Collections.singletonList(id))
                            val mediaContent = FileContent(
                                "image/jpeg",
                                actualFfile
                            )
                            val file = googleDriveService.files().create(fileMetadata, mediaContent)
                                .setFields("id")
                                .execute()
                            Log.d("TAG", "uploadFilesToGDrive: ${file.id}")

                        }
                        withContext(Dispatchers.Main) {
                            showHideProgress(false)
                            Toast.makeText(
                                this@CleanImagesActivity, getString(R.string.files_uploaded),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        deletePhotos.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showHideProgress(false)
                        Toast.makeText(
                            this@CleanImagesActivity, e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    deletePhotos.clear()

                    Log.d("TAG", "uploadFileToGDrive: ${e.message}")
                }
            }
        } ?: Toast.makeText(
            this@CleanImagesActivity, getString(R.string.drive_login_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setmTheme() {
        var mtheme = AppPreferences.getInstance(this).getTheme
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

}