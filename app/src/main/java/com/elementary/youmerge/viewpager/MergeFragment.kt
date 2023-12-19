package com.elementary.youmerge.viewpager

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daasuu.epf.EPlayerView
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.elementary.youmerge.R
import com.elementary.youmerge.trimmer.TrimmerListModel
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.SharedPreference
import com.elementary.youmerge.videofilters.AddFilterAdapter
import com.elementary.youmerge.videofilters.FilterSave
import com.google.android.exoplayer2.SimpleExoPlayer
import com.tomergoldst.tooltips.ToolTipsManager
import com.video.trimmer.interfaces.OnProgressVideoListener
import com.video.trimmer.interfaces.OnRangeSeekBarListener
import com.video.trimmer.interfaces.OnTrimVideoListener
import com.video.trimmer.interfaces.OnVideoListener
import com.video.trimmer.view.RangeSeekBarView
import com.video.trimmer.view.Thumb
import com.video.trimmer.view.TimeLineView
import com.video.trimmer.view.VideoTrimmer
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.ViewType
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.ceil

class MergeFragment : Fragment() {

    private lateinit var v: View
    lateinit var videosData: TrimmerListModel

    private lateinit var mPhotoEditorView: PhotoEditorView
    private lateinit var trimVideoTrimmer: VideoTrimmer

    private lateinit var filterView: FrameLayout
    private lateinit var filtersRecyclerView: RecyclerView
    private lateinit var addFilterAdapter: AddFilterAdapter
    private var filterPosition = 0

    private lateinit var directoryTrimmedVideos: File
    private var dialog: ProgressDialog? = null

    lateinit var player: SimpleExoPlayer
    lateinit var ePlayerView: EPlayerView

    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: ImageView
    private lateinit var timeLineView: TimeLineView
    private lateinit var videoSeekBar: SeekBar

    lateinit var ivRulerLeft: ImageView
    lateinit var ivRulerTop: ImageView


    // by zahab

    lateinit var mSrc: Uri
    private var mFinalPath: String? = null

    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoListener> = ArrayList()

    private var mOnTrimVideoListener: OnTrimVideoListener? = null
    private var mOnVideoListener: OnVideoListener? = null

    private var mDuration = 0f
    private var mTimeVideo = 0f
    private var mStartPosition = 0f

    private var mEndPosition = 0f
    private var mResetSeekBar = true
    private val mMessageHandler = MessageHandler(this)

    lateinit var timeLineBar: RangeSeekBarView
    lateinit var layout_surface_view: ConstraintLayout

    private lateinit var videoMediPlayer: MediaPlayer

    private lateinit var verticalRulerGuideline: View
    private lateinit var horizontalRulerGuideline: View

    private lateinit var toolTipsManager: ToolTipsManager
    private lateinit var toolTip: TextView

    private var leftRulerBitmap: Bitmap? = null
    private var topRulerBitmap: Bitmap? = null
    private var croppedLeftRulerBitmap: Bitmap? = null
    private var croppedTopRulerBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.item_trimmer_list, container, false)

        videosData = arguments?.getParcelable("videoData")!!

        initViews()
        initializeVideoView()
        initTimeLinebar()
        setUpFiltersRecycler()

        return v
    }

    private fun initViews() {

        toolTipsManager = ToolTipsManager()

        directoryTrimmedVideos = File(
            context!!.getExternalFilesDir(null),
            "${Constants.APP_NAME}/${Constants.TRIMMED_VIDEOS_FOLDER}"
        )

        toolTip = v.findViewById(R.id.toolTip)
        mPhotoEditorView = v.findViewById(R.id.photoEditorView)
        filtersRecyclerView = v.findViewById(R.id.filtersRecyclerView)
        videoView = v.findViewById(R.id.videoView)
        btnPlayPause = v.findViewById(R.id.btnPlayPause)
        timeLineView = v.findViewById(R.id.timeLineVie)
        videoSeekBar = v.findViewById(R.id.videoSeekBar)
        timeLineBar = v.findViewById(R.id.timeLineBar)
        layout_surface_view = v.findViewById(R.id.layout_surface_view)
        horizontalRulerGuideline = v.findViewById(R.id.horizontalRulerGuideline)
        verticalRulerGuideline = v.findViewById(R.id.verticalRulerGuideline)
        ivRulerLeft = v.findViewById(R.id.ivRulerLeft)
        ivRulerTop = v.findViewById(R.id.ivRulerTop)

        initializePhotoEditor()
    }

    private fun initTimeLinebar() {
        timeLineView.setVideo(Uri.parse(videosData.croppedPath))
        timeLineBar.setOnTouchListener(View.OnTouchListener { v, event ->
            try {
                val action = event.action
                when (action) {
                    MotionEvent.ACTION_DOWN ->                    // Disallow ScrollView to intercept touch events.
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP ->                     //Allow ScrollView to intercept touch events once again.
                        v.parent.requestDisallowInterceptTouchEvent(true)
                }
                v.onTouchEvent(event)
            } catch (e: Exception) {
            }
            true
        })

        setUpListeners()
        setUpMargins()
    }

    private fun setUpMargins() {
        val marge = timeLineBar.thumbs[0].widthBitmap
        val lp = timeLineView.layoutParams as FrameLayout.LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        timeLineView.layoutParams = lp
    }

    private fun setMaxDuration(maxDuration: Int) {
        mMaxDuration = maxDuration * 1000
    }

    private fun setMinDuration(minDuration: Int) {
        mMinDuration = minDuration * 1000
    }

    fun initializeVideoView() {
        videoView.setVideoURI(Uri.parse(videosData.croppedPath))
        videoView.requestFocus()
    }

    private fun initializePhotoEditor() {
        if (videosData.photoEditor == null) {
            videosData.photoEditor = PhotoEditor.Builder(context, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build()

            var currentMovingViewType = ""
            videosData.photoEditor?.setOnPhotoEditorListener(object : OnPhotoEditorListener {
                override fun onEditTextChangeListener(
                    rootView: View?,
                    text: String?,
                    colorCode: Int
                ) {
                }

                override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {}

                override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {}

                override fun onStartViewChangeListener(viewType: ViewType?, x: Float, y: Float) {
                    currentMovingViewType = viewType?.name!!
                    println("${viewType?.name} started")



                    if (viewType.name != "BRUSH_DRAWING") {
                        if (x != -11111111f || y != 11111111f) {
                            verticalRulerGuideline.translationX = x
                            horizontalRulerGuideline.translationY = y
                        }
                    }
                }

                override fun onStopViewChangeListener(viewType: ViewType?) {
                    if (viewType?.name != "BRUSH_DRAWING") {
                        val intent = Intent(Constants.HideShowViewBroadCastReceiver)
                        intent.putExtra("hideShowViews", true)
                        timeLineBar.visibility = View.VISIBLE
                        videoSeekBar.visibility = View.VISIBLE
                        timeLineView.visibility = View.VISIBLE
                        LocalBroadcastManager.getInstance(activity?.applicationContext!!)
                            .sendBroadcast(intent)
                    }
                    toolTip.visibility = View.GONE
                    verticalRulerGuideline.visibility = View.GONE
                    horizontalRulerGuideline.visibility = View.GONE
                }

                override fun getViewCoordinates(
                    x: Float,
                    y: Float,
                    childView: View,
                    parentView: RelativeLayout,
                    viewType: ViewType,
                    percentageOfViewInParentXaxis: Float,
                    percentageOfViewInParentYaxis: Float,
                    viewCoard_X: Float,
                    viewCoard_Y: Float,
                ) {
                    parentView.parent.requestDisallowInterceptTouchEvent(true)
                    if (viewType.name != "BRUSH_DRAWING") {
                        val intent = Intent(Constants.HideShowViewBroadCastReceiver)
                        intent.putExtra("hideShowViews", false)
                        timeLineBar.visibility = View.GONE
                        videoSeekBar.visibility = View.GONE
                        timeLineView.visibility = View.GONE
                        LocalBroadcastManager.getInstance(activity?.applicationContext!!)
                            .sendBroadcast(intent)

                        if (SharedPreference.getBoolean(context, Constants.HIDE_SHOW_RULER)) {
                            verticalRulerGuideline.visibility = View.VISIBLE
                            horizontalRulerGuideline.visibility = View.VISIBLE
                        } else {
                            verticalRulerGuideline.visibility = View.GONE
                            horizontalRulerGuideline.visibility = View.GONE
                        }
                    }

                    verticalRulerGuideline.translationX = verticalRulerGuideline.translationX + x
                    horizontalRulerGuideline.translationY =
                        horizontalRulerGuideline.translationY + y

                    println("Guide Top = ${verticalRulerGuideline.translationX}")
                    println("Guide Left= ${horizontalRulerGuideline.translationY}")

                    val topOriginalImageWidth = 1242
                    val leftOriginalImageHeight = 2685

                    /*---------- Top Ruler Calculations --------------*/
                    val topRulerPercentageCurrentShowing: Double =
                        ((croppedTopRulerBitmap?.width!!.toDouble() * 100) / topRulerBitmap?.width!!.toDouble())

                    val topRulerPointsCurrentShowing: Double =
                        (topRulerPercentageCurrentShowing * topOriginalImageWidth) / 100

                    val currentXAxisPointOnRuler: Double =
                        (topRulerPointsCurrentShowing * percentageOfViewInParentXaxis) / 100

                    /*---------- Left Ruler Calculations --------------*/
                    val leftRulerPercentageCurrentShowing: Double =
                        ((croppedLeftRulerBitmap?.height!!.toDouble() * 100) / leftRulerBitmap?.height!!.toDouble())

                    val leftRulerPointsCurrentShowing: Double =
                        (leftRulerPercentageCurrentShowing * leftOriginalImageHeight) / 100

                    val currentYAxisPointOnRuler: Double =
                        (leftRulerPointsCurrentShowing * percentageOfViewInParentYaxis) / 100

                    toolTip.visibility = View.VISIBLE
                    toolTip.text =
                        "x=${ceil(currentXAxisPointOnRuler)} , y=${ceil(currentYAxisPointOnRuler)}"

//                    if (viewType != ViewType.IMAGE) {
                        toolTip.x =
                            ((((layout_surface_view.width - videoView.width).toFloat()) / 2) + viewCoard_X)
                        toolTip.y =
                            ((((layout_surface_view.height - videoView.height).toFloat()) / 2) + viewCoard_Y)
//                    }
//                    else {
//                        toolTip.x =
//                            ((((layout_surface_view.width - videoView.width).toFloat()) / 2) + viewCoard_X)
//                        toolTip.y =
//                            ((((layout_surface_view.height - videoView.height).toFloat()) / 2) - viewCoard_Y)
//                    }

                    toolTip.animate()

                    println("Current Ruler X Points = $topRulerPointsCurrentShowing")
                    println("Current Ruler Y Points = $leftRulerPointsCurrentShowing")
                    println("Current X Axis Point on Ruler = ${ceil(currentXAxisPointOnRuler)}")
                    println("Current Y Axis Point on Ruler = ${ceil(currentYAxisPointOnRuler)}")
                }
            })
        }
    }

    fun enableBrush(selectedColor: Int) {
        initializePhotoEditor()
        videosData.photoEditor?.setBrushDrawingMode(true)
        videosData.photoEditor?.brushColor = selectedColor
        videosData.photoEditor?.brushSize = 12f
    }

    fun disableBrush() {
        initializePhotoEditor()
        videosData.photoEditor?.setBrushDrawingMode(false)
    }

    fun setBrushColor(selectedColor: Int) {
        initializePhotoEditor()
        videosData.photoEditor?.brushColor = selectedColor
    }

    fun addTextSticker(text: String, selectedColor: Int, backgroundColor: Int) {
        initializePhotoEditor()
        videosData.photoEditor?.addText(text, selectedColor, backgroundColor)
    }

    fun addImageSticker(/*bitmap: Bitmap*/ path: String) {
        initializePhotoEditor()
        videosData.photoEditor?.addImage(path)
    }

    fun addEmojiSticker(text: String) {
        initializePhotoEditor()
        videosData.photoEditor?.addEmoji(text)
    }

    fun clearPhotoEditor() {
        initializePhotoEditor()
        videosData.photoEditor?.clearAllViews()
    }

    fun undoEditing() {
        initializePhotoEditor()
        videosData.photoEditor?.undo()
    }

    private fun setUpFiltersRecycler() {
        addFilterAdapter = AddFilterAdapter(this, videosData.croppedPath)
        filtersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = addFilterAdapter
        }
    }

    fun onFilterItemClick(position: Int) {
        dialog = ProgressDialog(context!!)
        dialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog?.setTitle("Loading")
        dialog?.setMessage("Please wait")
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.isIndeterminate = true
        dialog?.show()

        filterPosition = position

        val filterFilepath =
            "${directoryTrimmedVideos}/${System.currentTimeMillis()}$filterPosition.mp4"

        Mp4Composer(videosData.croppedPath, filterFilepath)
            .rotation(Rotation.NORMAL)
            .fillMode(FillMode.PRESERVE_ASPECT_FIT)
            .filter(
                FilterSave.createGlFilter(
                    FilterSave.createFilterList()[filterPosition],
                    context
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
                    dialog?.dismiss()

                    Handler(Looper.getMainLooper()).post {
                        videosData.croppedPath = filterFilepath
                        initializeVideoView()
                    }
                    Log.d("mAppName", "onCompleted() Filter : $filterFilepath")
                }

                override fun onCanceled() {
                    dialog?.dismiss()
                    Log.d("mAppName", "onCanceled")
                }

                override fun onFailed(exception: Exception) {
                    dialog?.dismiss()
                    Log.e("mAppName", "onFailed() Filter", exception)
                }
            })
            .start()


    }

    fun hideShowFilters(visibility: Int) {
        filtersRecyclerView.visibility = visibility
    }

    private fun setUpListeners() {

        mListeners = ArrayList()
        mListeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Float, max: Float, scale: Float) {
                updateVideoProgress(time)
            }
        })

        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    onClickVideoPlayPause()
                    return true
                }
            })

        videoView.setOnErrorListener { _, what, _ ->
            mOnTrimVideoListener?.onError("Something went wrong reason : $what")
            false
        }

        videoView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }


        videoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onPlayerIndicatorSeekChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStart()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStop(seekBar)
            }
        })

        timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
//                videoSeekBar.visibility = View.GONE
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
                onStopSeekThumbs()
            }
        })

        videoView.setOnPreparedListener {
            onVideoPrepared(it)
            videoMediPlayer = it
        }

        videoView.setOnCompletionListener { onVideoCompleted() }
    }


    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        val duration = (mDuration * progress / 1000L)
        if (fromUser) {
            if (duration < mStartPosition) setProgressBarPosition(mStartPosition)
            else if (duration > mEndPosition) setProgressBarPosition(mEndPosition)
        }
    }

    private fun onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        videoView.pause()
        btnPlayPause.visibility = View.VISIBLE
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        videoView.pause()
        btnPlayPause.visibility = View.VISIBLE

        val duration = (mDuration * seekBar.progress / 1000L).toInt()
        videoView.seekTo(duration)
        notifyProgressUpdate(false)
    }

    private fun setProgressBarPosition(position: Float) {
        if (mDuration > 0) videoSeekBar.progress = (1000L * position / mDuration).toInt()
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            Thumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L)
                videoView.seekTo(mStartPosition.toInt())
                videosData.minPosition =
                    ((videosData.duration * value / 100L).toLong())

                println("Left Positio ${videosData.duration * value / 100L}")

            }
            Thumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L)
                videosData.maxPosition =
                    ((videosData.duration * value / 100L).toLong())

                println("Right Positio ${videosData.duration * value / 100L}")

            }
        }
        setTimeFrames()
        mTimeVideo = mEndPosition - mStartPosition
    }

    private fun onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        videoView.pause()
        btnPlayPause.visibility = View.VISIBLE
    }


    private fun onVideoPrepared(mp: MediaPlayer) {
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight

        videosData.videoWidth = videoWidth
        videosData.videoHeight = videoHeight


        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = layout_surface_view.width
        val screenHeight = layout_surface_view.height
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = videoView.layoutParams
        val editorLp = mPhotoEditorView.layoutParams
        val verticalGuidelineParam = verticalRulerGuideline.layoutParams
        val horizontalGuidelineParam = horizontalRulerGuideline.layoutParams
        val leftRulerParams = ivRulerLeft.layoutParams
        val topRulerParams = ivRulerTop.layoutParams

        leftRulerBitmap =
            BitmapFactory.decodeResource(context!!.resources, R.drawable.ruler_left)
        topRulerBitmap = BitmapFactory.decodeResource(context!!.resources, R.drawable.ruler_top)

        leftRulerBitmap?.width
        leftRulerBitmap?.height
        topRulerBitmap?.width
        topRulerBitmap?.height

        if (videoProportion > screenProportion) {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() / videoProportion).toInt()

            /*Params for editor width height according to video*/
            editorLp.width = screenWidth
            editorLp.height = (screenWidth.toFloat() / videoProportion).toInt()

            /*Params for ruler guideline*/
            horizontalGuidelineParam.width = screenWidth
            verticalGuidelineParam.height = (screenWidth.toFloat() / videoProportion).toInt()

            croppedLeftRulerBitmap = Bitmap.createBitmap(
                leftRulerBitmap!!,
                0,
                0,
                leftRulerBitmap?.width!!,
                lp.height
            )
            croppedTopRulerBitmap = Bitmap.createBitmap(
                topRulerBitmap!!,
                0,
                0,
                lp.width,
                topRulerBitmap?.height!!
            )

        } else {
            lp.width = (videoProportion * screenHeight.toFloat()).toInt()
            lp.height = screenHeight

            /*Params for editor width height according to video*/
            editorLp.width = (videoProportion * screenHeight.toFloat()).toInt()
            editorLp.height = screenHeight

            /*Params for ruler guideline*/
            horizontalGuidelineParam.width = (videoProportion * screenHeight.toFloat()).toInt()
            verticalGuidelineParam.height = screenHeight

            croppedLeftRulerBitmap = Bitmap.createBitmap(
                leftRulerBitmap!!,
                0,
                0,
                leftRulerBitmap?.width!!,
                lp.height
            )

            croppedTopRulerBitmap = Bitmap.createBitmap(
                topRulerBitmap!!,
                0,
                0,
                lp.width,
                topRulerBitmap?.height!!
            )

        }

        videoView.layoutParams = lp
        mPhotoEditorView.layoutParams = editorLp
        verticalRulerGuideline.layoutParams = verticalGuidelineParam
        horizontalRulerGuideline.layoutParams = horizontalGuidelineParam
        ivRulerLeft.layoutParams = leftRulerParams
        ivRulerTop.layoutParams = topRulerParams

        ivRulerTop.setImageBitmap(croppedTopRulerBitmap)
        ivRulerLeft.setImageBitmap(croppedLeftRulerBitmap)

        if (SharedPreference.getBoolean(context, Constants.HIDE_SHOW_RULER)) {
            ivRulerLeft.visibility = View.VISIBLE
            ivRulerTop.visibility = View.VISIBLE
        } else {
            ivRulerLeft.visibility = View.GONE
            ivRulerTop.visibility = View.GONE
        }

        btnPlayPause.visibility = View.VISIBLE

        mDuration = videoView.duration.toFloat()

        setMinDuration(2)
        setMaxDuration(60)

        setSeekBarPosition()
        setTimeFrames()
        mOnVideoListener?.onVideoPrepared()
    }


    private fun onVideoCompleted() {
        videoView.seekTo(mStartPosition.toInt())
    }

    class MessageHandler internal constructor(view: MergeFragment) : Handler() {
        private val mView: WeakReference<MergeFragment> = WeakReference(view)
        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view == null || view.videoView == null) return
            view.notifyProgressUpdate(true)
            if (view.videoView.isPlaying) sendEmptyMessageDelayed(0, 10)
        }
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0f) return
        val position = videoView.currentPosition
        if (all) {
            for (item in mListeners) {
                item.updateProgress(position.toFloat(), mDuration, (position * 100 / mDuration))
            }
        } else {
            mListeners[0].updateProgress(
                position.toFloat(),
                mDuration,
                (position * 100 / mDuration)
            )
        }
    }

    private fun setSeekBarPosition() {
        when {
            mDuration >= mMaxDuration && mMaxDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMaxDuration / 2
                mEndPosition = mDuration / 2 + mMaxDuration / 2

                timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))

                videosData.minPosition = (((mStartPosition * 100 / mDuration).toLong()))
                videosData.maxPosition = (((mEndPosition * 100 / mDuration)).toLong())
            }
            mDuration <= mMinDuration && mMinDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMinDuration / 2
                mEndPosition = mDuration / 2 + mMinDuration / 2

                timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))

                videosData.minPosition = (((mStartPosition * 100 / mDuration)).toLong())
                videosData.maxPosition = ((mEndPosition * 100 / mDuration).toLong())

            }
            else -> {
                timeLineBar.setThumbValue(0, 0f)
                timeLineBar.setThumbValue(1, (mDuration * 100) / mDuration)

                mStartPosition = 0f
                mEndPosition = mDuration

                videosData.minPosition = 0
                videosData.maxPosition = videosData.duration

            }
        }
        videoView.seekTo(mStartPosition.toInt())
        mTimeVideo = mDuration
        timeLineBar.initMaxWidth()
    }

    private fun setTimeFrames() {
//        val seconds = context.getString(com.video.trimmer.R.string.short_seconds)
//        textTimeSelection.text = String.format(
//            "%s %s - %s %s",
//            TrimVideoUtils.stringForTime(mStartPosition),
//            seconds,
//            TrimVideoUtils.stringForTime(mEndPosition),
//            seconds
//        )
    }

    companion object {
        private const val MIN_TIME_FRAME = 1000
        private const val SHOW_PROGRESS = 2
    }


    private fun updateVideoProgress(time: Float) {
        if (videoView == null) return
        if (time <= mStartPosition && time <= mEndPosition) videoSeekBar.visibility = View.GONE
        else videoSeekBar.visibility = View.VISIBLE
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            videoView.pause()
            btnPlayPause.visibility = View.VISIBLE
            mResetSeekBar = true
            return
        }
        setProgressBarPosition(time)
    }

    private fun onClickVideoPlayPause() {
        if (videoView.isPlaying) {
            btnPlayPause.visibility = View.VISIBLE
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            videoView.pause()
        } else {
            btnPlayPause.visibility = View.GONE
            if (mResetSeekBar) {
                mResetSeekBar = false
                videoView.seekTo(mStartPosition.toInt())
            }
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            videoView.start()
        }
    }

    fun hideShowRuler(visibility: Int) {
        ivRulerTop.visibility = visibility
        ivRulerLeft.visibility = visibility
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (this::videoView.isInitialized && this::timeLineBar.isInitialized) {
            videoView.setVideoURI(Uri.parse(videosData.croppedPath))
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::videoView.isInitialized && this::btnPlayPause.isInitialized && mMessageHandler != null) {
            btnPlayPause.visibility = View.VISIBLE
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            videoView.pause()
        }
    }

}