package com.example.datarecovery.views.activities

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityDuplicateBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.ContactModel
import com.example.datarecovery.models.Duplicate
import com.example.datarecovery.models.DuplicateFile
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.views.adapters.ContactParentAdapter
import com.example.datarecovery.views.adapters.DuplicateParentAdapter
import kotlinx.coroutines.*
import java.io.File

class DuplicateActivity : BaseActivity() {
    lateinit var binding: ActivityDuplicateBinding
    var adapter : DuplicateParentAdapter ?= null
    var imagesList =ArrayList<Duplicate>()
    var deletedItems =ArrayList<DuplicateFile>()
    val uriList: ArrayList<Uri> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDuplicateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val from = intent.getStringExtra("from")
        if(from.equals("images")) {
            showHideProgress(true,object :DataListener{
                override fun onRecieve(any: Any) {
                    if(any as Boolean){
                        finish()
                    }
                }

            })
            CoroutineScope(Dispatchers.IO).launch {
                 imagesList = getDuplicateImages(this@DuplicateActivity)
                if (imagesList.isNotEmpty()) {
                    imagesList.withIndex()?.forEach { fileParent ->
                        fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                            Log.d("TAG", "testttt: ${imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].getFile()}")
                            Log.d("TAG", "testttt: ${imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect}")
                            if (fileChild.index != 0) {
                                imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect = true
                                deletedItems.add(imagesList[fileParent.index].getDuplicateFiles()[fileChild.index])
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter = DuplicateParentAdapter(this@DuplicateActivity, imagesList,object :DataListener{
                        override fun onRecieve(any: Any) {
                            val item = any as DuplicateFile
                            if(deletedItems.contains(item)){
                                deletedItems.remove(item)
                            }else{
                                deletedItems.add(item)
                            }
                            Log.d("TAG", "onRecieve: ${item.isSelect}")
                        }
                    })
                    binding.duplicateRV.adapter = adapter
                    showHideProgress(false,object :DataListener{
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                finish()
                            }
                        }

                    })
                    if(imagesList.isEmpty()){
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }
        }else if(from.equals("videos")) {
            binding.titleTV.text = "Videos"
            showHideProgress(true,object :DataListener{
                override fun onRecieve(any: Any) {
                    if(any as Boolean){
                        finish()
                    }
                }

            })
            CoroutineScope(Dispatchers.IO).launch {
                 imagesList = getAllDuplicateVideos(this@DuplicateActivity)
                if (imagesList.isNotEmpty()) {
                    imagesList.withIndex().forEach { fileParent ->
                        fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                            if (fileChild.index != 0) {
                                imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect = true
                                deletedItems.add(imagesList[fileParent.index].getDuplicateFiles()[fileChild.index])
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter = DuplicateParentAdapter(this@DuplicateActivity, imagesList,object :DataListener{
                        override fun onRecieve(any: Any) {
                            val item = any as DuplicateFile
                            Log.d("TAG", "onRecieve: ${item.isSelect}")
                            if(deletedItems.contains(item)){
                                deletedItems.remove(item)
                            }else{
                                deletedItems.add(item)
                            }
                        }

                    })
                    binding.duplicateRV.adapter = adapter
                    showHideProgress(false,object :DataListener{
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                finish()
                            }
                        }

                    })
                    if(imagesList.isEmpty()){
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }
        }else if(from.equals("audios")) {
            binding.titleTV.text = "Audios"
            showHideProgress(true,object :DataListener{
                override fun onRecieve(any: Any) {
                    if(any as Boolean){
                        finish()
                    }
                }

            })
            CoroutineScope(Dispatchers.IO).launch {
                 imagesList = getDuplicateAudios(this@DuplicateActivity)
                if (imagesList.isNotEmpty()) {
                    imagesList.withIndex().forEach { fileParent ->
                        fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                            if (fileChild.index != 0) {
                                imagesList[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect = true
                                deletedItems.add(imagesList[fileParent.index].getDuplicateFiles()[fileChild.index])
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter = DuplicateParentAdapter(this@DuplicateActivity, imagesList,object :DataListener{
                        override fun onRecieve(any: Any) {
                            val item = any as DuplicateFile
                            Log.d("TAG", "onRecieve: ${item.isSelect}")
                            if(deletedItems.contains(item)){
                                deletedItems.remove(item)
                            }else{
                                deletedItems.add(item)
                            }
                        }

                    })
                    binding.duplicateRV.adapter = adapter
                    showHideProgress(false,object :DataListener{
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                finish()
                            }
                        }

                    })
                    if(imagesList.isEmpty()){
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }
        }else if(from.equals("contacts")) {
            binding.titleTV.text = "Contacts"
            showHideProgress(true,object :DataListener{
                override fun onRecieve(any: Any) {
                    if(any as Boolean){
                        finish()
                    }
                }

            })
            CoroutineScope(Dispatchers.IO).launch {
                val contactList = getDuplicateContacts(this@DuplicateActivity)
                withContext(Dispatchers.Main) {
                    val madapter = ContactParentAdapter(this@DuplicateActivity, contactList,selectedContacts,object :
                        DataListener {
                        override fun onRecieve(any: Any) {
                            val item = any as ContactModel
                            Log.d("TAG", "onRecieve: ${item.name}")
                            if(selectedContacts.contains(item)){
                                selectedContacts.remove(item)
                            }else{
                                selectedContacts.add(item)
                            }
                            Log.d("TAG", "onRecieve: ${selectedContacts.size}")
                        }
                    })
                    binding.duplicateRV.adapter = madapter
                    showHideProgress(false,object :DataListener{
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                finish()
                            }
                        }

                    })
                    if(contactList.isEmpty()){
                        binding.noTV.visibility = View.VISIBLE
                    }
                }
            }
        }

        clickListener(from.toString())
    }

    private fun clickListener(from:String) {
        binding.deletelayout.setOnClickListener {
            if(from.equals("contacts")){
                if(selectedContacts.isEmpty()){
                    Toast.makeText(this@DuplicateActivity,getString(R.string.no_item_selected),Toast.LENGTH_LONG).show()
                }else{
                    selectedContacts.withIndex().forEach {
                        it.value.id?.let { it1 ->
                            deleteContactById(it1)
                        }
                    }
                    filesDeleted(object :DataListener{
                        override fun onRecieve(any: Any) {
                            if(any as Boolean){
                                finish()
                            }
                        }

                    })
                }
            }else{
                if(deletedItems.isEmpty()){
                    Toast.makeText(this@DuplicateActivity,getString(R.string.no_item_selected),Toast.LENGTH_LONG).show()
                }else{

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val selectedDuplicateFiles = mutableListOf<File?>()
                        for (i in  deletedItems){
                             selectedDuplicateFiles.add(i.getFile())
                        }
                        if(from.equals("images")){
                            getUri(
                                selectedDuplicateFiles,
                                MediaStore.Images.Media.getContentUri("external")
                            )
                        }else if(from.equals("videos")){
                            getUri(
                                selectedDuplicateFiles,
                                MediaStore.Video.Media.getContentUri("external")
                            )
                        }else if(from.equals("audios")){
                            getUri(
                                selectedDuplicateFiles,
                                MediaStore.Audio.Media.getContentUri("external")
                            )
                        }
//                        withContext(Dispatchers.Main){
//                            waitDialog?.dismiss()
//                        }
                        requestDeletePermission(uriList)
                    }else {
                        var isDeleted = true
                        for (i in deletedItems) {
                            if (i.getFile().exists()) {
                                if (!i.getFile().delete()) {
                                    isDeleted = false
                                    break
                                }
                            }
                        }
                        if (isDeleted) {
                            deletedItems.clear()
                            MediaScanner(this@DuplicateActivity)
                            filesDeleted(object : DataListener {
                                override fun onRecieve(any: Any) {
                                    if (any as Boolean) {
                                        finish()
                                    }
                                }

                            })
                        }
                    }

                }
            }
        }
        binding.backIV.setOnClickListener {
            onBackPressed()
        }

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

    private val deleteRequest = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { it ->
            Log.e("deleteResult", it.toString())
            if (it.resultCode == Activity.RESULT_OK) {

                filesDeleted(object :DataListener{
                    override fun onRecieve(any: Any) {
                        if(any as Boolean){
                            finish()
                        }
                    }

                })
            } else {
                uriList.clear()
            }
        }

    private fun getUri(list: MutableList<File?>, uri: Uri) {
        list.forEach {
            it?.let {
                val mediaId = getFilePathToMediaID(it.path)
                val uri = ContentUris.withAppendedId(uri, mediaId)
                uriList.add(uri)
            }
        }
    }
    private fun requestDeletePermission(uriList: ArrayList<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = contentResolver?.let { MediaStore.createDeleteRequest(it, uriList) }

            pi?.let {
                val intent: IntentSenderRequest =
                    IntentSenderRequest.Builder(pi.intentSender).setFillInIntent(null)
                        .setFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        ).build()
                deleteRequest.launch(intent)
            }
        }
    }

}