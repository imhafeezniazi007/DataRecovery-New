package com.example.datarecovery.base

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.blankj.utilcode.util.FileUtils.getFileMD5ToString
import com.bumptech.glide.Glide
import com.example.datarecovery.BuildConfig
import com.example.datarecovery.R
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.ContactModel
import com.example.datarecovery.models.Duplicate
import com.example.datarecovery.models.DuplicateFile
import com.example.datarecovery.utils.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


open class BaseActivity : LocalizationActivity(), OnLocaleChangedListener {
    private val localizationDelegate = LocalizationActivityDelegate(this)
    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE

        )
    val APP_STORAGE_ACCESS_REQUEST_CODE = 501
    val PERMISSION_REQUEST_CODE = 101
    val destinationDirectory = StringBuilder()
    private var loadingDialog: AlertDialog? = null
    var selectedContacts = ArrayList<ContactModel>()
    var PERMISSIONS_REQUEST = 1002
    val mAllImages = mutableListOf<File>()
    val mAllAudios = mutableListOf<File>()
    val mAllVedios = mutableListOf<File>()
    private var rewardedInterstitialAd:RewardedInterstitialAd? = null
    lateinit var firebaseAnalytics: FirebaseAnalytics
    protected var bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        localizationDelegate.addOnLocaleChangedListener(this)
        localizationDelegate.onCreate()
        setTheme(R.style.LightTheme);
        super.onCreate(savedInstanceState)
        ThemesUtil.onActivityCreateSetTheme(this);
//       setLanguage(Resources.getSystem().configuration.locale.language)
        permission()

       /* val settings = AppLovinSdkSettings(this)
        settings.testDeviceAdvertisingIds = listOf("d577ea34-66cf-47bc-97e2-283166933478")
        val sdk = AppLovinSdk.getInstance(settings, this)*/

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

    }

    fun permission() {
        if (VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val uri =
                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                    )
                    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
//                        startActivity(intent)
                } catch (ex: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
//                        startActivity(intent)
                }
            } else {
//                AdmobAdsModel(this).interstitialAdShow(this) {
//                    startActivity(
//                        Intent(
//                            this@MainActivity,
//                            ScanningActivity::class.java
//                        )
//                    )
//                }
            }
        }
       checkAndRequestPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                    var isAllGranted = true
                    var i = 0
                    while (i < grantResults.size) {
                        val status = grantResults[i]
                        if (status != PackageManager.PERMISSION_GRANTED) {
                            isAllGranted = false
                            break
                        }
                        i++
                    }
                    if (isAllGranted) {
                        Toast.makeText(applicationContext, "Granted", Toast.LENGTH_LONG).show()
                        try {
                            createFolder()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    } else {
//                        Toast.makeText(this, getString(R.string.allow_permission_storage), Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    createFolder()
                    // perform action when allow permission success
                    Log.d("TAG", "onActivityResult: done")
                } else {
//                    Toast.makeText(this, getString(R.string.allow_permission_storage), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    fun createFolder() {
        /*destinationDirectory.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
        destinationDirectory.append(separator)
        destinationDirectory.append(getString(R.string.app_name))
        destinationDirectory.append(separator)
        destinationDirectory.append("Recovery")
        val folder = File(
            destinationDirectory.toString()
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }*/

//        val mediaStorageDir = File(Environment.getExternalStorageDirectory().toString() + "/Recovery")
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Recovery")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("error", "failed to create directory")
            }
        }

    }

    fun copy(src: File): Boolean {
//        val dst = File(
//            Environment.getExternalStoragePublicDirectory("Datarecovery").toString()
//        )

//        val mediaStorageDir = File(Environment.getExternalStorageDirectory().toString() + "/Recovery")
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Recovery")
/*
        val sbDirectory = StringBuilder()
        sbDirectory.append(Environment.getExternalStoragePublicDirectory())
        sbDirectory.append(separator)
        sbDirectory.append(getString(R.string.app_name))
        sbDirectory.append(separator)
        sbDirectory.append("Recovery")*/
        val dst = File(
            mediaStorageDir.toString()
        )
        /*val dst = File(
            Environment.getExternalStorageDirectory().toString() +
                    File.separator + "Datarecovery"
        )*/
        try {
            File(src.path)
                .copyTo(File(dst, src.name), true)
            return true

        } catch (ex: NoSuchFileException) {
            Log.d("TAG", "copy: ${ex.message}")
            return false
        } catch (ex: IOException) {
            Log.d("TAG", "copy: ${ex.message}")
            return false
        }

    }

    fun showHideProgressBar(show: Boolean) {
        closeKeyboard()
        if (show) {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                return
            }
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val view: View = inflater.inflate(R.layout.loading_dialog_progress, null)
            builder.setView(view)
            builder.setCancelable(true)
            loadingDialog = builder.create()
            if (loadingDialog!!.getWindow() != null) loadingDialog!!.getWindow()!!
                .setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
            loadingDialog!!.setCancelable(true)
            loadingDialog!!.show()

        } else {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                loadingDialog!!.dismiss()
            }
        }
    }
    fun showHideProgress(show: Boolean) {
        closeKeyboard()
        if (show) {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                return
            }
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val view: View = inflater.inflate(R.layout.loading_dialog_progress, null)
            builder.setView(view)
            builder.setCancelable(true)
            loadingDialog = builder.create()
            if (loadingDialog!!.getWindow() != null) loadingDialog!!.getWindow()!!
                .setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
            loadingDialog!!.setCancelable(true)
            loadingDialog!!.show()

        } else {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                loadingDialog!!.dismiss()
            }
        }
    }
    fun showHideProgress(show: Boolean,dataListener: DataListener) {
        closeKeyboard()
        if (show) {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                return
            }
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val view: View = inflater.inflate(R.layout.loading_dialog, null)
            val loadingIV = view.findViewById<ImageView>(R.id.loadingIV)
            builder.setView(view)
            builder.setCancelable(false)
            loadingDialog = builder.create()
            Glide.with(this).asGif().load(R.raw.animation).into(loadingIV);
            if (loadingDialog!!.getWindow() != null) loadingDialog!!.getWindow()!!
                .setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
            loadingDialog!!.setCancelable(true)
            loadingDialog!!.show()
            loadingDialog!!.setOnCancelListener {
                dataListener.onRecieve(true)
            }
        } else {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                loadingDialog!!.dismiss()
            }
        }
    }

    fun loadingAdProgress(show: Boolean) {
        closeKeyboard()
        if (show) {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                return
            }
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this,R.style.WrapContentDialog)
            val inflater = layoutInflater
            val view: View = inflater.inflate(R.layout.loading_ad_dialog, null)

            builder.setView(view)
            builder.setCancelable(false)
            loadingDialog = builder.create()
            if (loadingDialog!!.getWindow() != null) loadingDialog!!.getWindow()!!
                .setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
            loadingDialog!!.setCancelable(false)
            loadingDialog!!.show()

        } else {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                loadingDialog!!.dismiss()
            }
        }
    }
    fun loadingProgress(show: Boolean) {
        closeKeyboard()
        if (show) {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                return
            }
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this,R.style.WrapContentDialog)
            val inflater = layoutInflater
            val view: View = inflater.inflate(R.layout.loading_config_dialog, null)

            builder.setView(view)
            builder.setCancelable(false)
            loadingDialog = builder.create()
            if (loadingDialog!!.getWindow() != null) loadingDialog!!.getWindow()!!
                .setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
            loadingDialog!!.setCancelable(false)
            loadingDialog!!.show()

        } else {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                loadingDialog!!.dismiss()
            }
        }
    }

    fun closeKeyboard() {
        val inputManager =
            applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager != null && this.currentFocus != null) inputManager.hideSoftInputFromWindow(
            this.currentFocus!!.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN
        )
    }

    suspend fun isFolderPresent(): String {
        var isPresent = ""
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).async {
                val FOLDER_NAME = "DataRecovery"
                val result: FileList = getDriveService()!!.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
                    .execute()
                for (file in result.files) {
                    if (file.name == FOLDER_NAME) {
                        isPresent = file.id
                        break
                    }
                }
            }.await()
        } ?: ""
        return isPresent
    }

    fun getDriveService(): Drive? {
        GoogleSignIn.getLastSignedInAccount(this)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        return null
    }

    /**
     * delete file from the user's My Drive Folder.
     */
    suspend fun deleteFolderFile(fileId: String): Boolean {
        var isDeleted = false
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).async {
                // Retrieve the metadata as a File object.
                googleDriveService.files().delete(fileId).execute();
                isDeleted = true
            }.await()
        } ?: ""
        return isDeleted

    }

    /**
     * Download file from the user's My Drive Folder.
     */
    suspend fun downloadFile(fileSaveLocation: File?, fileId: String?): Boolean {
        var isDownloaded = false
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).async {
                // Retrieve the metadata as a File object.
                val outputStream: OutputStream = FileOutputStream(fileSaveLocation)
                googleDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                isDownloaded = true
            }.await()
        } ?: ""
        return isDownloaded
    }


    suspend fun getDuplicateImages(context: Context): ArrayList<Duplicate> {
        var duplicateFiles = ArrayList<Duplicate>()
        CoroutineScope(Dispatchers.IO).async {
//            val allImages = getAllImages(context)
            val allimages = fetchAllImages()
            val allSameSizeImages = getSameSizeFiles(allimages)
            duplicateFiles = getDuplicateFiles(allSameSizeImages)
            Log.e("duplicateSize", duplicateFiles.size.toString())

        }.await()
        return duplicateFiles
    }


    suspend fun getAllImages(context: Context): List<File?> {
        val photosList = mutableListOf<File>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"

        val query = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val nameColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
            val data = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                try {
                    photosList += File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)))
                } catch (ex: java.lang.Exception) {

                }

            }
        }

        return photosList
    }

    suspend fun getSameSizeFiles(files: List<File?>): List<File?> {
        val hashMap: HashMap<Long, ArrayList<File>> = HashMap()
        val sameSizeFiles: ArrayList<File> = ArrayList()
        for (file in files) {
            file?.let {
                if (hashMap.containsKey(file.length())) {
                    val oldFiles: ArrayList<File>? = hashMap[file.length()]
                    oldFiles?.add(file)
                    oldFiles?.let {
                        hashMap[file.length()] = it
                    }
                } else {
                    hashMap[file.length()] = arrayListOf(file)
                }
            }
        }
        hashMap.forEach {
            if (it.value.size > 1) {
                it.value.forEach { file ->
                    sameSizeFiles.add(file)
                }
            }
        }
        return sameSizeFiles
    }

    fun getDuplicateFiles(files: List<File?>): ArrayList<Duplicate> {
        val hashmap: HashMap<String, Duplicate?> = HashMap()
        for (file in files) {
            if ((file?.length() ?: 0) > 0) {

                val md5Hash: String = getFileMD5ToString(file)

                if (hashmap.containsKey(md5Hash)) {
                    val old: Duplicate? = hashmap[md5Hash]
                    val oldList = old?.getDuplicateFiles()
                    file?.let { DuplicateFile(it, false) }?.let { oldList?.add(it) }
                    hashmap[md5Hash] = oldList?.let { Duplicate(it) }
                } else {
                    val fileList: ArrayList<DuplicateFile> = ArrayList()
                    file?.let { DuplicateFile(it, false).let { fileList.add(it) } }
                    hashmap[md5Hash] = Duplicate(fileList)
                }
            }
        }

        val duplicateFileslist: ArrayList<Duplicate> = ArrayList()

        for (single in hashmap.values) {
            if ((single?.getDuplicateFiles()?.size ?: 0) > 1) {
                single?.let { duplicateFileslist.add(it) }
                single?.getDuplicateFiles()?.joinToString()?.let { Log.e("testDuplicate", it) }
            }
        }
        return duplicateFileslist
    }

    suspend fun getDuplicateAudios(context: Context): ArrayList<Duplicate> {

        var duplicateFiles = ArrayList<Duplicate>()
        CoroutineScope(Dispatchers.IO).async {
//            val allAudios = getAllAudios(context)
            val allAudios = fetchAllAudios()
            val allSameSizeAudioFiles = getSameSizeFiles(allAudios)
            duplicateFiles = getDuplicateFiles(allSameSizeAudioFiles)

        }.await()
        return duplicateFiles
    }

    suspend fun getAllAudios(context: Context): List<File> {
        val audioFileList = mutableListOf<File>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        )

        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            while (cursor.moveToNext()) {
                try {
                    audioFileList += File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)))
                } catch (e: java.lang.Exception) {
                }
            }
        }
        return audioFileList
    }

    suspend fun getAllDuplicateVideos(context: Context): ArrayList<Duplicate> {
        var duplicateFiles = ArrayList<Duplicate>()
        CoroutineScope(Dispatchers.IO).async {
//            val allVideos = getALlVideos(context)
            val allVideos = fetchAllVedios()
            val allSameSizeVideoFiles = getSameSizeFiles(allVideos)
            duplicateFiles = getDuplicateFiles(allSameSizeVideoFiles)

        }.await()
        return duplicateFiles
    }

    suspend fun getALlVideos(context: Context): List<File> {
        val videoFileList = mutableListOf<File>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE
        )

        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

        val query = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
            val nameColumn =
                cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
            val data = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            var pos = 0
            while (cursor.moveToNext()) {
                try {
                    pos++
                    val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    videoFileList += File(cursor.getString(pathIndex))
                } catch (e: java.lang.Exception) {
                }
            }
        }

        return videoFileList
    }

    private suspend fun getAllDuplicateContacts(context: Context): ArrayList<ArrayList<ContactModel>?> {
        val duplicateContactMap: HashMap<String, ArrayList<ContactModel>?> = HashMap()
        val duplicateContactList: ArrayList<ArrayList<ContactModel>?> = ArrayList()
        val allContactList = getAllContacts(context)

        allContactList.forEach { contact ->
            if (duplicateContactMap.containsKey(contact.mobileNumber) && duplicateContactMap[contact.mobileNumber]?.get(
                    0
                )?.id != contact.id
            ) {
                val old: ArrayList<ContactModel>? = duplicateContactMap[contact.mobileNumber]
                old?.add(contact)
                duplicateContactMap[contact.mobileNumber.toString()] = old

            } else {
                duplicateContactMap[contact.mobileNumber.toString()] = arrayListOf(contact)
            }
        }
        selectedContacts.clear()
        for (single in duplicateContactMap.values) {
            if ((single?.size ?: 0) > 1) {
                duplicateContactList.add(single)
                single?.withIndex()?.forEach {
                    if (it.index != 0) {
                        selectedContacts.add(it.value)
                    }
                }
            }
        }
        return duplicateContactList
    }

    suspend fun getAllContacts(context: Context): ArrayList<ContactModel> {
        val allContacts: ArrayList<ContactModel> = ArrayList()
        val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cr: ContentResolver = context.contentResolver

        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        if (cursor != null) {
            val mobileNoSet = HashSet<String>()
            cursor.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val idIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                var name: String
                var number: String
                var id: String
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex)
                    number = cursor.getString(numberIndex)
                    id = cursor.getString(idIndex)
                    number = number.replace(" ", "")
                    allContacts.add(ContactModel(id, name, number))
                    mobileNoSet.add(number)
                }
            }
        }
        Log.e("allContact", allContacts.joinToString())

        return allContacts
    }

    suspend fun getDuplicateContacts(context: Context): ArrayList<ArrayList<ContactModel>?> {
        var duplicateContacts = ArrayList<ArrayList<ContactModel>?>()
        CoroutineScope(Dispatchers.IO).async {
            duplicateContacts = getAllDuplicateContacts(context)

        }.await()
        return duplicateContacts
    }

    fun findIndex(arr: ArrayList<ContactModel>, item: ContactModel): Int {
        return arr.indexOf(item)
    }

    fun deleteContactById(id: String) {
        val cr = contentResolver
        val cur = cr?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        cur?.let {
            try {
                if (it.moveToFirst()) {
                    do {
                        if (cur.getString(
                                cur.getColumnIndex(ContactsContract.PhoneLookup._ID) ?: 0
                            ) == id
                        ) {
                            val lookupKey =
                                cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY) ?: 0
                                )
                            val uri =
                                Uri.withAppendedPath(
                                    ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                                    lookupKey
                                )
                            cr.delete(uri, null, null)
                            break
                        }
                    } while (it.moveToNext())
                }

            } catch (e: Exception) {
                println(e.stackTrace)
            } finally {
                it.close()
            }
        }
    }

    fun filesDeleted(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.files_deleted_layout, null)
        val ok = view.findViewById<ConstraintLayout>(R.id.okBtn)

        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        ok.setOnClickListener {
            dataListener.onRecieve(true)
            imageDialog.dismiss()

        }
        imageDialog!!.getWindow()!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        imageDialog.setCancelable(false)
        imageDialog.show()
    }

    fun completeScanningDialog(activity: Activity,recoverable: Int, total: Int) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.complete_dialog, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(true)
        val ok = view.findViewById<ConstraintLayout>(R.id.recoverlayout)
        val recoverableTv = view.findViewById<TextView>(R.id.recoverableTv)
        val totalTv = view.findViewById<TextView>(R.id.totalTv)
        totalTv.text = total.toString() + getString(R.string.files)

        recoverableTv.text = recoverable.toString() + " "+getString(R.string.recoverable_files_found)

        ok.setOnClickListener {
            imageDialog.dismiss()
        }
        imageDialog.window!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        if (!activity.isFinishing) {
            //show dialog
            imageDialog.show()
        }
    }

    fun cleanCompleteScanningDialog(recoverable: Int, total: Int,dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.complete_dialog, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(true)
        val ok = view.findViewById<ConstraintLayout>(R.id.recoverlayout)
        val recoverableTv = view.findViewById<TextView>(R.id.recoverableTv)
        val totalTv = view.findViewById<TextView>(R.id.totalTv)
        totalTv.text = total.toString() + getString(R.string.files)

        recoverableTv.text = recoverable.toString()+" " + getString(R.string.files_found)

        ok.setOnClickListener {
            dataListener.onRecieve(true)
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
    fun watchAdDialog(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.watchad_dialog, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(false)
        val cancelIV = view.findViewById<ImageView>(R.id.cancelIV)
        val adIV = view.findViewById<ImageView>(R.id.adIV)
        val premiumLayout = view.findViewById<ConstraintLayout>(R.id.premiumLayout)
        val orTV = view.findViewById<TextView>(R.id.orTV)
        if(AppPreferences.getInstance(this).isAppPurchased){
            premiumLayout.visibility = View.GONE
            orTV.visibility = View.GONE
        }
        cancelIV.setOnClickListener {
            dataListener.onClick(true)
            imageDialog.dismiss()
        }
        adIV.setOnClickListener {
            dataListener.onRecieve(true)
            imageDialog.dismiss()
        }
        premiumLayout.setOnClickListener {
            dataListener.onPremium(true)
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

    fun showSnackbar() {
        val snack = Snackbar.make(
            findViewById(android.R.id.content), getString(R.string.snackbar_text),
            Snackbar.LENGTH_INDEFINITE
        )

        val params = snack.view as SnackbarLayout
        params.minimumHeight = 150
        val view = snack.view
        val params1 = view.layoutParams as FrameLayout.LayoutParams
        params1.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        snack.setAction(getString(R.string.ok)) { v: View? ->
            snack.dismiss()
        }
        snack.show()
    }

    fun sortingDialog(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.sort_layout, null)
        builder.setView(view)
        builder.setCancelable(true)
        imageDialog = builder.create()
        imageDialog.setCancelable(true)
        val rdGroup = view.findViewById<RadioGroup>(R.id.rdGroup)
        rdGroup.setOnCheckedChangeListener { group, checkedId ->
            // This will get the radiobutton that has changed in its check state
            val checkedRadioButton = group.findViewById<View>(checkedId) as RadioButton
            // This puts the value (true/false) into the variable
            val isChecked = checkedRadioButton.isChecked
            // If the radiobutton that has changed in check state is now checked...
            Log.d("TAG", "sortingDialog: ${checkedRadioButton.id}")
            if (isChecked) {

            }
        }
        imageDialog.window!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
        val window: Window = imageDialog.window!!
        val wlp: WindowManager.LayoutParams = window.attributes
        wlp.gravity = Gravity.TOP or Gravity.RIGHT

        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
        imageDialog.show()

    }

    fun sortingPopup(isChecked:Int,dataListener: DataListener, view: View) {
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = layoutInflater.inflate(R.layout.sort_layout, null)
        val rdGroup = popupView.findViewById<RadioGroup>(R.id.rdGroup)
        val ascSizeRd = popupView.findViewById<RadioButton>(R.id.ascSizeRd)
        val descSizeRd = popupView.findViewById<RadioButton>(R.id.descSizeRd)
        val ascDateRd = popupView.findViewById<RadioButton>(R.id.ascDateRd)
        val descDateRd = popupView.findViewById<RadioButton>(R.id.descDateRd)

        if(isChecked == 1){
            ascSizeRd.isChecked = true
        }else if(isChecked == 2){
            descSizeRd.isChecked = true
        }else if(isChecked == 3){
            ascDateRd.isChecked = true
        }else if(isChecked == 4){
            descDateRd.isChecked = true
        }

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.setBackgroundDrawable(BitmapDrawable())
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
        })
        if(isChecked == 1){}
        else if(isChecked == 2){}
        else if(isChecked == 3){}
        else if(isChecked == 4){}
        rdGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("TAG", "sortingPopup: ${checkedId}")

            if(R.id.ascSizeRd == checkedId){
                dataListener.onRecieve(1)
                popupWindow.dismiss()
            }else if(R.id.descSizeRd == checkedId){
                dataListener.onRecieve(2)
                popupWindow.dismiss()
            }else if(R.id.ascDateRd == checkedId){
                dataListener.onRecieve(3)
                popupWindow.dismiss()
            }else if(R.id.descDateRd == checkedId){
                dataListener.onRecieve(4)
                popupWindow.dismiss()
            }
        }
        popupWindow.showAsDropDown(view)
    }

    override fun attachBaseContext(newBase: Context) {
        applyOverrideConfiguration(localizationDelegate.updateConfigurationLocale(newBase))
        super.attachBaseContext(newBase)
    }

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }

    override fun getResources(): Resources {
        return localizationDelegate.getResources(super.getResources())
    }


    fun checkAndRequestPermissions() {
        val permission1: Int = ContextCompat.checkSelfPermission(
            this,
            READ_CONTACTS
        )
        val permission2: Int = ContextCompat.checkSelfPermission(
            this,
            WRITE_CONTACTS
        )
        val permission3: Int = ContextCompat.checkSelfPermission(
            this,
            WRITE_EXTERNAL_STORAGE
        )
        val permission4: Int = ContextCompat.checkSelfPermission(
            this,
            READ_EXTERNAL_STORAGE
        )
        val listPermissionsNeeded=ArrayList<String>()
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_CONTACTS)
        }
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_CONTACTS)
        }
        if (permission3 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }
        if (permission4 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_EXTERNAL_STORAGE)
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(), PERMISSIONS_REQUEST
            )
        } else {
            createFolder()
        }

    }

    suspend fun fetchAllImages():List<File> {
        mAllImages.clear()
        val path = Environment.getExternalStorageDirectory().absolutePath
                if (Utils.getFileList(path) != null) {
                    val job = CoroutineScope(Dispatchers.IO).async {
                        checkFileOfDirectoryImages(
                            Utils.getFileList(path)
                        )
                    }
                    job.await()
                }
            return mAllImages
    }
    suspend fun fetchAllAudios():List<File>{
        mAllAudios.clear()
        val path = Environment.getExternalStorageDirectory().absolutePath
        if (Utils.getFileList(path) != null) {
            val job = CoroutineScope(Dispatchers.IO).async {
                checkFileOfDirectoryImages(
                    Utils.getFileList(path)
                )
            }
            job.await()
        }
        return mAllAudios
    }
    suspend fun fetchAllVedios():List<File>{
        mAllVedios.clear()
        val path = Environment.getExternalStorageDirectory().absolutePath
        if (Utils.getFileList(path) != null) {
            val job = CoroutineScope(Dispatchers.IO).async {
                checkFileOfDirectoryImages(
                    Utils.getFileList(path)
                )
            }
            job.await()
        }
        return mAllVedios
    }
    private fun checkFileOfDirectoryImages(fileArr: Array<File>) {
        if (fileArr != null) for (i in fileArr.indices) {
            if (fileArr[i].isDirectory) {
                checkFileOfDirectoryImages(Utils.getFileList(fileArr[i].path))
            } else {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {
                        val file = File(fileArr[i].path)
                        mAllImages.add(file)
                }else {
                    if(!fileArr[i].path.contains("Recovery")) {
                        if (fileArr[i].path.endsWith(".opus") ||
                            fileArr[i].path.endsWith(".mp3") ||
                            fileArr[i].path.endsWith(".aac") ||
                            fileArr[i].path.endsWith(".m4a")
                        )
                        {
                            val file = File(fileArr[i].path)
                            mAllAudios.add(file)

                        }
                            if (fileArr[i].path.endsWith(".mkv") ||
                                fileArr[i].path.endsWith(".mp4")
                            ) {
                                val file = File(fileArr[i].path)
                                mAllVedios.add(file)

                            }


                    }
                }
            }
        }
    }


    private fun completeDialogPopulateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        val headlineView = adView.findViewById<TextView>(R.id.nativeTV)
        headlineView.text = nativeAd.headline
        adView.headlineView = headlineView
        val mediaView = adView.findViewById<MediaView>(R.id.mediaView)
        adView.mediaView = mediaView
        adView.setNativeAd(nativeAd)

    }

    fun getFilePathToMediaID(songPath: String): Long {
        var id: Long = 0
        val cr: ContentResolver? = contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor: Cursor? = cr?.query(uri, projection, "$selection=?", selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        cursor?.close()
        return id
    }

    fun rateusApp(){
        val manager = ReviewManagerFactory.create(this@BaseActivity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = manager.launchReviewFlow(this@BaseActivity, reviewInfo)
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
        }
    }
    open fun logEvent(eventName: String, bundle: Bundle) {
        if (bundle.containsKey("HomeScreen")) {
            var description = bundle.getString("HomeScreen")
            if (description != null) {
                if (description.isNotEmpty() && description.length >= 100) {
                    description = description.substring(0, 95)
                    description = "$description..."
                    bundle.putString("HomeScreen", description)
                }else if (description.isNotEmpty()) {
                    bundle.putString("HomeScreen", description)
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

}