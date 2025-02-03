package com.example.datarecovery.views.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentAudiosBinding
import com.example.datarecovery.databinding.FragmentBackupBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.DuplicateFile
import com.example.datarecovery.utils.MediaScanner
import com.example.datarecovery.views.activities.AllDuplicateActivity
import com.example.datarecovery.views.adapters.DuplicateParentAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class AudiosFragment : Fragment() {
    lateinit var binding: FragmentAudiosBinding
    var adapter:DuplicateParentAdapter?=null
    val uriList: ArrayList<Uri> = ArrayList()

    private val deleteRequest = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { it ->
        Log.e("deleteResult", it.toString())
        if (it.resultCode == Activity.RESULT_OK) {

            MediaScanner(requireContext())
            (requireActivity() as AllDuplicateActivity).filesDeleted(object :
                DataListener {
                override fun onRecieve(any: Any) {
                    if (any as Boolean) {
                        (requireActivity() as AllDuplicateActivity).finish()
                    }
                }

            })
        } else {
            uriList.clear()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAudiosBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DuplicateParentAdapter(requireContext(), (requireActivity() as AllDuplicateActivity).duplicateAudios,object :DataListener{
            override fun onRecieve(any: Any) {
                val item = any as DuplicateFile
                Log.d("TAG", "onRecieve: ${item.isSelect}")
                if((requireActivity() as AllDuplicateActivity).deletedAudios.contains(item)){
                    (requireActivity() as AllDuplicateActivity).deletedAudios.remove(item)
                }else{
                    (requireActivity() as AllDuplicateActivity).deletedAudios.add(item)
                }
            }

        })
        binding.duplicateRV.adapter = adapter
        if((requireActivity() as AllDuplicateActivity).duplicateAudios.isEmpty()){
            binding.noTV.visibility = View.VISIBLE
        }
        clickListener()
    }

    private fun clickListener() {
        binding.deletelayout.setOnClickListener {
                if((requireActivity() as AllDuplicateActivity).deletedAudios.isEmpty()){
                    Toast.makeText(requireContext(),getString(R.string.no_item_selected), Toast.LENGTH_LONG).show()
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val selectedDuplicateFiles = mutableListOf<File?>()
                        for (i in (requireActivity() as AllDuplicateActivity).deletedVideos) {
                            selectedDuplicateFiles.add(i.getFile())
                        }
                        getUri(
                            selectedDuplicateFiles,
                            MediaStore.Audio.Media.getContentUri("external")
                        )
                        requestDeletePermission(uriList)
                    } else {
                        var isDeleted = true
                        for (i in (requireActivity() as AllDuplicateActivity).deletedAudios) {
                            if (i.getFile().exists()) {
                                if (!i.getFile().delete()) {
                                    isDeleted = false
                                    break
                                }
                            }
                        }
                        if (isDeleted) {
                            (requireActivity() as AllDuplicateActivity).deletedAudios.clear()
                            MediaScanner(requireContext())
                            (requireActivity() as AllDuplicateActivity).filesDeleted(object :
                                DataListener {
                                override fun onRecieve(any: Any) {
                                    if (any as Boolean) {
                                        (requireActivity() as AllDuplicateActivity).finish()
                                    }
                                }

                            })
                        }

                    }
                }
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
            val pi = requireActivity().contentResolver?.let { MediaStore.createDeleteRequest(it, uriList) }

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
    fun getFilePathToMediaID(songPath: String): Long {
        var id: Long = 0
        val cr: ContentResolver? = requireActivity().contentResolver
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

}