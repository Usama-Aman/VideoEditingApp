package com.elementary.youmerge.video_crop_prac

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.canhub.cropper.CropImage
import com.daasuu.epf.EPlayerView
import com.daasuu.epf.PlayerScaleType
import com.elementary.youmerge.R
import com.elementary.youmerge.cropview.window.CropVideoView
import com.elementary.youmerge.utils.AppUtils
import com.elementary.youmerge.utils.Constants
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import kotlin.math.abs

class VideoCropActivity : AppCompatActivity() {

    var filePath = ""
    private lateinit var directoryTrimmedVideos: File

    private lateinit var cropVideoView: CropVideoView
    private lateinit var btnCancel: TextView
    private lateinit var btnDone: TextView
    private lateinit var btnRotate: TextView
    private lateinit var cropImageView: CropImageView

    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_crop)

        filePath = intent.getStringExtra("filePath")!!

        initViews()
    }

    private fun initViews() {
        directoryTrimmedVideos = File(
            getExternalFilesDir(null), "${Constants.APP_NAME}/${Constants.TRIMMED_VIDEOS_FOLDER}"
        )

        cropVideoView = findViewById(R.id.cropVideoView)
        btnCancel = findViewById(R.id.btnCancel)
        btnDone = findViewById(R.id.btnDone)
        btnRotate = findViewById(R.id.btnRotate)

        cropImageView = findViewById(R.id.cropImageView)


        val bitmap = ThumbnailUtils.createVideoThumbnail(
            filePath,
            MediaStore.Video.Thumbnails.MINI_KIND
        )
        cropImageView.setImageBitmap(bitmap)



//        cropVideoView.setPlayer(this, filePath)
//        cropVideoView.setFixedAspectRatio(false)

//        fetchVideoInfo(filePath)
//
        var degree = 0
        btnRotate.setOnClickListener {
            degree += 90
            cropImageView.rotateImage(degree)
        }
//
        btnCancel.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {
            AppUtils.preventTwoClick(btnDone)
            startCropVideo()
//            cropImageView.setImageBitmap(cropImageView.croppedImage)
        }
    }

    private fun fetchVideoInfo(uri: String) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(File(uri).absolutePath)
        val videoWidth =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val videoHeight =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        val rotationDegrees =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
        cropVideoView.initBounds(videoWidth, videoHeight, rotationDegrees)
    }

    private fun startCropVideo() {
        dialog = ProgressDialog(this)
        dialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog?.setTitle("Loading")
        dialog?.setMessage("Please wait")
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.isIndeterminate = true
        dialog?.show()


//        val cropRect: Rect = cropVideoView.cropRect
        val cropRect: Rect = cropImageView.cropRect
        val cropDegree = cropImageView.rotatedDegrees

        val width = abs(cropRect.left - cropRect.right)
        val height = abs(cropRect.top - cropRect.bottom)
        val x = cropRect.left
        val y = cropRect.top


        val cropOutputPath = "$directoryTrimmedVideos/${System.currentTimeMillis()}.mp4"
        val crop = String.format(
            "crop=%d:%d:%d:%d:exact=0",
            cropRect.right,
            cropRect.bottom,
            cropRect.left,
            cropRect.top
        )

//        val crop = String.format(
//            "crop=%d:%d:%d:%d:exact=0",
//            width,
//            height,
//            x,
//            y
//        )



        val cmd = arrayOf(
            "-y",
            "-i",
            filePath,
//            "-vf",
            "-filter:v",
            crop,
//            "transpose=0"/*:passthrough=portrait*/, /*0 = normal, 1= right rotate, 2- left rotate, 3 = left rotate, 4 = left rotate, */

            /*transponse=1 -> right rotate 90 , transpose=1,transpose=1 -> 180 , transpose=2 -> left rotate 270*/

//            "rotate=90",
//            "-metadata:s:v:0","rotate=90",
//            "-threads",
//            " 5",
            cropOutputPath
        )

        val ee = FFmpeg.executeAsync(cmd, ExecuteCallback { executionId, returnCode ->

            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    dialog?.dismiss()
                    Log.i(Config.TAG, "Crop command execution completed successfully.")
                    val intent = Intent()
                    intent.putExtra("croppedFilePath", cropOutputPath)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                Config.RETURN_CODE_CANCEL -> {
                    dialog?.dismiss()
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Cannot Crop", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                else -> {
                    dialog?.dismiss()
                    Log.i(
                        Config.TAG,
                        String.format(
                            "Crop command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this, String.format(
                                "Crop command execution failed with returnCode=%d.",
                                returnCode
                            ), Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                }
            }


        })
    }

}