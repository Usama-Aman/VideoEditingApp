package com.elementary.youmerge.all_videos

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elementary.youmerge.R
import com.elementary.youmerge.utils.AppUtils
import java.io.File

class AllVideosAdapter(
    _filesList: MutableList<FileListModel>,
    _allVideosFragment: AllVideosFragment
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var filesList = _filesList
    private var allVideosFragment = _allVideosFragment
    private lateinit var context: Context
    private var mLastClickTime: Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return Item(
            LayoutInflater.from(parent.context).inflate(R.layout.item_all_videos, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Item)
            holder.bind(position)
    }

    override fun getItemCount(): Int = filesList.size

    inner class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var imageView: ImageView = itemView.findViewById(R.id.imageView)
        private var allVideoItemLayout: ConstraintLayout =
            itemView.findViewById(R.id.allVideoItemLayout)
        private var ivTick: ImageView = itemView.findViewById(R.id.ivTick)
        private var tvName: TextView = itemView.findViewById(R.id.tvName)
        private var btnShareVideo: ImageView = itemView.findViewById(R.id.btnShareVideo)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val uri: Uri = Uri.fromFile(File(filesList[position].path))
            Glide.with(context).load(uri).thumbnail(1f).placeholder(R.drawable.logo).into(imageView)

            tvName.text = filesList[position].name

            imageView.setOnClickListener {
                AppUtils.preventTwoClick(imageView)
                if (allVideosFragment.isSelectEnabled) {

                    filesList[adapterPosition].isChecked = !filesList[adapterPosition].isChecked
                    if (filesList[adapterPosition].isChecked)
                        allVideosFragment.checkedVideos.add(filesList[adapterPosition])
                    else {
                        if (allVideosFragment.checkedVideos.contains(filesList[adapterPosition]))
                            allVideosFragment.checkedVideos.remove(filesList[adapterPosition])
                    }

                    notifyItemChanged(adapterPosition)
                } else {
                    allVideosFragment.playVideo(filesList[position].path)
                }
            }

            tvName.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                allVideosFragment.showEditNameDialog(filesList[position].path, filesList[position].name, adapterPosition)
            }

            if (filesList[position].isChecked)
                ivTick.visibility = View.VISIBLE
            else
                ivTick.visibility = View.GONE

            btnShareVideo.visibility = View.VISIBLE
            btnShareVideo.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                allVideosFragment.shareVideo(filesList[adapterPosition].path)
            }

        }

    }
}