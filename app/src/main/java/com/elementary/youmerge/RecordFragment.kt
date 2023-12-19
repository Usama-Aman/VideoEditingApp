package com.elementary.youmerge

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.Constants.APP_NAME
import com.elementary.youmerge.utils.Constants.MERGER_VIDEOS_FOLDER
import com.elementary.youmerge.utils.Constants.TRIMMED_VIDEOS_FOLDER
import com.elementary.youmerge.utils.Constants.VIDEOS_FOLDER
import java.io.File

class RecordFragment : Fragment() {


    private var mToggleButton: ToggleButton? = null
    private lateinit var textView: TextView
    private lateinit var btnGoPro: LinearLayout
    private lateinit var v: View

    private lateinit var directoryVideos: File
    private lateinit var directoryMergeVideos: File
    private lateinit var directoryTrimmedVideos: File

    private var isVideoRecordingStarted = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.record_fragment, container, false)

        initViews()

        return v
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initViews() {

        btnGoPro = v.findViewById(R.id.btnGoPro)
        textView = v.findViewById(R.id.textView)
        mToggleButton = v.findViewById(R.id.toggle)
        mToggleButton?.isChecked = isVideoRecordingStarted

        textView.text =
            if (!isVideoRecordingStarted) "Record" else "Recording"


//        mToggleButton!!.setOnClickListener {
//            if (!Settings.canDrawOverlays(context))
//                (activity as MainActivity).askForSystemOverlayPermission()
//            else {
////                startService(Intent(this, BubbleButtonService::class.java))
////                finish()
//                isVideoRecordingStarted = !isVideoRecordingStarted
//                (activity as MainActivity).onToggleScreenShare(isVideoRecordingStarted)
//                textView.text =
//                    if (!isVideoRecordingStarted) "Record" else "Recording"
//
//            }
//        }

        btnGoPro.setOnClickListener {
            context!!.startActivity(Intent(context, GoProActivity::class.java))
        }

        createDirectories()
    }

    private fun createDirectories() {
        directoryVideos = File(
            context!!.getExternalFilesDir(null), "${Constants.APP_NAME}/$VIDEOS_FOLDER"
        )

        directoryMergeVideos = File(
            context!!.getExternalFilesDir(null), "$APP_NAME/$MERGER_VIDEOS_FOLDER"
        )
        directoryTrimmedVideos = File(
            context!!.getExternalFilesDir(null), "$APP_NAME/$TRIMMED_VIDEOS_FOLDER"
        )

        val f1 = File(context!!.getExternalFilesDir(null), APP_NAME)
        if (!f1.exists()) f1.mkdirs()

        if (!directoryVideos.exists()) directoryVideos.mkdirs()
        if (!directoryMergeVideos.exists()) directoryMergeVideos.mkdirs()
        if (!directoryTrimmedVideos.exists()) directoryTrimmedVideos.mkdirs()

    }


}
