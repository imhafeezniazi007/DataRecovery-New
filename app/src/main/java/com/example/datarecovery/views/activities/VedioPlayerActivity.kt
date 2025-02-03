package com.example.datarecovery.views.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityAudiosBinding
import com.example.datarecovery.databinding.ActivityVedioBinding
import com.example.datarecovery.databinding.ActivityVedioPlayerBinding

class VedioPlayerActivity : BaseActivity() {
    lateinit var binding: ActivityVedioPlayerBinding
    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVedioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val path = intent.getStringExtra("path")

        // assigning id of VideoView from
        // activity_main.xml layout file
        val simpleVideoView = findViewById<View>(R.id.simpleVideoView) as VideoView

        if (mediaControls == null) {
            // creating an object of media controller class
            mediaControls = MediaController(this)

            // set the anchor view for the video view
            mediaControls!!.setAnchorView(simpleVideoView)
        }

        // set the media controller for video view
        simpleVideoView.setMediaController(mediaControls)

        /*// set the absolute path of the video file which is going to be played
        simpleVideoView.setVideoURI(
            Uri.parse("android.resource://"
                + packageName + "/" + R.raw.images))*/

        simpleVideoView.setVideoPath(path)

        simpleVideoView.requestFocus()

        // starting the video
        simpleVideoView.start()

        // display a toast message
        // after the video is completed
        simpleVideoView.setOnCompletionListener {
            Toast.makeText(applicationContext, getString(R.string.video_completed),
                Toast.LENGTH_LONG).show()
            true
        }

        // display a toast message if any
        // error occurs while playing the video
        simpleVideoView.setOnErrorListener { mp, what, extra ->
            Toast.makeText(applicationContext, getString(R.string.error_playing_error), Toast.LENGTH_LONG).show()
            false
        }

    }
}