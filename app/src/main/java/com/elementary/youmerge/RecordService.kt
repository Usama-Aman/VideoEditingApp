package com.elementary.youmerge

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.SharedPreference
import java.io.File

class RecordService : Service() {

    companion object {
        private const val NOTIFICATION_CHANNEL_NAME = "Recording"
        private const val NOTIFICATION_CHANNEL_ID = "Recording"
        private const val NOTIFICATION_ID = 9999

        private const val TAG = "RecordService"
        private const val REQUEST_CODE = 1000
        private var DISPLAY_WIDTH = 720
        private var DISPLAY_HEIGHT = 1280
        private val ORIENTATIONS = SparseIntArray()
        private const val REQUEST_PERMISSIONS = 10


        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private var mScreenDensity = 0
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private lateinit var mMediaProjectionCallback: MediaProjectionCallback
    private var mMediaRecorder: MediaRecorder? = null
    private lateinit var directoryVideos: File
    private lateinit var directoryMergeVideos: File

    var videoResolutionsList: MutableList<VideoSettingModel> = ArrayList()
    var bitRateList: MutableList<VideoSettingModel> = ArrayList()
    var frameRateList: MutableList<VideoSettingModel> = ArrayList()

    private var resultCode = 0
    private var resultData: Intent? = null

    override fun onCreate() {
        super.onCreate()
        setTheDefault()
        directoryVideos = File(
            getExternalFilesDir(null), "${Constants.APP_NAME}/${Constants.VIDEOS_FOLDER}"
        )

        val metrics = DisplayMetrics()
        val display = display

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.getRealMetrics(metrics)
//        }
        mScreenDensity = metrics.densityDpi
        DISPLAY_HEIGHT = metrics.heightPixels
        DISPLAY_WIDTH = metrics.widthPixels


        mMediaRecorder = MediaRecorder()
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager


        startInForeground()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent?.action == null) {
                resultCode = intent?.getIntExtra("resultCode", 1337)!!
                resultData = intent.getParcelableExtra("resultData")

            }

            initRecorder()
            mMediaProjectionCallback = MediaProjectionCallback()
            mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, resultData!!)
            mMediaProjection?.registerCallback(mMediaProjectionCallback, null)
            mVirtualDisplay = createVirtualDisplay()
            mMediaRecorder!!.start()
        } catch (e: java.lang.Exception) {
//            Toast.makeText(this, "Resources are not free.", Toast.LENGTH_SHORT).show()
            stopSelf()
        }

        return START_STICKY
    }

    private fun setTheDefault() {
        videoResolutionsList.clear()
        frameRateList.clear()
        bitRateList.clear()

        videoResolutionsList.add(VideoSettingModel(1, "1080 HD", 1080))
        videoResolutionsList.add(VideoSettingModel(2, "720 HD", 720))
        videoResolutionsList.add(VideoSettingModel(3, "480 SD", 480))
        videoResolutionsList.add(VideoSettingModel(4, "360 SD", 360))

        bitRateList.add(VideoSettingModel(1, "12 Mbps", 12))
        bitRateList.add(VideoSettingModel(2, "8 Mbps", 8))
        bitRateList.add(VideoSettingModel(3, "6 Mbps", 6))
        bitRateList.add(VideoSettingModel(4, "5 Mbps", 5))
        bitRateList.add(VideoSettingModel(5, "4 Mbps", 4))
        bitRateList.add(VideoSettingModel(6, "3 Mbps", 3))
        bitRateList.add(VideoSettingModel(7, "2 Mbps", 2))
        bitRateList.add(VideoSettingModel(8, "1 Mbps", 1))

        frameRateList.add(VideoSettingModel(1, "60 fps", 60))
        frameRateList.add(VideoSettingModel(2, "50 fps", 50))
        frameRateList.add(VideoSettingModel(3, "30 fps", 30))
        frameRateList.add(VideoSettingModel(4, "25 fps", 25))
        frameRateList.add(VideoSettingModel(5, "24fps", 24))

        if (!SharedPreference.getBoolean(this, Constants.isFirstLoggedIn)) {

            SharedPreference.saveInt(this, Constants.VIDEO_RESOLUTION, 1)
            SharedPreference.saveInt(this, Constants.BIT_RATE, 1)
            SharedPreference.saveInt(this, Constants.FRAME_RATE, 2)

        } else {
            SharedPreference.saveBoolean(this, Constants.isFirstLoggedIn, true)
        }
    }

    private fun initRecorder() {
        try {

            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setOutputFile("$directoryVideos/${System.currentTimeMillis()}.mp4")
            mMediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mMediaRecorder!!.setVideoEncodingBitRate(
                bitRateList[SharedPreference.getInt(
                    this, Constants.BIT_RATE,
                    1
                )].value * 1024 * 1000
            )
            mMediaRecorder!!.setVideoFrameRate(
                frameRateList[SharedPreference.getInt(this, Constants.FRAME_RATE, 1)].value
            )
//            val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
//                display?.rotation
//            else this.windowManager?.defaultDisplay?.rotation
//            val orientation = MainActivity.ORIENTATIONS[rotation?.plus(90)!!]
//            mMediaRecorder!!.setOrientationHint(orientation)
            mMediaRecorder!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay(
            "MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mMediaRecorder!!.surface, null /*Callbacks*/, null /*Handler*/
        )
    }


    private fun startInForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText("Recording is working in the foreground")
//                .setContentIntent(pendingIntent)
        val notification: Notification = builder.build()
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        startForeground(NOTIFICATION_ID, notification)
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            try {
//            if (isVideoRecording) {
//            mMediaRecorder!!.stop()
//            mMediaRecorder!!.reset()
                Log.v(TAG, "Recording Stopped")
//            }
                mMediaProjection = null
                stopScreenSharing()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder!!.stop()
                mMediaRecorder!!.reset()
                Log.v(TAG, "Stopping Recording")
            }
            stopScreenSharing()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun stopScreenSharing() {
        try {
            if (mVirtualDisplay == null) {
                return
            }
            mVirtualDisplay!!.release()
            //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
            // be reused again
            destroyMediaProjection()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun destroyMediaProjection() {
        try {
            if (mMediaProjection != null) {
                mMediaProjection!!.unregisterCallback(mMediaProjectionCallback)
                mMediaProjection!!.stop()
                mMediaProjection = null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        Log.i(TAG, "MediaProjection Stopped")
    }
}