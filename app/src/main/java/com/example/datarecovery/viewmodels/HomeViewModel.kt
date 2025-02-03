package com.example.datarecovery.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.FileUtils.getFileMD5ToString
import com.example.datarecovery.models.ContactModel
import com.example.datarecovery.models.Duplicate
import com.example.datarecovery.models.DuplicateFile
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.CheckFileOrDirectoryUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File

class HomeViewModel(private val context: Context): ViewModel() {

    val path = Environment.getExternalStorageDirectory().absolutePath
    var liveDataDocumentsList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataAudiosList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataImagesList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataVideosList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataIsDataLoaded = MutableLiveData<Boolean>()
    var documentsList = ArrayList<FilesModel>()
    var imagesList = ArrayList<FilesModel>()
    var audiosList = ArrayList<FilesModel>()
    var videosList = ArrayList<FilesModel>()
    val savedPath =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Recovery").toString()

    var liveDataSavedDocumentsList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataSavedAudiosList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataSavedImagesList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataSavedVideosList = MutableLiveData<ArrayList<FilesModel>>()
    var liveDataSavedIsDataLoaded = MutableLiveData<Boolean>()
    var documentsSavedList = ArrayList<FilesModel>()
    var imagesSavedList = ArrayList<FilesModel>()
    var audiosSavedList = ArrayList<FilesModel>()
    var videosSavedList = ArrayList<FilesModel>()
    var scannedFiles = 0

    var mAllImages = ArrayList<File>()
    val mAllAudios = mutableListOf<File>()
    val mAllVedios = mutableListOf<File>()
    var selectedContacts = ArrayList<ContactModel>()

    suspend fun getRecoverData() {
        if(CheckFileOrDirectoryUtils.getFileList(path) != null){
            val job = CoroutineScope(Dispatchers.IO).async {
                checkFileOfDirectory(
                    CheckFileOrDirectoryUtils.getFileList(path)
                )
            }
            job.await()
            liveDataIsDataLoaded.postValue(true)
        }

    }
    suspend fun getSavedData() {
        if(CheckFileOrDirectoryUtils.getFileList(savedPath) != null){
            val job = CoroutineScope(Dispatchers.IO).async {
                checkSavedFileOfDirectory(
                    CheckFileOrDirectoryUtils.getFileList(savedPath)
                )
            }
            job.await()
            liveDataSavedIsDataLoaded.postValue(true)
        }

    }

    fun checkFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) for (i in fileArr.indices) {
            if (fileArr[i].isDirectory) {
                checkFileOfDirectory(CheckFileOrDirectoryUtils.getFileList(fileArr[i].path))
            } else {
                scannedFiles += 1
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {
                    if (!fileArr[i].path.contains("Recovery")) {
                        val file = File(fileArr[i].path)
                        imagesList.add(FilesModel(file, false))
                        liveDataImagesList.postValue(imagesList)
                    }
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
                            liveDataDocumentsList.postValue(documentsList)
                        }else if (fileArr[i].path.endsWith(".opus") ||
                            fileArr[i].path.endsWith(".mp3") ||
                            fileArr[i].path.endsWith(".aac") ||
                            fileArr[i].path.endsWith(".m4a")
                        ){
                            val file = File(fileArr[i].path)
                            audiosList.add(FilesModel(file,false))
                            liveDataAudiosList.postValue(audiosList)
                        }else if (fileArr[i].path.endsWith(".mkv") ||
                            fileArr[i].path.endsWith(".mp4")){
                            val file = File(fileArr[i].path)
                            videosList.add(FilesModel(file,false))
                            liveDataVideosList.postValue(videosList)
                        }
                    }

                }
            }
        }

    }
    fun checkSavedFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) for (i in fileArr.indices) {
            if (fileArr[i].isDirectory) {
                checkFileOfDirectory(CheckFileOrDirectoryUtils.getFileList(fileArr[i].path))
            } else {
                scannedFiles += 1
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {
                    if (fileArr[i].path.contains("Recovery")) {
                        val file = File(fileArr[i].path)
                        imagesSavedList.add(FilesModel(file, false))
                        liveDataSavedImagesList.postValue(imagesSavedList)
                    }
                } else {
                    Log.d("checkFileOfDirectory", "checkFileOfDirectory: ${fileArr[i].path}")
                    if(fileArr[i].path.contains("Recovery")) {
                        if (fileArr[i].path.endsWith(".pdf") ||
                            fileArr[i].path.endsWith(".docx") ||
                            fileArr[i].path.endsWith(".doc")||
                            fileArr[i].path.endsWith(".txt")
                        ) {
                            val file = File(fileArr[i].path)
                            documentsSavedList.add(FilesModel(file, false))
                            liveDataSavedDocumentsList.postValue(documentsSavedList)
                        }else if (fileArr[i].path.endsWith(".opus") ||
                            fileArr[i].path.endsWith(".mp3") ||
                            fileArr[i].path.endsWith(".aac") ||
                            fileArr[i].path.endsWith(".m4a")
                        ){
                            val file = File(fileArr[i].path)
                            audiosSavedList.add(FilesModel(file,false))
                            liveDataSavedAudiosList.postValue(audiosSavedList)
                        }else if (fileArr[i].path.endsWith(".mkv") ||
                            fileArr[i].path.endsWith(".mp4")){
                            val file = File(fileArr[i].path)
                            videosSavedList.add(FilesModel(file,false))
                            liveDataSavedVideosList.postValue(videosSavedList)
                        }
                    }

                }
            }
        }

    }

    suspend fun getDuplicateImages(context: Context): ArrayList<Duplicate> {
        var duplicateFiles = ArrayList<Duplicate>()
        CoroutineScope(Dispatchers.IO).async {
            val allimages = fetchAllImages()
            Log.d("duplicate", "fetchAllImages: ${allimages.size}")
            val allSameSizeImages = getSameSizeFiles(allimages)
            Log.d("duplicate", "allSameSizeImages: ${allimages.size}")
            duplicateFiles = getDuplicateFiles(allSameSizeImages)
            Log.e("duplicate", "duplicateFiles : "+duplicateFiles.size.toString())

        }.await()
        return duplicateFiles
    }

    suspend fun fetchAllImages():List<File> {
        mAllImages.clear()
        val path = Environment.getExternalStorageDirectory().absolutePath
        if (CheckFileOrDirectoryUtils.getFileList(path) != null) {
            val job = CoroutineScope(Dispatchers.IO).async {
                checkDuplicateFileOfDirectory(
                    CheckFileOrDirectoryUtils.getFileList(path)
                )
            }
            job.await()
        }
        return mAllImages
    }

    fun checkDuplicateFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) for (i in fileArr.indices) {
            if (fileArr[i].isDirectory) {
                checkDuplicateFileOfDirectory(CheckFileOrDirectoryUtils.getFileList(fileArr[i].path))
            } else {
                scannedFiles += 1
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(fileArr[i].path, options)
                if (!(options.outWidth == -1 || options.outHeight == -1)) {
                    if (!fileArr[i].path.contains("Recovery")) {
                        val file = File(fileArr[i].path)
                        mAllImages.add(file)
                        Log.d("duplicate", "${mAllImages.size}: ")
                    }
                } else {
                    Log.d("checkFileOfDirectory", "checkFileOfDirectory: ${fileArr[i].path}")
                    if(!fileArr[i].path.contains("Recovery")) {
                        if (fileArr[i].path.endsWith(".pdf") ||
                            fileArr[i].path.endsWith(".docx") ||
                            fileArr[i].path.endsWith(".doc")||
                            fileArr[i].path.endsWith(".txt")
                        ) {
                            val file = File(fileArr[i].path)

                        }else if (fileArr[i].path.endsWith(".opus") ||
                            fileArr[i].path.endsWith(".mp3") ||
                            fileArr[i].path.endsWith(".aac") ||
                            fileArr[i].path.endsWith(".m4a")
                        ){
                            val file = File(fileArr[i].path)

                        }else if (fileArr[i].path.endsWith(".mkv") ||
                            fileArr[i].path.endsWith(".mp4")){
                            val file = File(fileArr[i].path)
                            mAllVedios.add(file)

                        }
                    }

                }
            }
        }

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
    suspend fun getAllDuplicateVideos(context: Context): ArrayList<Duplicate> {
        var duplicateFiles = ArrayList<Duplicate>()
        CoroutineScope(Dispatchers.IO).async {
            val allVideos = fetchAllVedios()
            val allSameSizeVideoFiles = getSameSizeFiles(allVideos)
            duplicateFiles = getDuplicateFiles(allSameSizeVideoFiles)

        }.await()
        return duplicateFiles
    }
    suspend fun fetchAllVedios():List<File>{
        mAllVedios.clear()
        val path = Environment.getExternalStorageDirectory().absolutePath
        if (CheckFileOrDirectoryUtils.getFileList(path) != null) {
            val job = CoroutineScope(Dispatchers.IO).async {
                checkDuplicateFileOfDirectory(
                    CheckFileOrDirectoryUtils.getFileList(path)
                )
            }
            job.await()
        }
        return mAllVedios
    }
    suspend fun getDuplicateAudios(context: Context): ArrayList<Duplicate> {

        var duplicateFiles = ArrayList<Duplicate>()
        CoroutineScope(Dispatchers.IO).async {
            val allAudios = fetchAllAudios()
            val allSameSizeAudioFiles = getSameSizeFiles(allAudios)
            duplicateFiles = getDuplicateFiles(allSameSizeAudioFiles)

        }.await()
        return duplicateFiles
    }
    suspend fun fetchAllAudios():List<File>{
        mAllAudios.clear()
        val path = Environment.getExternalStorageDirectory().absolutePath
        if (CheckFileOrDirectoryUtils.getFileList(path) != null) {
            val job = CoroutineScope(Dispatchers.IO).async {
                checkDuplicateFileOfDirectory(
                    CheckFileOrDirectoryUtils.getFileList(path)
                )
            }
            job.await()
        }
        return mAllAudios
    }
    suspend fun getDuplicateContacts(context: Context): ArrayList<ArrayList<ContactModel>?> {
        var duplicateContacts = ArrayList<ArrayList<ContactModel>?>()
        CoroutineScope(Dispatchers.IO).async {
            duplicateContacts = getAllDuplicateContacts(context)

        }.await()
        return duplicateContacts
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

}