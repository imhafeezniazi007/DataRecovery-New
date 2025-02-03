package com.example.datarecovery.views.fragments

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentBackupBinding
import com.example.datarecovery.models.FilesModel
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Utils
import com.example.datarecovery.views.activities.CleanAudiosActivity
import com.example.datarecovery.views.activities.CleanDocumentsActivity
import com.example.datarecovery.views.activities.CleanImagesActivity
import com.example.datarecovery.views.activities.CleanVediosActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.log


class BackupFragment : Fragment() {
    lateinit var binding: FragmentBackupBinding
    private val gso = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(
            Scope(DriveScopes.DRIVE_FILE),
            Scope(DriveScopes.DRIVE_APPDATA),
            Scope(DriveScopes.DRIVE)
        )
        .build()
    var mFile: ArrayList<FilesModel> = ArrayList<FilesModel>()

    var images = ArrayList<String>()
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    var mContext:Context?=null
    var login = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBackupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        clickListeners()
        fetchData()

    }

    private fun initView() {
        if(AppPreferences.getInstance(requireContext()).getDriveLoginEmail.isNotEmpty()){
            binding.userTV.visibility = View.VISIBLE
            binding.signinTV.text = getString(R.string.sign_out)
            login = true
            binding.userTV.text =AppPreferences.getInstance(requireContext()).getDriveLoginName+"\n"+ AppPreferences.getInstance(requireContext()).getDriveLoginEmail
        }else{
            login = false
            binding.userTV.visibility = View.GONE
            binding.signinTV.text = getString(R.string.sign_in)
        }
    }


    private fun clickListeners() {
            binding.imagesCV.setOnClickListener {
                startActivity(Intent(requireActivity(), CleanImagesActivity::class.java))
            }
            binding.videosCV.setOnClickListener {
                startActivity(Intent(requireActivity(), CleanVediosActivity::class.java))

            }
            binding.audiosCV.setOnClickListener {
                startActivity(Intent(requireActivity(), CleanAudiosActivity::class.java))
            }
            binding.documentsCV.setOnClickListener {
                startActivity(Intent(requireActivity(), CleanDocumentsActivity::class.java))
            }
        binding.signinTV.setOnClickListener {
            if(!login) {
                val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                val signInIntent = googleSignInClient.signInIntent
                startForResult.launch(signInIntent)
            }else{
                signOut()
            }
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }
    fun signOut(){
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d("TAG", "signOut: done")
            AppPreferences.getInstance(requireActivity()).setDriveLoginEmail("")
            AppPreferences.getInstance(requireActivity()).setDriveLoginName("")
            binding.userTV.text = ""
            binding.userTV.visibility = View.GONE
            binding.signinTV.text = requireContext().getString(R.string.sign_in)
        }
    }

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val intent = result.data
            handleSignData(intent)
            if (result.resultCode == Activity.RESULT_OK) {

                /*if (result.data != null) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(intent)

                    */
                /**
                 * handle [task] result
                 *//*
                } else {
                    Toast.makeText(requireContext(), "Google Login Error!", Toast.LENGTH_LONG)
                        .show()
                }*/
            }
        }

    fun getDriveService(): Drive? {
        GoogleSignIn.getLastSignedInAccount(requireContext())?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), listOf(DriveScopes.DRIVE_FILE)
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

    fun handleSignData(data: Intent?) {
        // The Task returned from this call is always completed, no need to attach
        // a listener.
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnCompleteListener {
                Log.d("TAG", "handleSignData: isSuccessful ${it.isSuccessful}")
                Log.d("TAG", "handleSignData: isSuccessful ${it.result.account?.name}")
                if (it.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isFolderPresent().isEmpty()) {
                            val create = createFolder()
                            Log.d("TAG", "handleSignData: $create")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), create, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Log.d("TAG", "handleSignData: account ${it.result?.account}")
                    Log.d("TAG", "handleSignData: displayName ${it.result?.displayName}")
                    Log.d("TAG", "handleSignData: Email ${it.result?.email}")
                    Log.d("TAG", "handleSignData: Email ${it.result?.photoUrl}")
                    AppPreferences.getInstance(requireActivity()).setDriveLoginEmail(it.result?.email!!)
                    AppPreferences.getInstance(requireActivity()).setDriveLoginName(it.result?.displayName!!)
                    binding.userTV.visibility = View.VISIBLE
                    binding.userTV.text =it.result?.displayName+"\n"+ it.result?.email
                    binding.signinTV.text =getString(R.string.sign_out)
                } else {
                    // authentication failed
                    Log.d("TAG", "handleSignData:exception ${it.exception?.message}")
                    Log.d("TAG", "handleSignData:exception ${it.exception}")
                }
            }

    }

    private fun accessDriveFiles() {
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).launch {
                var pageToken: String? = null
                try {
                    do {
                        val result = googleDriveService.files().list().apply {
                            fields = "nextPageToken, files(id, name)"
                            pageToken = this.pageToken
                            spaces = spaces
                        }.execute()
                        Log.d("TAG", "accessDriveFiles: name=${result.files.size}")

                        for (file in result.files) {
                            Log.d("TAG", "accessDriveFiles: name=${file.name} id=${file.id}}")
                            images.add(file.id)
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeFile(file.name, options)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(), "${result.files.size.toString()+getString(R.string.files_found)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } while (pageToken != null)
                } catch (ex: Exception) {
                    Log.d("TAG", "accessDriveFiles exception: ${ex.message}")
                }
            }
        } ?: Log.d("TAG", "Signin error - not logged in")
    }

    fun uploadFileToGDrive() {
        Log.d("TAG", "uploadFileToGDrive: ${getDriveService()}")
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request: Drive.Files.List = googleDriveService.files().list().setQ(
                        "mimeType='application/vnd.google-apps.folder' and trashed=false"
                    )
                    val files: FileList = request.execute()
                    for (file in files) {
                    }
                    val FILE_NAME_BACKUPP = "aqib"
                    val localFileDirectory = mFile.get(0).file.toURI()
                    val actualFfile = mFile.get(0).file
                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = actualFfile.name
                    val fileContent = FileContent("text/plain", actualFfile)
                    googleDriveService.Files().create(gfile, fileContent).execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("TAG", "uploadFileToGDrive: ${e.message}")
                }
            }
        } ?: Log.d("TAG", "Signin error - not logged in")


    }

    fun downloadFileFromGDrive(id: String) {
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).launch {
                Log.e("idDownload", id)
                val file = File(context?.filesDir, "${id}.jpg")
                if (!file.exists()) {
                    try {
                        val gDriveFile = googleDriveService.Files().get(id).execute()
                        Log.d(
                            "TAG",
                            "accessDriveFiles: name=${gDriveFile.name} id=${gDriveFile.id}}"
                        )

                    } catch (e: Exception) {
                        println("!!! Handle Exception $e")
                    }
                }
            }
        } ?: ""
    }

    fun fetchData() {
        var path =
            File(Environment.getExternalStorageDirectory().toString() + "/Recovery").toString()

        //Day la tat ca thu muc trong may
        (requireActivity() as MainActivity).showHideProgress(true)
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (Utils.getFileList(path) != null) {
                    val job = CoroutineScope(Dispatchers.IO).async {
                        checkFileOfDirectory(
                            Utils.getFileList(path)
                        )
                    }
                    job.await()
                    withContext(Dispatchers.Main) {
                        (requireActivity() as MainActivity).showHideProgress(false)
                    }
                }

            }

        } catch (e: Exception) {
            Log.e("Exception", "doInBackground: " + e.message)
        }

    }

    fun checkFileOfDirectory(fileArr: Array<File>) {
        if (fileArr != null) {
            for (i in fileArr.indices) {
                Log.d("TAG", "checkFileOfDirectory: ${fileArr[i].path}")
                if (fileArr[i].isDirectory) {
                    checkFileOfDirectory(Utils.getFileList(fileArr[i].path))
                } else {

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(fileArr[i].path, options)
                    if (!(options.outWidth == -1 || options.outHeight == -1)) {
                        val file = File(fileArr[i].path)
                        mFile.add(FilesModel(file, false))
                        Log.d("TAG", "checkFileOfDirectory: ${file.name}")
                    }
                }
            }
        }
    }

    /**
     * Creates a Folder in the user's My Drive.
     */
    suspend fun createFolder(): String {
        var result = ""
        CoroutineScope(Dispatchers.IO).async {
             val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
            val FOLDER_NAME = "DataRecovery"
            val metadata: com.google.api.services.drive.model.File =
                com.google.api.services.drive.model.File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType(FOLDER_MIME_TYPE)
                    .setName(FOLDER_NAME)
            val googleFolder: com.google.api.services.drive.model.File =
                getDriveService()?.files()?.create(metadata)?.execute()
                    ?: throw IOException("Null result when requesting Folder creation.")
            result= googleFolder.id
        }.await()
        return result
    }

    /**
     * Check Folder present or not in the user's My Drive.
     */
    suspend fun isFolderPresent(): String {
        var isPresent = ""
        getDriveService()?.let { googleDriveService ->
            CoroutineScope(Dispatchers.IO).async {
                   val FOLDER_NAME = "DataRecovery"
                var result: FileList ?= null
                if(googleDriveService != null){
                     result = getDriveService()!!.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
                        .execute()
                }
                if(result != null){
                    for (file in result!!.files) {
                        if (file.name == FOLDER_NAME){
                            Log.d("TAG", "isFolderPresent: ${file.name}")
                            isPresent = file.id
                            break
                        }
                    }
                }

            }.await()
        }?: ""
        return isPresent
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        this.mContext = null
    }
}