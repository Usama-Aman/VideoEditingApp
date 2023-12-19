package com.elementary.youmerge

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.SparseIntArray
import android.view.MenuItem
import android.view.Surface
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.youmerge.all_videos.AllVideosFragment
import com.elementary.youmerge.merge_videos.MergeVideosFragment
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.SharedPreference
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var mLastClickTime: Long = 0

    private var mProjectionManager: MediaProjectionManager? = null
    private lateinit var directoryVideos: File
    private lateinit var directoryMergeVideos: File
    private lateinit var directoryTrimmedVideos: File

    private lateinit var bottomNavigationView: BottomNavigationView

    var videoResolutionsList: MutableList<VideoSettingModel> = ArrayList()
    var bitRateList: MutableList<VideoSettingModel> = ArrayList()
    var frameRateList: MutableList<VideoSettingModel> = ArrayList()

    private lateinit var recordVideoButton: ImageView
    private lateinit var recordServiceIntent: Intent

    companion object {
        private const val TAG = "MainActivity"
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTheDefault()

        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        initViews()
        createDirectories()
    }

    private fun createDirectories() {
        directoryVideos = File(
            getExternalFilesDir(null), "${Constants.APP_NAME}/${Constants.VIDEOS_FOLDER}"
        )

        directoryMergeVideos = File(
            getExternalFilesDir(null),
            "${Constants.APP_NAME}/${Constants.MERGER_VIDEOS_FOLDER}"
        )
        directoryTrimmedVideos = File(
            getExternalFilesDir(null),
            "${Constants.APP_NAME}/${Constants.TRIMMED_VIDEOS_FOLDER}"
        )

        val f1 = File(getExternalFilesDir(null), Constants.APP_NAME)
        if (!f1.exists()) f1.mkdirs()

        if (!directoryVideos.exists()) directoryVideos.mkdirs()
        if (!directoryMergeVideos.exists()) directoryMergeVideos.mkdirs()
        if (!directoryTrimmedVideos.exists()) directoryTrimmedVideos.mkdirs()

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

            SharedPreference.saveInt(this, Constants.VIDEO_RESOLUTION, 3)
            SharedPreference.saveInt(this, Constants.BIT_RATE, 7)
            SharedPreference.saveInt(this, Constants.FRAME_RATE, 4)
            SharedPreference.saveBoolean(this, Constants.isFirstLoggedIn, true)
        }
    }

    private fun initViews() {
        recordVideoButton = findViewById(R.id.recordVideoButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        requestForPermission()
        bottomNavigationView.selectedItemId = R.id.allVideos

        recordVideoButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this))
                askForSystemOverlayPermission()
            else
                onToggleScreenShare()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//            return false;
//        }
//        mLastClickTime = SystemClock.elapsedRealtime();
        when (item.itemId) {
            R.id.allVideos -> {
                launchFragment(AllVideosFragment())
            }
            R.id.myVideos -> {
                launchFragment(MergeVideosFragment())
            }
            R.id.settings -> {
                launchFragment(SettingsFragment())
            }
            R.id.goPro -> {
                launchFragment(GoProActivity())
            }
        }
        return true
    }

    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, fragment)
            .commit()
    }


    private fun requestForPermission() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) + ContextCompat
                .checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.RECORD_AUDIO
                )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] +
                    grantResults[1] != PackageManager.PERMISSION_GRANTED
                ) {
                    requestForPermission()
                }
                return
            }
        }
    }

    private fun askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${packageName}")
            )
            startActivityForResult(intent, 1000)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Exception: $requestCode")
        }
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Exception: RESULT NOT OK")
        } else {
            /*start the service and send result code*/
            recordServiceIntent = Intent(this, RecordService::class.java)
            recordServiceIntent.putExtra("resultCode", resultCode)
            recordServiceIntent.putExtra("resultData", data)
            startService(recordServiceIntent)
        }

    }

    private fun onToggleScreenShare() {

        if (isMyServiceRunning(RecordService::class.java)) {
            stopService(Intent(this, RecordService::class.java))
            bottomNavigationView.selectedItemId = R.id.allVideos
        } else {
            shareScreen()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun shareScreen() {
        startActivityForResult(
            mProjectionManager!!.createScreenCaptureIntent(),
            REQUEST_CODE
        )
    }

    fun goToSelectedFragment(fragmentID: Int) {
        bottomNavigationView.selectedItemId = fragmentID
    }

    override fun recreate() {
//        super.recreate()
        val savedInstanceState = Bundle()
        onSaveInstanceState(savedInstanceState)
        super.onDestroy()
        onCreate(savedInstanceState)
    }

}
