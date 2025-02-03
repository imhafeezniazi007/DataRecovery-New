package com.example.datarecovery.utils

import android.content.Context
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import java.io.File

class MediaScanner(context: Context) : MediaScannerConnectionClient {
    val destFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/Recovery")

    private val mScannerConnection: MediaScannerConnection
    override fun onMediaScannerConnected() {
        mScannerConnection.scanFile(destFile.absolutePath, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        mScannerConnection.disconnect()
    }

    init {
        mScannerConnection = MediaScannerConnection(context, this)
        mScannerConnection.connect()
    }
}