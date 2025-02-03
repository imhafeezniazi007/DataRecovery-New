package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.datarecovery.BuildConfig
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityCleanDocumentsBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.utils.Utils
import com.example.datarecovery.viewmodels.HomeViewModel
import com.example.datarecovery.views.adapters.AudiosAdapter
import com.google.api.client.http.FileContent
import com.razzaghimahdi78.dotsloading.circle.LoadingCircleFady
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class CleanDocumentsActivity : BaseActivity() {
    var deletedDocumets: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var savedDocumentsList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: AudiosAdapter? = null
    lateinit var binding: ActivityCleanDocumentsBinding
    var scannedFiles = 0

    //    val path = File(Environment.getExternalStorageDirectory().toString() + "/Recovery").toString()
    val path = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + "/Recovery"
    ).toString()

    var sorting = 4
    var dataLoaded = false
    lateinit var mainViewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCleanDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()

        fetchData()
        imagesAdapter = AudiosAdapter(this@CleanDocumentsActivity, savedDocumentsList, object :
            DataListener {
            override fun onRecieve(any: Any) {

                performLogic(any as FilesModel)

            }

            override fun onClick(any: Any) {
                super.onClick(any)
                val file = any as FilesModel
                openFile(file.file)
            }
        })
        binding.recylerview.adapter = imagesAdapter
        binding.backIV.setOnClickListener {
            finish()
        }

        clickListener()
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
        val list = deletedDocumets.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            deletedDocumets.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                deletedDocumets.remove(item)
            } else {
                deletedDocumets.add(item)
            }
        }
    }

    private fun clickListener() {
        binding.recoverlayout.setOnClickListener {
            imagesAdapter?.setLongClickFalse()
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            var isDeleted = true
            val iterator = deletedDocumets.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = item.file.delete()
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@CleanDocumentsActivity,
                        getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    deletedDocumets.clear()
                    break
                } else {
                    savedDocumentsList.remove(item)
                    imagesAdapter?.notifyDataSetChanged()
                    iterator.remove()
                }

            }
            if (isDeleted) {
                deletedDocumets.clear()
                MediaScanner(this@CleanDocumentsActivity)
                Toast.makeText(
                    this@CleanDocumentsActivity,
                    getString(R.string.all_files_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        binding.deleteIV.setOnClickListener {
            if (deletedDocumets.isEmpty()) {
                Toast.makeText(
                    this@CleanDocumentsActivity,
                    getText(R.string.no_file_selected),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                imagesAdapter?.setLongClickFalse()
                var isDeleted = true
                val iterator = deletedDocumets.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }
                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@CleanDocumentsActivity, getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        deletedDocumets.clear()
                        break
                    } else {
                        savedDocumentsList.remove(item)
                        imagesAdapter?.notifyDataSetChanged()
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    deletedDocumets.clear()
                    imagesAdapter?.notifyDataSetChanged()
                    MediaScanner(this@CleanDocumentsActivity)
                    Toast.makeText(
                        this@CleanDocumentsActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.driveIV.setOnClickListener {
            if (deletedDocumets.isEmpty()) {
                Toast.makeText(
                    this@CleanDocumentsActivity,
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
                if (savedDocumentsList.isEmpty()) {
                    Toast.makeText(
                        this@CleanDocumentsActivity,
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
                                savedDocumentsList.sortBy {
                                    it.file.length()
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 2) {
                                sorting = 2
                                savedDocumentsList.sortByDescending {
                                    it.file.length()
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 3) {
                                sorting = 3
                                savedDocumentsList.sortBy {
                                    it.file.lastModified()
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 4) {
                                sorting = 4
                                savedDocumentsList.sortByDescending {
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
                    this@CleanDocumentsActivity,
                    getString(R.string.scanning_please_wait),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.backIV.setOnClickListener {
            onBackPressed()
        }
    }

    fun fetchData() {

        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.scannedFiles = 0
            mainViewModel.getSavedData()
        }

        mainViewModel.liveDataSavedDocumentsList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            savedDocumentsList.clear()
            savedDocumentsList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataSavedIsDataLoaded.observe(this) {
            if (it) {
                try {
                    savedDocumentsList.sortByDescending {
                        it.file.lastModified()
                    }

                    imagesAdapter?.setIsDataLoaded()
                    binding.progressLayout.visibility = View.GONE
                    dataLoaded = true
                    imagesAdapter?.setIsWatchAd()
                    completeScanningDialog(
                        this@CleanDocumentsActivity,
                        savedDocumentsList.size,
                        mainViewModel.scannedFiles
                    )
                    if (!savedDocumentsList.isEmpty()) {
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
                    savedDocumentsList.sortByDescending {
                        it.file.lastModified()
                    }
                    withContext(Dispatchers.Main){
                        imagesAdapter?.setIsDataLoaded()
                        binding.progressLayout.visibility = View.GONE
                        dataLoaded =true
                        imagesAdapter?.setIsWatchAd()
                        completeScanningDialog(this@CleanDocumentsActivity,savedDocumentsList.size,scannedFiles)
                        if(!savedDocumentsList.isEmpty()){
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

    fun checkFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) {
            for (i in fileArr.indices) {
                Log.d("TAG", "checkFileOfDirectory: ${fileArr[i].path}")
                if (fileArr[i].isDirectory) {
                    checkFileOfDirectory(Utils.getFileList(fileArr[i].path))
                } else {
                    Log.d("TAG", "checkFileOfDirectory: ${fileArr[i].path}")
                    scannedFiles += 1
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(fileArr[i].path, options)
                    if (!(options.outWidth == -1 || options.outHeight == -1)) {

                    } else {
                        if (fileArr[i].path.endsWith(".pdf") ||
                            fileArr[i].path.endsWith(".docx") ||
                            fileArr[i].path.endsWith(".doc") ||
                            fileArr[i].path.endsWith(".txt")
                        ) {
                            val file = File(fileArr[i].path)
                            savedDocumentsList.add(FilesModel(file, false))
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progressTV.text =
                                    scannedFiles.toString() + getString(R.string.files_scanned) + savedDocumentsList.size + getString(
                                        R.string.documents_found
                                    )
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
                        for (item in deletedDocumets) {
                            val actualFfile = item.file
                            val fileMetadata = com.google.api.services.drive.model.File()
                            fileMetadata.setName(actualFfile.name)
                            fileMetadata.setParents(Collections.singletonList(id))
                            var type = "application/pdf"
                            if (actualFfile.name.toString()
                                    .contains(".doc") || actualFfile.name.toString()
                                    .contains(".docx")
                            ) {
                                // Word document
                                type = "application/msword"
                            } else if (actualFfile.name.toString()
                                    .contains(".ppt") || actualFfile.name.toString()
                                    .contains(".pptx")
                            ) {
                                // Powerpoint file
                                type = "application/vnd.ms-powerpoint"
                            } else if (actualFfile.name.toString()
                                    .contains(".xls") || actualFfile.name.toString()
                                    .contains(".xlsx")
                            ) {
                                // Excel file
                                type = "application/vnd.ms-excel"
                            }
                            val mediaContent = FileContent(
                                type,
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
                                this@CleanDocumentsActivity, getString(R.string.files_uploaded),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        deletedDocumets.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showHideProgress(false)
                        Toast.makeText(
                            this@CleanDocumentsActivity, e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    deletedDocumets.clear()

                    Log.d("TAG", "uploadFileToGDrive: ${e.message}")
                }
            }
        } ?: Toast.makeText(
            this@CleanDocumentsActivity, getString(R.string.drive_login_error),
            Toast.LENGTH_SHORT
        ).show()
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

    override fun onBackPressed() {
        super.onBackPressed()
    }

}