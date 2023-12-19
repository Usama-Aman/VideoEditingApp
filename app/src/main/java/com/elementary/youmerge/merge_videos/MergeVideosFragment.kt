package com.elementary.youmerge.merge_videos

import android.app.AlertDialog
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.youmerge.ApplicationClass
import com.elementary.youmerge.R
import com.elementary.youmerge.all_videos.FileListModel
import com.elementary.youmerge.utils.AppUtils
import com.elementary.youmerge.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MergeVideosFragment : Fragment() {

    private lateinit var v: View
    private var filesList: MutableList<FileListModel> = ArrayList()
    private lateinit var videosRecyclerView: RecyclerView
    private lateinit var mergeVideosAdapter: MergeVideosAdapter

    private lateinit var directoryVideos: File
    private lateinit var directoryMergeVideos: File

    private lateinit var btnDeleteVideos: TextView
    private lateinit var btnSelect: TextView
    private lateinit var btnCancel: TextView
    private lateinit var noVideoText: TextView

    var isSelectEnabled = false

    lateinit var alertDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_merge_videos, container, false)

        initViews()
        setUpRecycler()
        getAllFiles()

        return v
    }

    private fun initViews() {
        btnDeleteVideos = v.findViewById(R.id.deleteVideos)
        btnCancel = v.findViewById(R.id.cancel)
        btnSelect = v.findViewById(R.id.select)
        noVideoText = v.findViewById(R.id.noVideoText)

        btnDeleteVideos.setOnClickListener {
            AppUtils.preventTwoClick(btnDeleteVideos)
            showDeleteDialog()

        }


        btnSelect.setOnClickListener {
            AppUtils.preventTwoClick(btnSelect)
            btnSelect.visibility = View.GONE

            btnDeleteVideos.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE

            isSelectEnabled = true
        }

        btnCancel.setOnClickListener {
            AppUtils.preventTwoClick(btnSelect)
            btnDeleteVideos.visibility = View.GONE
            btnCancel.visibility = View.GONE

            btnSelect.visibility = View.VISIBLE
            isSelectEnabled = false

            for (i in filesList.indices)
                filesList[i].isChecked = false

            mergeVideosAdapter.notifyDataSetChanged()
        }
    }

    private fun getAllFiles() {

        directoryMergeVideos = File(
            context!!.getExternalFilesDir(null),
            "${Constants.APP_NAME}/${Constants.MERGER_VIDEOS_FOLDER}"
        )

        AppUtils.showDialog(context!!)
        CoroutineScope(IO).launch {
            val files = directoryMergeVideos.listFiles()!!

            if (files != null && files.size > 1) {
                files.sortByDescending { it.lastModified() }
            }

            filesList.clear()
            for (i in files.indices) {
                if (files[i].length() > 100000)
                    filesList.add(
                        FileListModel(
                            (files[i].path),
                            AppUtils.getVideoDuration(files[i].path, context!!),
                            false,
                            files[i].name,
                            files[i].lastModified(), false
                        )
                    )
                Log.d("Files", "${files[i].name} - ${files[i].length()}")
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

                mergeVideosAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setUpRecycler() {
        videosRecyclerView = v.findViewById(R.id.allvideosRecyclerView)
        mergeVideosAdapter = MergeVideosAdapter(filesList, this)

        val orientation: Int = context!!.resources.configuration.orientation
        setRecyclerItems(orientation)

        videosRecyclerView.adapter = mergeVideosAdapter

    }

    fun playVideo(path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)
    }

    fun checkForSelected() {
        var isOneFileSelected = false
        for (i in filesList.indices)
            if (filesList[i].isChecked)
                isOneFileSelected = true

        if (isOneFileSelected) {
            btnDeleteVideos.setTextColor(ContextCompat.getColor(context!!, R.color.red))
            btnDeleteVideos.isEnabled = true
        } else {
            btnDeleteVideos.setTextColor(ContextCompat.getColor(context!!, R.color.red))
            btnDeleteVideos.isEnabled = false
        }

    }

    private fun showDeleteDialog() {
        alertDialog = AlertDialog.Builder(context)
            .setMessage("Are you sure you want to delete the selected videos?")
            .setPositiveButton("Yes") { _, _ ->
                for (i in filesList.indices)
                    if (filesList[i].isChecked) {
                        val fdelete: File = File(filesList[i].path)
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                println("file Deleted :")
                            } else {
                                println("file not Deleted :")
                            }
                        }
                    }
                alertDialog.dismiss()
                AppUtils.showToast(activity, "Selected files are deleted.", true)
                filesList.clear()
                getAllFiles()
                btnCancel.callOnClick()

            }
            .setNegativeButton("No") { _, _ ->
                alertDialog.dismiss()
            }
            .show()
    }

    fun showEditNameDialog(path: String, position: Int) {
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
        editText.setText(from.name.substring(0, from.name.indexOf(".")))

        alertDialogBuilderUserInput
            .setCancelable(true)

        val alertDialogAndroid =
            alertDialogBuilderUserInput.create()

        alertDialogAndroid.setOnShowListener { _ ->
            alertDialogAndroid.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.GONE
            alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.GONE
        }
        btnSave.setOnClickListener {
            val to = File("$directoryMergeVideos/${editText.text.toString()}.mp4")
            val renamed = from.renameTo(to)

            if (renamed) {
                mergeVideosAdapter.notifyItemChanged(position)
                AppUtils.showToast(activity, "File Name Changed", true)
            } else {
                AppUtils.showToast(activity, "File Name Not Changed", true)
            }

            filesList[position].name = editText.text.toString() + ".mp4"
            filesList[position].path = to.absolutePath
            alertDialogAndroid.dismiss()
        }

        btnCancel.setOnClickListener {
            alertDialogAndroid.dismiss()
        }



        alertDialogAndroid.show()

    }

    fun shareVideo(path: String) {
        startActivity(
            Intent.createChooser(
                Intent().setAction(Intent.ACTION_SEND)
                    .setType("video/*")
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra(
                        Intent.EXTRA_STREAM,
                        Uri.parse(path)
                    ), "Recording app"
            )
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setRecyclerItems(newConfig.orientation)
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


}