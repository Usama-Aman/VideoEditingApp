package com.elementary.youmerge.viewpager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.divyanshu.colorseekbar.ColorSeekBar
import com.elementary.youmerge.R
import com.elementary.youmerge.VideoSettingModel
import com.elementary.youmerge.trimmer.*
import com.elementary.youmerge.utils.AppUtils
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.SharedPreference
import com.elementary.youmerge.video_crop_prac.VideoCropActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.github.rubensousa.gravitysnaphelper.GravitySnapRecyclerView
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import ja.burhanrashid52.photoeditor.PhotoEditor
import java.io.File
import java.io.RandomAccessFile
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

class MergeViewPagerActivity : AppCompatActivity(), View.OnClickListener,
    PopupMenu.OnMenuItemClickListener {

    lateinit var directoryTrimmedVideos: File
    private lateinit var directoryMergeVideos: File

    var mainVideosList: ArrayList<TrimmerListModel> = ArrayList()
    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var viewPager: ViewPager

    private lateinit var currentVisibleFragment: MergeFragment

    private lateinit var ivEmoji: ImageView
    private lateinit var ivCloseEmoji: ImageView
    private lateinit var ivText: ImageView
    private lateinit var ivPencil: ImageView
    private lateinit var ivCrop: ImageView
    private lateinit var mergeMenuDots: ImageView
    private lateinit var ivUndoPencil: ImageView
    private lateinit var ivRemoveVideo: ImageView
    private lateinit var ivClearAll: ImageView
    private lateinit var ivAddImage: ImageView
    private lateinit var ivFilter: ImageView
    private lateinit var ivTextBackground: ImageView
    private lateinit var stickerEditText: EditText
    private lateinit var emojiLayout: ConstraintLayout
    private lateinit var colorSeekBar: ColorSeekBar
    private lateinit var btnTrimMergeSave: ImageView
    private lateinit var btnClosePencil: TextView
    private lateinit var emojiRecycler: RecyclerView
    private lateinit var widgetsLayout: ConstraintLayout
    private lateinit var progressBarLayout: ConstraintLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var btnProgressCancel: TextView
    private lateinit var progressNumber: TextView
    private lateinit var progressBarText2: TextView

    private var isTextSelected: MutableLiveData<Boolean> = MutableLiveData()
    private var isLineSelected: MutableLiveData<Boolean> = MutableLiveData()
    private var isTextBackgroundSelected: MutableLiveData<Boolean> = MutableLiveData()
    private var isFilterSelected: MutableLiveData<Boolean> = MutableLiveData()

    lateinit var trimmerListRecycler: GravitySnapRecyclerView
    lateinit var bottomRecycler: RecyclerView

    private lateinit var emojiAdapter: EmojiAdapter
    private lateinit var bottomListAdapter: BottomListAdapter

    private lateinit var clearAllAlertDialog: AlertDialog
    private lateinit var mergeVideosAlertDialog: AlertDialog

    var videoResolutionsList: MutableList<VideoSettingModel> = ArrayList()
    var bitRateList: MutableList<VideoSettingModel> = ArrayList()
    var frameRateList: MutableList<VideoSettingModel> = ArrayList()

    private var selectedColor: Int = 0
    private var selectedTextBackgroundColor: Int = 0
    private var trimCounts = 0
    private var stickerImagesCount = 0
    private var createImageCount = 0
    private var pictureAppended = 0
    private var finalMergedVideoPath = ""
    private var trimPath = ""

    private var checkForAudioCounts: Int = 0


    private var trimmedPaths: MutableList<String> = ArrayList()
    private var stickerImagePaths: MutableList<String> = ArrayList()

    var editedVideosList: ArrayList<TrimmerListModel> = ArrayList()

    val fragmentsList: MutableList<MergeFragment> = ArrayList()
    val positionsList: MutableList<Int> = ArrayList()

    val hideViewBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.action?.equals(Constants.HideShowViewBroadCastReceiver) == true) {
                    val isHideShowViews = intent.getBooleanExtra("hideShowViews", false)
                    if (this@MergeViewPagerActivity::widgetsLayout.isInitialized &&
                        this@MergeViewPagerActivity::bottomRecycler.isInitialized
                    ) {
                        if (isHideShowViews) {
                            widgetsLayout.visibility = View.VISIBLE
                            bottomRecycler.visibility = View.VISIBLE
                        } else {
                            widgetsLayout.visibility = View.GONE
                            bottomRecycler.visibility = View.GONE
                        }
                    }
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewpager_merge)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            hideViewBroadcastReceiver,
            IntentFilter(Constants.HideShowViewBroadCastReceiver)
        )
        mainVideosList = intent.getParcelableArrayListExtra("videosList")!!

        setTheDefault()
        initViews()
        initViewPager()
        setUpAdapter()
        setUpEmojiAdapter()
    }

    private fun initViews() {
        isTextBackgroundSelected.value = false
        isFilterSelected.value = false
        selectedColor = ContextCompat.getColor(this, R.color.red)
        selectedTextBackgroundColor = ContextCompat.getColor(this, R.color.transparent)


        directoryMergeVideos = File(
            getExternalFilesDir(null), "${Constants.APP_NAME}/${Constants.MERGER_VIDEOS_FOLDER}"
        )

        directoryTrimmedVideos = File(
            getExternalFilesDir(null), "${Constants.APP_NAME}/${Constants.TRIMMED_VIDEOS_FOLDER}"
        )

        viewPager = findViewById(R.id.viewPager)
        stickerEditText = findViewById(R.id.stickerEditText)
        emojiLayout = findViewById(R.id.emojiLayout)
        ivEmoji = findViewById(R.id.ivEmoji)
        ivCloseEmoji = findViewById(R.id.ivCloseEmoji)
        ivText = findViewById(R.id.ivText)
        ivPencil = findViewById(R.id.ivPencil)
        ivCrop = findViewById(R.id.ivCrop)
        colorSeekBar = findViewById(R.id.colorSeekBar)
        mergeMenuDots = findViewById(R.id.mergeMenuDots)
        btnTrimMergeSave = findViewById(R.id.btnTrimMergeSave)
        btnClosePencil = findViewById(R.id.btnClosePencil)
        ivRemoveVideo = findViewById(R.id.ivRemoveVideo)
        emojiRecycler = findViewById(R.id.emojiRecycler)
        widgetsLayout = findViewById(R.id.widgetsLayout)
        ivClearAll = findViewById(R.id.ivClearAll)
        progressBarLayout = findViewById(R.id.progressBarLayout)
        progressBar = findViewById(R.id.progressBar)
        btnProgressCancel = findViewById(R.id.btnProgressCancel)
        progressNumber = findViewById(R.id.progressNumber)
        ivUndoPencil = findViewById(R.id.ivUndoPencil)
        ivFilter = findViewById(R.id.ivFilter)
        ivAddImage = findViewById(R.id.ivAddImage)
        ivTextBackground = findViewById(R.id.ivTextBackground)
        progressBarText2 = findViewById(R.id.progressBarText2)

        ivEmoji.setOnClickListener(this)
        ivText.setOnClickListener(this)
        ivPencil.setOnClickListener(this)
        ivCrop.setOnClickListener(this)
        mergeMenuDots.setOnClickListener(this)
        ivCloseEmoji.setOnClickListener(this)
        btnTrimMergeSave.setOnClickListener(this)
        btnClosePencil.setOnClickListener(this)
        ivClearAll.setOnClickListener(this)
        ivRemoveVideo.setOnClickListener(this)
        btnProgressCancel.setOnClickListener(this)
        ivUndoPencil.setOnClickListener(this)
        ivFilter.setOnClickListener(this)
        ivAddImage.setOnClickListener(this)
        ivTextBackground.setOnClickListener(this)

        stickerEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
                stickerEditText.visibility = View.GONE
                ivText.requestFocus()
                isTextSelected.value = false
                isTextBackgroundSelected.value = false
                if (textView.text.toString().isNotBlank()) {
                    currentVisibleFragment.addTextSticker(
                        textView.text.toString(),
                        selectedColor,
                        selectedTextBackgroundColor
                    )
                }
                stickerEditText.setText("")

                true
            }
            false
        }

        colorSeekBar.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(color: Int) {
                if (isTextSelected.value == true) {
                    if (isTextBackgroundSelected.value == true) {
                        selectedTextBackgroundColor = color
                        stickerEditText.setBackgroundColor(color)
                    } else {
                        selectedColor = color
                        stickerEditText.setTextColor(color)
                    }
                }
                if (isLineSelected.value == true) {
                    selectedColor = color
                    currentVisibleFragment.setBrushColor(color)
                }
            }
        })

        observingForLiveDataVariable()
    }

    private fun initViewPager() {
        /*Create Fragments*/
        fragmentsList.clear()
        positionsList.clear()
        for (i in mainVideosList.indices) {
            val f = MergeFragment()
            val bundle = Bundle()
            bundle.putInt("position", i)
            bundle.putParcelable("videoData", mainVideosList[i])
            f.arguments = bundle
            fragmentsList.add(f)

            positionsList.add(i)
        }

        viewPagerAdapter =
            ViewPagerAdapter(mainVideosList, fragmentsList, positionsList, supportFragmentManager)
        viewPager.offscreenPageLimit = mainVideosList.size
        viewPager.adapter = viewPagerAdapter


        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (::currentVisibleFragment.isInitialized)
                    isFilterSelected.value = false
            }

            override fun onPageSelected(position: Int) {
                currentVisibleFragment = fragmentsList[position]

                bottomListAdapter.selectedPosition = position
                bottomListAdapter.notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        Handler(Looper.getMainLooper()).postDelayed({
            viewPager.setCurrentItem(0, true)
            currentVisibleFragment =
                viewPager.adapter?.instantiateItem(
                    viewPager,
                    viewPager.currentItem
                ) as MergeFragment
        }, 100)

    }

    private fun observingForLiveDataVariable() {
        isTextSelected.observe(this, Observer {
            if (it) {
                colorSeekBar.visibility = View.VISIBLE
                stickerEditText.visibility = View.VISIBLE
            } else {
                colorSeekBar.visibility = View.GONE
                stickerEditText.visibility = View.GONE
            }
        })

        isLineSelected.observe(this, Observer {
            if (it) {
                btnClosePencil.visibility = View.VISIBLE
                colorSeekBar.visibility = View.VISIBLE

                widgetsLayout.visibility = View.GONE
                bottomRecycler.visibility = View.GONE

                currentVisibleFragment.enableBrush(selectedColor)

                viewPager.isEnabled = false
                viewPager.isClickable = false
                viewPager.isFocusable = false

            } else {
                btnTrimMergeSave.visibility = View.VISIBLE
                btnClosePencil.visibility = View.GONE
                colorSeekBar.visibility = View.GONE

                widgetsLayout.visibility = View.VISIBLE
                bottomRecycler.visibility = View.VISIBLE

                currentVisibleFragment.disableBrush()
                viewPager.isEnabled = true
                viewPager.isClickable = true
                viewPager.isFocusable = true

            }
        })

        isTextBackgroundSelected.observe(this, {
            if (it) {
                ivTextBackground.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_text_color_selected /*Selected*/
                    )
                )
            } else {
                ivTextBackground.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_text_color /*unselected*/
                    )
                )
            }
        })

        isFilterSelected.observe(this, {
            if (it) {
                if (bottomRecycler.isVisible) {
                    bottomRecycler.visibility = View.GONE
                    currentVisibleFragment.hideShowFilters(View.VISIBLE)
                } else {
                    bottomRecycler.visibility = View.VISIBLE
                    if (this::currentVisibleFragment.isInitialized)
                        currentVisibleFragment.hideShowFilters(View.GONE)
                }
            } else {
                bottomRecycler.visibility = View.VISIBLE
                if (this::currentVisibleFragment.isInitialized)
                    currentVisibleFragment.hideShowFilters(View.GONE)
            }
        })
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

    private fun setUpAdapter() {
        /*BOTTOM*/
        bottomRecycler = findViewById(R.id.bottomRecycler)
        bottomRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bottomListAdapter = BottomListAdapter(mainVideosList, this)

        val itemTouchHelperCallBack: ItemTouchHelper.Callback = ItemMoveCallback(bottomListAdapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallBack)
        itemTouchHelper.attachToRecyclerView(bottomRecycler)

        bottomRecycler.adapter = bottomListAdapter
        bottomListAdapter.selectedPosition = 0
    }

    private fun setUpEmojiAdapter() {
        emojiAdapter = EmojiAdapter(this)

        val orientation: Int = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            emojiRecycler.layoutManager = GridLayoutManager(this, 5)
        else
            emojiRecycler.layoutManager = GridLayoutManager(this, 7)

        emojiRecycler.setHasFixedSize(true)
        emojiRecycler.adapter = emojiAdapter
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onClick(v: View?) {
        AppUtils.preventTwoClick(v!!)
        when (v.id) {

            R.id.ivEmoji -> {
                isTextSelected.value = false
                isLineSelected.value = false
                isTextBackgroundSelected.value = false
                isFilterSelected.value = false

                mergeMenuDots.visibility = View.GONE
                emojiLayout.visibility = View.VISIBLE
            }
            R.id.ivCloseEmoji -> {
                emojiLayout.visibility = View.GONE
                mergeMenuDots.visibility = View.VISIBLE
            }
            R.id.ivText -> {
                isLineSelected.value = false
                isTextSelected.value = true
                isTextBackgroundSelected.value = false
                isFilterSelected.value = false
                stickerEditText.requestFocus()
                stickerEditText.setTextColor(selectedColor)
                val inputMethodManager: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInputFromWindow(
                    stickerEditText.applicationWindowToken,
                    InputMethodManager.SHOW_FORCED, 0
                )
            }
            R.id.ivPencil -> {
                isTextSelected.value = false
                isTextBackgroundSelected.value = false
                isLineSelected.value = true
                btnTrimMergeSave.visibility = View.GONE
                isFilterSelected.value = false

            }
            R.id.mergeMenuDots -> {

                val popupMenu = PopupMenu(this, v)
                popupMenu.menuInflater.inflate(R.menu.menu_merge_screen, popupMenu.menu)
                val menuItem = popupMenu.menu.findItem(R.id.menuHideShowRuler)
                menuItem?.title =
                    if (SharedPreference.getBoolean(this, Constants.HIDE_SHOW_RULER))
                        "Hide Ruler"
                    else
                        "Show Ruler"

                popupMenu.setOnMenuItemClickListener(this)
                popupMenu.show()

                try {
                    popupMenu.setForceShowIcon(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
            R.id.btnClosePencil -> {
                isTextSelected.value = false
                isLineSelected.value = false
                isFilterSelected.value = false
                btnClosePencil.visibility = View.GONE
                btnTrimMergeSave.visibility = View.VISIBLE
            }
            R.id.ivClearAll -> {
                showClearAllDialog()
            }
            R.id.btnTrimMergeSave -> {
                showMergeDialog()
            }
            R.id.btnProgressCancel -> {
                progressNumber.text = "0%"
                progressBarLayout.visibility = View.GONE
                FFmpeg.cancel()
            }
            R.id.ivRemoveVideo -> {
                if (mainVideosList.size == 2) {
                    Toast.makeText(
                        this,
                        "Minimum 2 videos are required for merging",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }
                isTextSelected.value = false
                isLineSelected.value = false

                val deletePosition = viewPager.currentItem

                mainVideosList.removeAt(deletePosition)
                viewPagerAdapter.delPage(deletePosition)
                bottomListAdapter.notifyDataSetChanged()

                Handler(Looper.getMainLooper()).postDelayed({
                    currentVisibleFragment = fragmentsList[viewPager.currentItem]
                }, 100)

            }
            R.id.ivUndoPencil -> {

                if (isTextSelected.value == true) {
                    stickerEditText.setText("")
                    stickerEditText.requestFocus()
                    AppUtils.hideKeyboard(this)
                    isTextSelected.value = false
                } else
                    currentVisibleFragment.undoEditing()
            }
            R.id.ivFilter -> {
                isTextSelected.value = false
                isLineSelected.value = false
                isTextBackgroundSelected.value = false
                isFilterSelected.value = !isFilterSelected.value!!
            }
            R.id.ivCrop -> {
                isTextSelected.value = false
                isLineSelected.value = false
                isTextBackgroundSelected.value = false
                isFilterSelected.value = false

                val intent = Intent(this, VideoCropActivity::class.java)
                intent.putExtra("filePath", currentVisibleFragment.videosData.originalPath)
                startActivityForResult(intent, Constants.CROP_VIDEO_REQUEST_CODE)
            }
            R.id.ivAddImage -> {
                isTextSelected.value = false
                isLineSelected.value = false
                isTextBackgroundSelected.value = false
                isFilterSelected.value = false

                val options = Options.init()
                options.requestCode = Constants.IMPORT_IMAGE_REQUEST_CODE
                options.count = 1
                options.isExcludeVideos = true
                options.isFrontfacing = false
                options.screenOrientation = Options.SCREEN_ORIENTATION_USER
                options.path = "/youmerge/images"

                Pix.start(this, options)
            }
            R.id.ivTextBackground -> {
                if (isTextSelected.value == true)
                    isTextBackgroundSelected.value = !isTextBackgroundSelected.value!!
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.CROP_VIDEO_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    currentVisibleFragment.videosData.croppedPath =
                        data?.getStringExtra("croppedFilePath")!!
                    currentVisibleFragment.initializeVideoView()
                }
            }
            Constants.IMPORT_IMAGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue = data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (!returnValue.isNullOrEmpty()) {
                        val bmOptions = BitmapFactory.Options()
                        var bitmap = BitmapFactory.decodeFile(returnValue[0], bmOptions)
                        bitmap = Bitmap.createScaledBitmap(
                            bitmap!!,
                            100,
                            100,
                            true
                        )
                        currentVisibleFragment.addImageSticker(/*bitmap*/ returnValue[0])
                    }
                }
            }
        }
    }

    private fun showClearAllDialog() {
        clearAllAlertDialog = AlertDialog.Builder(this)
            .setMessage("Are you sure you want to clear all formatting?")
            .setPositiveButton("Yes") { _, _ ->
                if (isLineSelected.value == true) {
                    btnClosePencil.visibility = View.GONE
                    btnTrimMergeSave.visibility = View.VISIBLE
                }
                isLineSelected.value = false
                isTextSelected.value = false

                currentVisibleFragment.clearPhotoEditor()
            }
            .setNegativeButton("No") { _, _ ->
                clearAllAlertDialog.dismiss()
            }
            .show()
    }

    private fun showMergeDialog() {
        mergeVideosAlertDialog = AlertDialog.Builder(this)
            .setMessage("Are you sure you want to merge the selected videos?")
            .setPositiveButton("Yes") { _, _ ->

                isTextSelected.value = false
                isLineSelected.value = false
                stickerImagePaths.clear()
                createImageCount = 0
                progressBar.progress = 0
                progressNumber.text = "0%"
                progressBarLayout.visibility = View.VISIBLE

                editedVideosList.clear()

                for (i in positionsList.indices) {
                    val fragVideoModel = fragmentsList[i].videosData

                    editedVideosList.add(
                        TrimmerListModel(
                            fragVideoModel.croppedPath,
                            fragVideoModel.originalPath,
                            fragVideoModel.duration,
                            fragVideoModel.minPosition,
                            fragVideoModel.maxPosition,
                            fragVideoModel.trimmedPath,
                            fragVideoModel.drawView,
                            fragVideoModel.imagesPaths,
                            fragVideoModel.photoEditor,
                            fragVideoModel.videoWidth,
                            fragVideoModel.videoHeight,
                        )
                    )
                }
                createImages()

            }
            .setNegativeButton("No") { _, _ ->
                mergeVideosAlertDialog.dismiss()
            }
            .show()
    }

    fun createEmojiSticker(string: String, isEmoji: Boolean) {
        if (isEmoji) {
            ivCloseEmoji.callOnClick()
        }
        currentVisibleFragment.addEmojiSticker(string)
    }

    private fun createImages() {
        try {
            if (createImageCount <= positionsList.size - 1) {

                if (editedVideosList[createImageCount].imagesPaths == null) {
                    editedVideosList[createImageCount].imagesPaths = ArrayList()
                    editedVideosList[createImageCount].imagesPaths?.clear()
                }

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (editedVideosList[createImageCount].photoEditor?.isCacheEmpty == false
                ) {
                    val pp = "$directoryTrimmedVideos/${System.currentTimeMillis()}.png"
                    editedVideosList[createImageCount].imagesPaths?.add(pp)

                    editedVideosList[createImageCount].photoEditor?.saveAsFile(pp,
                        object : PhotoEditor.OnSaveListener {
                            override fun onSuccess(imagePath: String) {
                                progressBar.progress =
                                    progressBar.progress + 30 / editedVideosList.size
                                progressNumber.text = "${progressBar.progress}%"
                                Log.e("File Saved", "$createImageCount")

                                createImageCount++
                                createImages()
                            }

                            override fun onFailure(exception: java.lang.Exception) {
                                Log.e("File Not Saved", "$createImageCount")
                            }

                        })
                } else {
                    progressBar.progress = progressBar.progress + 30 / editedVideosList.size
                    progressNumber.text = "${progressBar.progress}%"
                    Log.e("No File Created", "$createImageCount")

                    createImageCount++
                    createImages()
                }

            } else {


                trimPath = ""
                trimmedPaths.clear()
                trimCounts = 0

                trimPath = "${directoryTrimmedVideos}/${System.currentTimeMillis()}.mp4"

                executeCutVideoCommand(
                    editedVideosList[trimCounts].minPosition.toInt() * 1000,
                    editedVideosList[trimCounts].maxPosition.toInt() * 1000,
                    editedVideosList[trimCounts].croppedPath,
                    trimPath
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun executeCutVideoCommand(
        startMs: Int,
        endMs: Int,
        originalPath: String,
        trimmedPath: String
    ) {

        val complexCommand = arrayOf(
            "-ss",
            "" + startMs / 1000,
            "-y",
            "-i",
            originalPath,
            "-t",
            "" + (endMs - startMs) / 1000,
            "-r",
            "50",
            trimmedPath
        )

        val ff = FFmpeg.executeAsync(
            complexCommand
        ) { executionId, returnCode ->

            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Async command execution completed successfully.");

                    if (editedVideosList[trimCounts].imagesPaths?.size == 0) {
                        trimmedPaths.add(trimmedPath)

                        if (trimCounts == (editedVideosList.size - 1)) {
                            progressBar.progress = progressBar.progress + 40 / editedVideosList.size
                            progressNumber.text = "${progressBar.progress}%"

//                            mergeTrimmedVideos()
//                            merge()

                            /*Check for audio and add null audio*/
                            checkForAudioCounts = 0
                            checkForAudioAndAddAudio()

                        } else {

                            progressBar.progress = progressBar.progress + 40 / editedVideosList.size
                            progressNumber.text = "${progressBar.progress}%"

                            trimCounts++
                            trimPath = "${directoryTrimmedVideos}/${System.currentTimeMillis()}.mp4"

                            executeCutVideoCommand(
                                editedVideosList[trimCounts].minPosition.toInt() * 1000,
                                editedVideosList[trimCounts].maxPosition.toInt() * 1000,
                                editedVideosList[trimCounts].croppedPath,
                                trimPath
                            )
                        }

                    } else
                        addSticker(trimmedPath, trimCounts)

                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                }
                else -> {
                    progressBarText2.text = String.format(
                        "Trim command execution failed with returnCode=%d.",
                        returnCode
                    )

                    Log.i(
                        Config.TAG,
                        String.format(
                            "Trimming execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun addSticker(trimmedVideoPath: String, counts: Int) {
        val stickerCommand: MutableList<String> = ArrayList()
        stickerCommand.add("-i")
        stickerCommand.add(trimmedVideoPath)
        stickerCommand.add("-i")
        for (i in editedVideosList[counts].imagesPaths?.indices!!) {
            stickerCommand.add(editedVideosList[counts].imagesPaths?.get(i)!!)
            if (i < editedVideosList[counts].imagesPaths?.size!! - 1)
                stickerCommand.add("-i")
        }
        stickerCommand.add("-filter_complex")
//        if (editedVideosList[counts].imagesPaths?.size!! == 1)
//        stickerCommand.add("overlay=0:0")
        /*(main_w-overlay_w)/2:(main_h-overlay_h)/2:shortest=1*/
//        else
//            stickerCommand.add("overlay=0.0,overlay=0.0")

        if (editedVideosList[counts].videoWidth % 2 != 0) {
            editedVideosList[counts].videoWidth =
                ceil(((editedVideosList[counts].videoWidth / 2) * 2).toDouble()).toInt()
        }

        if (editedVideosList[counts].videoHeight % 2 != 0) {
            editedVideosList[counts].videoHeight =
                ceil(((editedVideosList[counts].videoHeight / 2) * 2).toDouble()).toInt()
        }


        stickerCommand.add("[0:v]scale=${editedVideosList[counts].videoWidth}:${editedVideosList[counts].videoHeight}[v0];[1:v]scale=${editedVideosList[counts].videoWidth}:${editedVideosList[counts].videoHeight}[v1];[v0][v1]overlay")
        stickerCommand.add("-r")
        stickerCommand.add("50")

        val p = "${directoryTrimmedVideos}/${System.currentTimeMillis()}.mp4"
        trimmedPaths.add(p)

        stickerCommand.add(p)

        val i = FFmpeg.executeAsync(
            stickerCommand.toTypedArray()
        ) { _, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    Log.i(
                        Config.TAG,
                        "Async command execution completed successfully."
                    )


                    if (trimCounts == (editedVideosList.size - 1)) {
                        progressBar.progress = progressBar.progress + 40 / editedVideosList.size
                        progressNumber.text = "${progressBar.progress}%"

//                        mergeTrimmedVideos()
//                        merge()

                        /*Check for audio and add null audio*/
                        checkForAudioCounts = 0
                        checkForAudioAndAddAudio()

                    } else {
                        progressBar.progress = progressBar.progress + 40 / editedVideosList.size
                        progressNumber.text = "${progressBar.progress}%"

                        trimCounts++
                        trimPath = "${directoryTrimmedVideos}/${System.currentTimeMillis()}.mp4"

                        executeCutVideoCommand(
                            editedVideosList[trimCounts].minPosition.toInt() * 1000,
                            editedVideosList[trimCounts].maxPosition.toInt() * 1000,
                            editedVideosList[trimCounts].croppedPath,
                            trimPath
                        )
                    }

                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                }
                else -> {
                    progressBarText2.text = String.format(
                        "Sticker execution failed with returnCode=%d.",
                        returnCode
                    )
                    Log.i(
                        Config.TAG,
                        String.format(
                            "Sticker command execution failed with returnCode=%d.",
                            returnCode
                        )
                    );
                }
            }
        }
    }

    private fun checkForAudioAndAddAudio() {

        if (checkForAudioCounts == trimmedPaths.size)
            mergeTrimmedVideos()
        else
            addAudio()

    }

    private fun addAudio() {
        val nullAudioFilePath = "${directoryTrimmedVideos}/${System.currentTimeMillis()}.mp4"

        val audioCommand = arrayOf(
            "-y",
            "-i",
            trimmedPaths[checkForAudioCounts],
            "-f",
            "lavfi",
            "-i",
            "anullsrc=channel_layout=stereo:sample_rate=44100",
            "-c:v",
            "copy",
            "-shortest",
            nullAudioFilePath
        )

        val addAudioExecution =
            FFmpeg.executeAsync(audioCommand, ExecuteCallback { executionId, returnCode ->

                when (returnCode) {
                    Config.RETURN_CODE_SUCCESS -> {
                        Log.i(
                            Config.TAG,
                            "Audio Added command execution completed successfully."
                        )

                        trimmedPaths[checkForAudioCounts] = nullAudioFilePath
                        checkForAudioCounts++
                        checkForAudioAndAddAudio()
                    }
                    Config.RETURN_CODE_CANCEL -> {
                        Log.i(Config.TAG, "Async command execution cancelled by user.");
                    }
                    else -> {
                        progressBarText2.text = String.format(
                            "Audio execution failed with returnCode=%d.",
                            returnCode
                        )
                        Log.i(
                            Config.TAG,
                            String.format(
                                "Audio command execution failed with returnCode=%d.",
                                returnCode
                            )
                        )
                    }
                }

            })

    }

    private fun mergeTrimmedVideos() {
        finalMergedVideoPath = "$directoryMergeVideos/${System.currentTimeMillis()}.mp4"
        val outFile = File(finalMergedVideoPath)

//        var highestVideoWidth: Int = 3840000
//        var highestVideoHeight: Int = 2160000
        var highestVideoWidth: Int = 0
        var highestVideoHeight: Int = 0

        val mlisst: MutableList<String> = ArrayList()
        mlisst.clear()

        mlisst.add("-i")
        for (i in trimmedPaths.indices) {

            if (highestVideoWidth < editedVideosList[i].videoWidth && highestVideoHeight < editedVideosList[i].videoHeight) {
                highestVideoWidth = editedVideosList[i].videoWidth
                highestVideoHeight = editedVideosList[i].videoHeight
            }

            mlisst.add(trimmedPaths[i])
            if (i < trimmedPaths.size - 1)
                mlisst.add("-i")
        }
        mlisst.add("-filter_complex")
        val fc = StringBuilder()

        /*It is crashing*/
//        for (i in trimmedPaths.indices) {
//
//            if (editedVideosList[i].videoWidth == highestVideoWidth && editedVideosList[i].videoHeight == highestVideoHeight){
//                fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight},setsar=1:1[v$i];")
//            }else{
//                fc.append("[$i:v]scale=iw*min($highestVideoWidth/iw,${highestVideoHeight}/ih):ih*min(${highestVideoWidth}/iw,${highestVideoHeight}/ih),")
//                fc.append("pad=${highestVideoWidth}:${highestVideoHeight}")
//                fc.append(":(${highestVideoWidth}-iw*min(${highestVideoWidth}/iw,${highestVideoHeight}/ih))/2")
//                fc.append(":(${highestVideoHeight}-ih*min(${highestVideoWidth}/iw,${highestVideoHeight}/ih))/2,setsar1:1[v$i];")
//            }
//        }

        /*it shows the bottom green line video*/
//        for (i in trimmedPaths.indices) {
//            fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}:force_original_aspect_ratio=decrease,setsar=1[v$i];")
//            fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}:force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2:(ow-iw)/2:(oh-ih)/2:color=black[v$i];")
//            fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}:force_original_aspect_ratio=increase,pad=max(iw\\,ih):ow:(ow-iw)/2:(oh-ih)/2[v$i];")
//            if (highestVideoWidth <= editedVideosList[i].videoWidth && highestVideoHeight <= editedVideosList[i].videoHeight)
//                fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}")
//            else
//                fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}:force_original_aspect_ratio=decrease,pad=max(iw\\,ih):ow:(ow-iw)/2:(oh-ih)/2[v$i];")
//                fc.append("[$i:v]pad=${highestVideoWidth}:${highestVideoHeight}:(ow-iw)/2:(oh-ih)/2[v$i];")
//                fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}:force_original_aspect_ratio=increase,pad=${highestVideoWidth}:${highestVideoHeight}:ow:(ow-iw)/2:(oh-ih)/2[v$i];")
//            if (highestVideoWidth <= editedVideosList[i].videoWidth && highestVideoHeight <= editedVideosList[i].videoHeight)
//                fc.append("[$i:v]scale=${editedVideosList[i].videoWidth}x${editedVideosList[i].videoHeight}:force_original_aspect_ratio=increase,pad=${editedVideosList[i].videoWidth}:${editedVideosList[i].videoHeight}:-1:-1:color=black[v$i];")
//            else
//            fc.append("[$i:v]scale=${highestVideoWidth}x${highestVideoHeight}:force_original_aspect_ratio=increase,pad=${highestVideoWidth}:${highestVideoHeight}:-1:-1:color=black[v$i];")
//        }

        /*-------------------------------*/

        var maxHeight = 0
        var maxWidth = 0

        for (i in editedVideosList.indices) {
            if (maxHeight < editedVideosList[i].videoHeight)
                maxHeight = editedVideosList[i].videoHeight

            if (maxWidth < editedVideosList[i].videoWidth)
                maxWidth = editedVideosList[i].videoWidth
        }
        for (i in editedVideosList.indices) {
            fc.append("[$i:v]scale=min(${maxWidth}\\,iw):min(${maxHeight}\\,ih):force_original_aspect_ratio=increase,pad=${maxWidth}:${maxHeight}:-1:-1:color=black[v$i];")
//            fc.append("[$i:v]scale=min(${maxWidth}\\,iw):min(${maxHeight}\\,ih):force_original_aspect_ratio=increase,pad=min(${maxWidth}\\,iw):min(${maxHeight}\\,ih):-1:-1:color=red[v$i];")

        }


        for (i in trimmedPaths.indices) {
            fc.append("[v$i][$i:a]")
        }
        fc.append("concat=unsafe=1:n=${trimmedPaths.size}:v=1:a=1")

//        ---------------------------------------
//        for (i in trimmedPaths.indices)
//            fc.append("[$i:v:0][$i:a:0]")
//        fc.append("concat=unsafe=1:n=${trimmedPaths.size}:v=1:a=1 [outv][outa]")
//        ---------------------------------------


        mlisst.add(fc.toString())
//        mlisst.add("-s")
//        mlisst.add("1920x1080")
//        mlisst.add("${editedVideosList[0].videoWidth}x${editedVideosList[0].videoHeight}")
//        mlisst.add("${highestVideoWidth}x${highestVideoHeight}") /*For now highest means lowest as sir yasir said*/
//        ---------------
//        mlisst.add("-map")
//        mlisst.add("[outv]")
//        mlisst.add("-map")
//        mlisst.add("[outa]")
//        ---------------
        mlisst.add("-r")
        mlisst.add("50")
//        mlisst.add("-b:v")
//        mlisst.add(
//            (bitRateList[SharedPreference.getInt(
//                this, Constants.BIT_RATE,
//                1
//            )].value * 1024 * 1000).toString()
//        )
        mlisst.add("-preset")
        mlisst.add("ultrafast")
        mlisst.add(finalMergedVideoPath)


        //        complexCommand = arrayOf(
//            "-y",
//            "-i",
//            file1.toString(),
//            "-i",
//            file2.toString(),
//            "-strict",
//            "experimental",
//            "-filter_complex",
//            "[0:v]scale=1920x1080,setsar=1:1[v0];
//            [1:v] scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2,setsar=1:1[v1]
//            ;[v0][0:a][v1][1:a] concat=n=2:v=1:a=1",
//            "-ab",
//            "48000",
//            "-ac",
//            "2",
//            "-ar",
//            "22050",
//            "-s",
//            "1920x1080",
//            "-vcodec",
//            "libx264",
//            "-crf",
//            "27",
//            "-q",
//            "4",
//            "-preset",
//            "ultrafast",
//            rootPath.toString() + "/output.mp4"
//        )

//        val complexCommand = arrayOf(
//            "-y",
//            "-i",
//            "/mnt/m_external_sd/Videos/VID-20161221-WA0000.mp4",
//            "-i",
//            "/mnt/m_external_sd/Videos/Brodha V - Aathma Raama [Music Video]_HD.mp4",
//            "-i",
//            "/mnt/m_external_sd/DCIM/Sinha's POP/20150530_073113.mp4",
//            "-filter_complex",
//            "[0:v]scale=480x640,setsar=1[v0];" +
//                    "[1:v]scale=480x640,setsar=1[v1];" +
//                    "[2:v]scale=480x640,setsar=1[v2];" +
//                    "[v0][0:a][v1][1:a][v2][2:a]concat=n=3:v=1:a=1",
//            "-ab",
//            "48000",
//            "-ac",
//            "2",
//            "-ar",
//            "22050",
//            "-s",
//            "480x640",
//            "-vcodec",
//            "libx264",
//            "-crf",
//            "27",
//            "-preset",
//            "ultrafast",
//            savingPath
//        )

        val ff =
            FFmpeg.executeAsync(mlisst.toTypedArray()) { executionId, returnCode ->

                when (returnCode) {
                    Config.RETURN_CODE_SUCCESS -> {
                        Log.i(Config.TAG, "Async command execution completed successfully.");

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                this@MergeViewPagerActivity,
                                "Videos Merged Successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        val files = directoryTrimmedVideos.listFiles()!!
                        for (i in files.indices) {
                            val fdelete: File = File(files[i].path)
                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    println("file Deleted :")
                                } else {
                                    println("file not Deleted :")
                                }
                            }
                        }

                        progressBar.progress = 100
                        progressNumber.text = "100%"

                        setResult(Activity.RESULT_OK)
                        finish()

                    }
                    Config.RETURN_CODE_CANCEL -> {
                        Log.i(Config.TAG, "Async command execution cancelled by user.");
                    }
                    else -> {
                        progressBarText2.text = String.format(
                            "Merge execution failed with returnCode=%d.",
                            returnCode
                        )

                        Log.i(
                            Config.TAG,
                            String.format(
                                "Merge command execution failed with returnCode=%d.",
                                returnCode
                            )
                        )
                    }
                }


            }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()

        finish()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuCancel -> {
                onBackPressed()
            }
            R.id.menuClearFormatting -> {
                showClearAllDialog()
            }
            R.id.menuDeleteVideo -> {
                ivRemoveVideo.callOnClick()
            }
            R.id.menuMergeVideos -> {
                showMergeDialog()
            }
            R.id.menuHideShowRuler -> {
                SharedPreference.saveBoolean(
                    this,
                    Constants.HIDE_SHOW_RULER,
                    !SharedPreference.getBoolean(this, Constants.HIDE_SHOW_RULER)
                )

                if (SharedPreference.getBoolean(this, Constants.HIDE_SHOW_RULER)) {
                    for (i in fragmentsList.indices) {
                        fragmentsList[i].hideShowRuler(View.VISIBLE)
                    }
                } else {
                    for (i in fragmentsList.indices) {
                        fragmentsList[i].hideShowRuler(View.GONE)
                    }
                }


            }
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            emojiRecycler.layoutManager = GridLayoutManager(this, 5)
        else
            emojiRecycler.layoutManager = GridLayoutManager(this, 7)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hideViewBroadcastReceiver)
    }

}