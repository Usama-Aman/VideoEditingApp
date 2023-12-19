package com.elementary.youmerge.videofilters

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daasuu.epf.EPlayerView
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.elementary.youmerge.R
import com.elementary.youmerge.utils.AppUtils
import com.elementary.youmerge.utils.Constants
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File

class VideoFilterActivity : AppCompatActivity() {


    lateinit var player: SimpleExoPlayer
    lateinit var ePlayerView: EPlayerView

    private lateinit var filterView: FrameLayout
    private lateinit var filtersRecyclerView: RecyclerView
    private lateinit var addFilterAdapter: AddFilterAdapter
    private lateinit var btnPlayPause: ImageView
    private var filterPosition = 0

    private lateinit var btnSaveFilteredVideo: TextView

    private var filePath = ""

    private lateinit var directoryMergeVideos: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_filters)


        filePath = intent.getStringExtra("filePath")!!

        directoryMergeVideos = File(
            getExternalFilesDir(null), "${Constants.APP_NAME}/${Constants.MERGER_VIDEOS_FOLDER}"
        )

        initViews()

        setUpFiltersRecycler()
        setUpSimpleExoPlayer()
        setUpGlPlayerView()
    }

    private fun setUpFiltersRecycler() {
//        addFilterAdapter = AddFilterAdapter(this, filePath)
        filtersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = addFilterAdapter
        }
    }

    private fun initViews() {
        filterView = findViewById(R.id.filterView)
        filtersRecyclerView = findViewById(R.id.filtersRecyclerView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnSaveFilteredVideo = findViewById(R.id.btnSaveFilteredVideo)


        btnPlayPause.setOnClickListener {
            if (player.isPlaying)
                player.pause()
            else
                player.play()
        }


        btnSaveFilteredVideo.setOnClickListener {
            AppUtils.showDialog(this)

            val filterFilepath =
                "${directoryMergeVideos}/${System.currentTimeMillis()}$filterPosition.mp4"

            Mp4Composer(filePath, filterFilepath)
                .rotation(Rotation.NORMAL)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(
                    FilterSave.createGlFilter(
                        FilterSave.createFilterList()[filterPosition],
                        this
                    )
                )
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        Log.d("", "onProgress Filter = " + progress * 100)
                    }

                    override fun onCurrentWrittenVideoTime(timeUs: Long) {
                        Log.d("", "onCurrentWrittenVideoTime Filter = $timeUs")
                    }

                    override fun onCompleted() {
                        AppUtils.dismissDialog()
                        Log.d("mAppName", "onCompleted() Filter : $filterFilepath")
                    }

                    override fun onCanceled() {
                        AppUtils.dismissDialog()
                        Log.d("mAppName", "onCanceled")
                    }

                    override fun onFailed(exception: Exception) {
                        AppUtils.dismissDialog()
                        Log.e("mAppName", "onFailed() Filter", exception)
                    }
                })
                .start()


        }
    }

    private fun setUpSimpleExoPlayer() {

        val dataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.fromFile(File(filePath)))

        player = ExoPlayerFactory.newSimpleInstance(this).apply {
            prepare(videoSource)
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    private fun setUpGlPlayerView() {
        ePlayerView = EPlayerView(this).apply {
            setSimpleExoPlayer(player)
        }
        filterView.addView(ePlayerView)
        ePlayerView.onResume()

    }

    fun onFilterItemClick(position: Int) {
        filterPosition = position
        ePlayerView.setGlFilter(
            FilterType.createGlFilter(
                FilterType.createFilterList()[filterPosition],
                this
            )
        )
    }


}