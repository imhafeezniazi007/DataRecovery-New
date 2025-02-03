package com.example.datarecovery.views.activities

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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityCleanAudiosBinding
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

class CleanAudiosActivity : BaseActivity() {
    lateinit var binding: ActivityCleanAudiosBinding
    var deletedAudios: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var savedAudiosList: ArrayList<FilesModel> = ArrayList<FilesModel>()
    var imagesAdapter: AudiosAdapter? = null

    //    val path = File(Environment.getExternalStorageDirectory().toString() + "/Recovery").toString()
    val path = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + "/Recovery"
    ).toString()

    var scannedFiles = 0
    var seekbar: SeekBar? = null
    private var startTime = 0.0
    private val finalTime = 0.0
    val mediaPlayer = MediaPlayer()
    private var currentIndex = -1
    var sorting = 4
    var dataLoaded = false
    lateinit var mainViewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCleanAudiosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = HomeViewModel(this)
        setmTheme()
        fetchData()
        imagesAdapter = AudiosAdapter(this@CleanAudiosActivity, savedAudiosList, object :
            DataListener {
            override fun onRecieve(any: Any) {
                performLogic(any as FilesModel)
            }

            override fun onClick(any: Any) {
                val file = any as FilesModel
                currentIndex = findFileIndex(savedAudiosList, file)
                playAudioDialog(file.file)
            }
        })
        binding.recylerview.adapter = imagesAdapter
        binding.backIV.setOnClickListener {
            finish()
        }
        clickListener()

    }

    private fun clickListener() {
        binding.recoverlayout.setOnClickListener {
            imagesAdapter?.setLongClickFalse()
            binding.recoverlayout.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            var isDeleted = true
            val iterator = deletedAudios.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val isSucces = copy(item.file)
                if (!isSucces) {
                    isDeleted = false
                    Toast.makeText(
                        this@CleanAudiosActivity,
                        getString(R.string.try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                    deletedAudios.clear()
                    break
                } else {
                    iterator.remove()
                }

            }
            if (isDeleted) {
                deletedAudios.clear()
                MediaScanner(this@CleanAudiosActivity)
                showSnackbar()
//                Toast.makeText(this@CleanAudiosActivity,"All files recover successfully",Toast.LENGTH_SHORT).show()
            }

        }
        binding.deleteIV.setOnClickListener {
            if (deletedAudios.isEmpty()) {
                Toast.makeText(
                    this@CleanAudiosActivity,
                    getString(R.string.no_image_selected),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                imagesAdapter?.setLongClickFalse()
                var isDeleted = true
                val iterator = deletedAudios.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    var isSucces = false
                    if (item.file.exists()) {
                        isSucces = item.file.delete()
                    }
                    if (!isSucces) {
                        isDeleted = false
                        Toast.makeText(
                            this@CleanAudiosActivity, getString(R.string.try_again_later),
                            Toast.LENGTH_SHORT
                        ).show()
                        deletedAudios.clear()
                        break
                    } else {
                        savedAudiosList.remove(item)
                        imagesAdapter?.notifyDataSetChanged()
                        iterator.remove()
                    }
                }
                if (isDeleted) {
                    deletedAudios.clear()
                    imagesAdapter?.notifyDataSetChanged()
                    MediaScanner(this@CleanAudiosActivity)
                    Toast.makeText(
                        this@CleanAudiosActivity,
                        getString(R.string.all_files_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.driveIV.setOnClickListener {
            if (deletedAudios.isEmpty()) {
                Toast.makeText(
                    this@CleanAudiosActivity,
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
                if (savedAudiosList.isEmpty()) {
                    Toast.makeText(
                        this@CleanAudiosActivity,
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
                                savedAudiosList.sortBy {
                                    it.file.length()
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 2) {
                                sorting = 2
                                savedAudiosList.sortByDescending {
                                    it.file.length()
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 3) {
                                sorting = 3
                                savedAudiosList.sortBy {
                                    it.file.lastModified()
                                }
                                withContext(Dispatchers.Main) {
                                    imagesAdapter?.notifyDataSetChanged()
                                }
                            } else if (sort == 4) {
                                sorting = 4
                                savedAudiosList.sortByDescending {
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
                    this@CleanAudiosActivity,
                    getString(R.string.scanning_please_wait),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.backIV.setOnClickListener {
            onBackPressed()
        }
    }

    private fun performLogic(file: FilesModel) {
        val list = deletedAudios.filter { s -> s.file == file.file }
        if (list.isEmpty()) {
            deletedAudios.add(file)
        } else {
            val item = list.first()
            if (!file.isCheck) {
                deletedAudios.remove(item)
            } else {
                deletedAudios.add(item)
            }
        }

    }

    fun fetchData() {

        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.scannedFiles = 0
            mainViewModel.getSavedData()
        }

        mainViewModel.liveDataSavedAudiosList.observe(this) {
            Log.d("TAG", "observer: ${it.size}")
            binding.progressTV.text =
                mainViewModel.scannedFiles.toString() + getString(R.string.files_scanned) + " , " + it.size + " " + getString(
                    R.string.images_found
                )
            savedAudiosList.clear()
            savedAudiosList.addAll(it)
            imagesAdapter?.notifyDataSetChanged()
        }
        mainViewModel.liveDataSavedIsDataLoaded.observe(this) {
            if (it) {
                try {
                    savedAudiosList.sortByDescending {
                        it.file.lastModified()
                    }

                    imagesAdapter?.setIsDataLoaded()
                    dataLoaded = true
                    binding.progressLayout.visibility = View.GONE
                    imagesAdapter?.setIsWatchAd()
                    completeScanningDialog(
                        this@CleanAudiosActivity,
                        savedAudiosList.size,
                        mainViewModel.scannedFiles
                    )
                    if (!savedAudiosList.isEmpty()) {
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


        /*//Day la tat ca thu muc trong may
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
                    savedAudiosList.sortByDescending {
                        it.file.lastModified()
                    }
                    withContext(Dispatchers.Main){
                        imagesAdapter?.setIsDataLoaded()
                        dataLoaded =true
                        binding.progressLayout.visibility = View.GONE
                        imagesAdapter?.setIsWatchAd()
                        completeScanningDialog(this@CleanAudiosActivity,savedAudiosList.size,scannedFiles)
                        if(!savedAudiosList.isEmpty()){
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
                    scannedFiles += 1
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(fileArr[i].path, options)
                    if (!(options.outWidth == -1 || options.outHeight == -1)) {

                    } else {
                        if (fileArr[i].path.endsWith(".opus") ||
                            fileArr[i].path.endsWith(".mp3") ||
                            fileArr[i].path.endsWith(".aac") ||
                            fileArr[i].path.endsWith(".m4a")
                        ) {
                            val file = File(fileArr[i].path)
                            savedAudiosList.add(FilesModel(file, false))
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progressTV.text =
                                    scannedFiles.toString() + " Files scanned, " + savedAudiosList.size + " Audios found"
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
                        for (item in deletedAudios) {
                            val actualFfile = item.file
                            val fileMetadata = com.google.api.services.drive.model.File()
                            fileMetadata.setName(actualFfile.name)
                            fileMetadata.setParents(Collections.singletonList(id))
                            val mediaContent = FileContent(
                                "audio/mp4",
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
                                this@CleanAudiosActivity, "Files has been uploaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        deletedAudios.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        showHideProgress(false)
                        Toast.makeText(
                            this@CleanAudiosActivity, e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    deletedAudios.clear()

                    Log.d("TAG", "uploadFileToGDrive: ${e.message}")
                }
            }
        } ?: Toast.makeText(
            this@CleanAudiosActivity, getString(R.string.drive_login_error),
            Toast.LENGTH_SHORT
        ).show()
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
        seekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
            if (currentIndex < (savedAudiosList.size - 1)) {
                currentIndex += 1;
                val nextFile = savedAudiosList[currentIndex].file
                try {
                    name.text = nextFile.name
                    play.setImageResource(R.drawable.ic_pause)
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
                val previousFile = savedAudiosList[currentIndex].file
                try {
                    name.text = previousFile.name
                    play.setImageResource(R.drawable.ic_pause)
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