package com.elementary.youmerge.all_videos

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.elementary.youmerge.ApplicationClass
import com.elementary.youmerge.MainActivity
import com.elementary.youmerge.R
import com.elementary.youmerge.databasase.AppDatabase
import com.elementary.youmerge.databasase.ImportFilesModel
import com.elementary.youmerge.trimmer.TrimmerListModel
import com.elementary.youmerge.utils.AppUtils
import com.elementary.youmerge.utils.Constants
import com.elementary.youmerge.utils.Constants.APP_NAME
import com.elementary.youmerge.utils.Constants.VIDEOS_FOLDER
import com.elementary.youmerge.utils.SharedPreference
import com.elementary.youmerge.viewpager.MergeViewPagerActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception


class AllVideosFragment : Fragment() {

    private lateinit var v: View
    private var filesList: MutableList<FileListModel> = ArrayList()
    var checkedVideos: MutableList<FileListModel> = ArrayList()
    private lateinit var videosRecyclerView: RecyclerView
    private lateinit var allVideosAdapter: AllVideosAdapter

    private lateinit var directoryVideos: File
    private lateinit var directoryMergeVideos: File

    private lateinit var btnMergeVideos: TextView
    private lateinit var btnDeleteVideos: TextView
    private lateinit var btnSelect: TextView
    private lateinit var btnCancel: TextView
    private lateinit var btnImport: TextView
    private lateinit var noVideoText: TextView

    var trimList: ArrayList<TrimmerListModel> = ArrayList()
    var isSelectEnabled = false
    lateinit var alertDialog: AlertDialog

    private var database: AppDatabase? = null

    companion object {
        private const val MERGE_REQUEST_CODE = 3001
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.all_videos_fragment, container, false)
        database = AppDatabase.getAppDataBase(context!!)

        initViews()
        setUpRecycler()
        getAllFiles()

        return v
    }

    private fun initViews() {
        checkedVideos.clear()
        btnMergeVideos = v.findViewById(R.id.mergeVideos)
        btnDeleteVideos = v.findViewById(R.id.deleteVideos)
        btnCancel = v.findViewById(R.id.cancel)
        btnSelect = v.findViewById(R.id.select)
        btnImport = v.findViewById(R.id.btnImport)
        noVideoText = v.findViewById(R.id.noVideoText)

        btnDeleteVideos.setOnClickListener {
            AppUtils.preventTwoClick(btnDeleteVideos)
            showDeleteDialog()
        }

        btnMergeVideos.setOnClickListener {
            AppUtils.preventTwoClick(btnMergeVideos)
            goToTrimActivity()
        }

        btnSelect.setOnClickListener {
            AppUtils.preventTwoClick(btnSelect)
            btnSelect.visibility = View.GONE
            btnImport.visibility = View.GONE

            btnMergeVideos.visibility = View.VISIBLE
            btnDeleteVideos.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE

            isSelectEnabled = true
        }

        btnCancel.setOnClickListener {
            checkedVideos.clear()
            AppUtils.preventTwoClick(btnSelect)
            btnMergeVideos.visibility = View.GONE
            btnDeleteVideos.visibility = View.GONE
            btnCancel.visibility = View.GONE

            btnSelect.visibility = View.VISIBLE
            btnImport.visibility = View.VISIBLE
            isSelectEnabled = false

            for (i in filesList.indices)
                filesList[i].isChecked = false

            allVideosAdapter.notifyDataSetChanged()
        }

        btnImport.setOnClickListener {

            val options = Options.init()
            options.requestCode = Constants.IMPORT_VIDEOS_REQUEST_CODE
//            if (!SharedPreference.getBoolean(context!!, Constants.IS_PRO_USER))
//                options.count = 10
            options.isExcludeVideos = false
            options.isFrontfacing = false
            options.screenOrientation = Options.SCREEN_ORIENTATION_PORTRAIT
            options.path = "/youmerge/images"

            Pix.start(this, options)
        }
    }

    private fun showDeleteDialog() {
        if (checkedVideos.size == 0) {
            AppUtils.showToast(activity, "Please select at least one video to delete", false)
            return
        }

        alertDialog = AlertDialog.Builder(context)
            .setMessage("Are you sure you want to delete the selected videos?")
            .setPositiveButton("Yes") { _, _ ->

                for (i in filesList.indices)
                    if (filesList[i].isChecked) {
                        if (filesList[i].fromImport) {
                            CoroutineScope(IO).launch {
                                database?.screenRecordingDAO()?.deletePath(filesList[i].id)
                            }
                        } else {
                            val fdelete: File = File(filesList[i].path)
                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    println("file Deleted :")
                                } else {
                                    println("file not Deleted :")
                                }
                            }
                        }
                    }

                AppUtils.showToast(activity, "Selected files are deleted", true)

                filesList.clear()
                getAllFiles()
                alertDialog.dismiss()

                btnCancel.callOnClick()
            }
            .setNegativeButton("No") { _, _ ->
                alertDialog.dismiss()
            }
            .show()
    }

    private fun getAllFiles() {
        directoryVideos = File(
            context!!.getExternalFilesDir(null), "${APP_NAME}/${VIDEOS_FOLDER}"
        )

        directoryMergeVideos = File(
            context!!.getExternalFilesDir(null), "$APP_NAME/${Constants.MERGER_VIDEOS_FOLDER}"
        )

        filesList.clear()
        AppUtils.showDialog(context!!)
        CoroutineScope(IO).launch {

            try {
                val files = directoryVideos.listFiles()!!
                database?.screenRecordingDAO()?.fetchAllPaths()?.forEach {
                    filesList.add(
                        FileListModel(
                            it.path,
                            it.duration,
                            it.isChecked,
                            it.name,
                            it.lastModified,
                            true,
                            it.id
                        )
                    )
                }

                if (!files.isNullOrEmpty())
                    for (i in files.indices) {
                        if (files[i].length() > 100000)
                            filesList.add(
                                FileListModel(
                                    (files[i].path),
                                    AppUtils.getVideoDuration(files[i].path, context!!),
                                    false,
                                    files[i].name, files[i].lastModified(), false, 0
                                )
                            )
                        Log.d("Files", "${files[i].name} - ${files[i].length()}")
                    }

                /*Sorting the list*/
                if (filesList.size > 1) {
                    filesList.sortByDescending { it.lastModified }
                }

                withContext(Main) {
                    AppUtils.dismissDialog()

                    if (filesList.size == 0) {
                        btnSelect.visibility = View.GONE
                        noVideoText.visibility = View.VISIBLE
                    } else {
                        btnSelect.visibility = View.VISIBLE
                        noVideoText.visibility = View.GONE
                    }
                    allVideosAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    fun playVideo(path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)
    }

    private fun setUpRecycler() {
        videosRecyclerView = v.findViewById(R.id.allvideosRecyclerView)
        allVideosAdapter = AllVideosAdapter(filesList, this)

        val orientation: Int = context!!.resources.configuration.orientation
        setRecyclerItems(orientation)

        videosRecyclerView.adapter = allVideosAdapter

    }

    private fun setRecyclerItems(orientation: Int) {
        if (ApplicationClass.isPhone) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                videosRecyclerView.layoutManager = GridLayoutManager(context, 3)
            else
                videosRecyclerView.layoutManager = GridLayoutManager(context, 6)

        } else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                videosRecyclerView.layoutManager = GridLayoutManager(context, 5)
            else
                videosRecyclerView.layoutManager = GridLayoutManager(context, 7)
        }
    }

    private fun goToTrimActivity() {
        trimList.clear()
        for (i in checkedVideos.indices)
            trimList.add(
                TrimmerListModel(
                    checkedVideos[i].path,
                    checkedVideos[i].path,
                    checkedVideos[i].duration,
                    0,
                    checkedVideos[i].duration,
                    "", null, null, null, 0, 0
                )
            )

        if (trimList.size <= 1) {
            AppUtils.showToast(activity, "Please select at least two videos", false)
            return
        }

        if (!SharedPreference.getBoolean(context, Constants.IS_PRO_USER)) {
            if (trimList.size > 2) {
                AppUtils.showToast(activity, "You can not merge more than 2 videos", false)
                return
            }
        }

        if (trimList.size > 10) {
            AppUtils.showToast(activity, "You can not merge more than 10 videos", false)
            return
        }

        val intent = Intent(context, MergeViewPagerActivity::class.java)
        intent.putParcelableArrayListExtra("videosList", trimList)
        startActivityForResult(intent, MERGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MERGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    AppUtils.showToast(activity, "Successfully Merged", true)
                    (activity as MainActivity).goToSelectedFragment(R.id.myVideos)
                } else {
                    btnCancel.callOnClick()
                }
            }
            Constants.IMPORT_VIDEOS_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue = data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (!returnValue.isNullOrEmpty()) {
                        var isFileExist = false

                        val importFilesList: MutableList<ImportFilesModel> = ArrayList()
                        importFilesList.clear()

                        for (i in returnValue?.indices!!) {
                            for (j in filesList.indices)
                                if (filesList[j].path == returnValue[i])
                                    isFileExist = true

                            val duration = AppUtils.getVideoDuration(returnValue[i], context!!)
                            if (!isFileExist)
                                importFilesList.add(
                                    ImportFilesModel(
                                        0,
                                        returnValue[i],
                                        duration,
                                        false,
                                        returnValue[i].substring(returnValue[i].lastIndexOf("/") + 1),
                                        File(returnValue[i]).lastModified()
                                    )
                                )
                        }

                        CoroutineScope(IO).launch {
                            database?.screenRecordingDAO()?.saveImportPaths(importFilesList)

                            withContext(Main) {
                                (activity as MainActivity).goToSelectedFragment(R.id.allVideos)
                                if (isFileExist)
                                    AppUtils.showToast(activity, "Some files already exists", true)
                                else
                                    AppUtils.showToast(activity, "Files Imported", true)

                            }

                        }
                    }
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    btnImport.callOnClick()
                }
                return
            }
        }
    }


    fun showEditNameDialog(path: String, name :String, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(activity)
        val mView: View =
            layoutInflaterAndroid.inflate(R.layout.dialog_rename_file, null)
        val alertDialogBuilderUserInput =
            AlertDialog.Builder(activity)
        alertDialogBuilderUserInput.setView(mView)


        val btnSave: TextView = mView.findViewById(R.id.btnSave)
        val btnCancel: TextView = mView.findViewById(R.id.btnCancel)
        val editText: EditText = mView.findViewById(R.id.etDialog)

        val from = File(path)
//        editText.setText(from.name.substring(0, from.name.indexOf(".")))
        editText.setText(name)

        alertDialogBuilderUserInput
            .setCancelable(true)

        val alertDialogAndroid =
            alertDialogBuilderUserInput.create()

        alertDialogAndroid.setOnShowListener { _ ->
            alertDialogAndroid.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.GONE
            alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.GONE
        }

        btnSave.setOnClickListener {
            val to = File("$directoryVideos/${editText.text.toString()}.mp4")

            if (filesList[position].fromImport) {
                CoroutineScope(IO).launch {
                    database?.screenRecordingDAO()
                        ?.updateName("${editText.text.toString()}.mp4", filesList[position].id)
//                    database?.screenRecordingDAO()
//                        ?.updatePath(to.absolutePath, filesList[position].id)
                }
            } else {
                val renamed = from.renameTo(to)

                if (renamed) {
                    AppUtils.showToast(activity, "File Name Changed", true)
                } else {
                    AppUtils.showToast(activity, "File Name Not Changed", false)
                }
                filesList[position].path = to.absolutePath
            }
            filesList[position].name = editText.text.toString() + ".mp4"
            allVideosAdapter.notifyItemChanged(position)

            alertDialogAndroid.dismiss()
        }

        btnCancel.setOnClickListener {
            alertDialogAndroid.dismiss()
        }

        alertDialogAndroid.show()

    }

    fun shareVideo(path: String) {
        startActivityForResult(
            Intent.createChooser(
                Intent().setAction(Intent.ACTION_SEND)
                    .setType("video/*")
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra(
                        Intent.EXTRA_STREAM,
                        Uri.parse(path)
                    ), "Recording app"
            ), Constants.SHARE_INTENT_REQUEST_CODE
        )

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        setRecyclerItems(newConfig.orientation)

    }


}
