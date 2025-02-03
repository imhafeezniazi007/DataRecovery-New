package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityCleanVediosBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.utils.Utils
import com.example.datarecovery.viewmodels.HomeViewModel
import com.example.datarecovery.views.adapters.ImagesAdapter
import com.google.api.client.http.FileContent
import com.razzaghimahdi78.dotsloading.circle.LoadingCircleFady
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class CleanVediosActivity : BaseActivity() {
    lateinit var binding: ActivityCleanVediosBinding
    var deletedVedios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var cleanVedios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var savedVediosList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: ImagesAdapter? = null
    var fromClean = false
    var path = ""
    var scannedFiles = 0
    var sorting = 4
    var dataLoaded = false
    lateinit var mainViewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCleanVediosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = HomeViewModel(this)
        setmTheme()
        val from = intent.getStringExtra("from")
        if (from.equals("clean")) {
            fromClean = true
            binding.titleTV.text = getString(R.string.clean_up_vedios)
//            fetchData()
            imagesAdapter =
                ImagesAdapter(
                    this@CleanVediosActivity,
                    cleanVedios,
                    object : DataListener {
                        override fun onRecieve(any: Any) {
                            performLogic(any as FilesModel)

                        }

                        override fun onClick(any: Any) {
                            super.onClick(any)
                            val filesModel = any as FilesModel
                            val intent =
                                Intent(this@CleanVediosActivity, VedioPlayerActivity::class.java)
                            intent.putExtra("path", filesModel.file.path)
                            startActivity(intent)
                        }
                    })
        } else {
            binding.titleTV.text = getString(R.string.saved_vedios)
            fetchData()
            imagesAdapter =
                ImagesAdapter(
                    this@CleanVediosActivity,
                    savedVediosList,
                    object : DataListener {
                        override fun onRecieve(any: Any) {
                            performLogic(any as FilesModel)

                        }

                        override fun onClick(any: Any) {
                            val filesModel = any as FilesModel
                            val intent =
                                Intent(this@CleanVediosActivity, VedioPlayerActivity::class.java)
                            intent.putExtra("path", filesModel.file.path)
                            startActivity(intent)
                        }
                    })
        }
        binding.recylerview.adapter = imagesAdapter

        clickListener()
    }

    private fun performLogic(file: FilesModel) {
        val list = deletedVedios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            deletedVedios.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                deletedVedios.remove(item)
            } else {
                deletedVedios.add(item)
            }
        }

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
            deletedVedios.forEach {
                val isSucces = copy(it.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@CleanVediosActivity, getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    deletedVedios.clear()
                    return@forEach
                } else {
                    deletedVedios.remove(it)
                }
            }
            if (isDeleted) {
                Toast.makeText(
                    this@CleanVediosActivity,
                    getString(R.string.all_files_recover_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        binding.deleteIV.setOnClickListener {
            if (deletedVedios.isEmpty()) {
                Toast.makeText(
                    this@CleanVediosActivity,
                    getString(R.string.no_image_selected),
                    Toast.LENGTH_SHORT
                )
                    .show()

            } else {
                imagesAdapter?.setLongClickFalse()
                var isDeleted = true
                val iterator = deletedVedios.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }
                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@CleanVediosActivity,
                            getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        deletedVedios.clear()
                        break
                    } else {
                        if (fromClean) {
                            cleanVedios.remove(item)
                            imagesAdapter?.notifyDataSetChanged()
                        } else {
                            cleanVedios.remove(item)
                            imagesAdapter?.notifyDataSetChanged()
                        }
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    deletedVedios.clear()
                    MediaScanner(this@CleanVediosActivity)
                    Toast.makeText(
                        this@CleanVediosActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.driveIV.setOnClickListener {
            if (deletedVedios.isEmpty()) {
                Toast.makeText(
                    this@CleanVediosActivity,
                    getString(R.string.no_file_selected),
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
                    if (cleanVedios.isEmpty()) {
                        Toast.makeText(
                            this@CleanVediosActivity,
                            getString(R.string.no_files_found),
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }
                } else {
                    if (savedVediosList.isEmpty()) {
                        Toast.makeText(
                            this@CleanVediosActivity,
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
                                    cleanVedios.sortBy {
                                        it.file.length()
                                    }
                                } else {
                                    savedVediosList.sortBy {
                                        it.file.length()
                                    }
                                }

                            } else if (sort == 2) {
                                sorting = 2
                                if (fromClean) {
                                    cleanVedios.sortByDescending {
                                        it.file.length()
                                    }
                                } else {
                                    savedVediosList.sortByDescending {
                                        it.file.length()
                                    }
                                }

                            } else if (sort == 3) {
                                sorting = 3
                                if (fromClean) {
                                    cleanVedios.sortBy {
                                        it.file.lastModified()
                                    }
                                } else {
                                    savedVediosList.sortBy {
                                        it.file.lastModified()
                                    }
                                }
                            } else if (sort == 4) {
                                sorting = 4
                                if (fromClean) {
                                    cleanVedios.sortByDescending {
                                        it.file.lastModified()
                                    }
                                } else {
                                    savedVediosList.sortByDescending {
                                        it.file.lastModified()
                                    }
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
                    this@CleanVediosActivity,
                    getString(R.string.scanning_please_wait),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.viewMoreLayout.setOnClickListener {
            imagesAdapter?.setIsWatchAd()
            imagesAdapter?.notifyDataSetChanged()
            Constant.watchedAdScannig = true
            binding.viewMoreLayout.visibility = View.GONE
        }
    }

    fun fetchData() {

        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.scannedFiles = 0
            mainViewModel.getSavedData()
        }

        mainViewModel.liveDataSavedVideosList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            savedVediosList.clear()
            savedVediosList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataSavedIsDataLoaded.observe(this) {
            if (it) {
                try {
                    if (fromClean) {
                        cleanVedios.sortByDescending {
                            it.file.lastModified()
                        }
                    } else {
                        savedVediosList.sortByDescending {
                            it.file.lastModified()
                        }
                    }

                    imagesAdapter?.setIsDataLoaded()
                    binding.progressLayout.visibility = View.GONE
                    dataLoaded = true
                    imagesAdapter?.setIsWatchAd()

                    binding.viewMoreLayout.visibility = View.GONE
                    if (fromClean) {
                        cleanCompleteScanningDialog(
                            cleanVedios.size,
                            mainViewModel.scannedFiles,
                            object : DataListener {
                                override fun onRecieve(any: Any) {
                                    val flag = any as Boolean
                                    if (flag) {
                                    }
                                }

                            })

                        if (!cleanVedios.isEmpty()) {
                            binding.countLayout.visibility = View.VISIBLE
                            binding.recoverlayout.visibility = View.INVISIBLE
                        } else {
                            binding.recoverlayout.visibility = View.GONE
                            binding.countLayout.visibility = View.GONE
                            binding.noRecordFountTv.visibility = View.VISIBLE

                        }
                    } else {
                        completeScanningDialog(
                            this@CleanVediosActivity,
                            savedVediosList.size,
                            scannedFiles
                        )
                        if (!savedVediosList.isEmpty()) {
                            binding.countLayout.visibility = View.VISIBLE
                            binding.recoverlayout.visibility = View.INVISIBLE
                        } else {
                            binding.recoverlayout.visibility = View.GONE
                            binding.countLayout.visibility = View.GONE
                            binding.noRecordFountTv.visibility = View.VISIBLE

                        }
                    }


                } catch (e: Exception) {
                    binding.progressLayout.visibility = View.GONE
                    Log.e("Exception", "doInBackground: " + e.message)
                }
            }
        }


        /*if (fromClean) {
            path = Environment.getExternalStorageDirectory().absolutePath
        } else {
//            path = File(Environment.getExternalStorageDirectory().toString() + "/Recovery").toString()
            path =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Recovery").toString()

        }
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
                    if(fromClean){
                        cleanVedios.sortByDescending {
                            it.file.lastModified()
                        }
                    }else{
                        savedVediosList.sortByDescending {
                            it.file.lastModified()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        imagesAdapter?.setIsDataLoaded()
                        binding.progressLayout.visibility = View.GONE
                        dataLoaded = true
                        imagesAdapter?.setIsWatchAd()

                        binding.viewMoreLayout.visibility = View.GONE
                        if(fromClean) {
                            cleanCompleteScanningDialog(cleanVedios.size,scannedFiles,object :DataListener{
                                override fun onRecieve(any: Any) {
                                    val flag = any as Boolean
                                    if(flag){
                                    }
                                }

                            })

                            if(!cleanVedios.isEmpty()){
                                binding.countLayout.visibility =View.VISIBLE
                                binding.recoverlayout.visibility =View.INVISIBLE
                            }else{
                                binding.recoverlayout.visibility =View.GONE
                                binding.countLayout.visibility =View.GONE
                                binding.noRecordFountTv.visibility =View.VISIBLE

                            }
                        }else{
                            completeScanningDialog(this@CleanVediosActivity,savedVediosList.size,scannedFiles)
                            if(!savedVediosList.isEmpty()){
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

                    } else {
                        if (fileArr[i].path.endsWith(".mkv") ||
                            fileArr[i].path.endsWith(".mp4")
                        ) {
                            val file = File(fileArr[i].path)
                            if (fromClean) {
                                if (!file.path.contains("Recovery")) {
                                    cleanVedios.add(FilesModel(file, false))
                                }
                            } else {
                                savedVediosList.add(FilesModel(file, false))
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                if (fromClean) {
                                    binding.progressTV.text =
                                        scannedFiles.toString() + getString(R.string.files_scanned) + " , " + cleanVedios.size + getString(
                                            R.string.videos_found
                                        )
                                } else {
                                    binding.progressTV.text =
                                        scannedFiles.toString() + getString(R.string.files_scanned) + " , " + savedVediosList.size + getString(
                                            R.string.videos_found
                                        )
                                }
                                imagesAdapter?.notifyDataSetChanged()
                            }
                        }
                    }

                }

            }
        }
    }

    fun uploadFilesToGDrive() {
        getDriveService()?.let { googleDriveService ->
            showHideProgress(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    isFolderPresent().let { id ->
                        for (item in deletedVedios) {
                            val actualFfile = item.file
                            val fileMetadata = com.google.api.services.drive.model.File()
                            fileMetadata.setName(actualFfile.name)
                            fileMetadata.setParents(Collections.singletonList(id))
                            val mediaContent = FileContent(
                                "video/mp4",
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
                                this@CleanVediosActivity, getString(R.string.files_uploaded),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        deletedVedios.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showHideProgress(false)
                        Toast.makeText(
                            this@CleanVediosActivity, e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    deletedVedios.clear()

                    Log.d("TAG", "uploadFileToGDrive: ${e.message}")
                }
            }
        } ?: Toast.makeText(
            this@CleanVediosActivity, getString(R.string.drive_login_error),
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