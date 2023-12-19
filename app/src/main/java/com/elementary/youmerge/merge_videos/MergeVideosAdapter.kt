package com.elementary.youmerge.merge_videos

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
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
import com.elementary.youmerge.all_videos.FileListModel
import com.elementary.youmerge.utils.AppUtils

class MergeVideosAdapter(
    _filesList: MutableList<FileListModel>,
    _mergeVideosFragment: MergeVideosFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var filesList = _filesList
    private var mergeVideosFragment = _mergeVideosFragment
    private lateinit var context: Context
    private var mLastClickTime: Long = 0

    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        mediaMetadataRetriever = MediaMetadataRetriever()
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
            Glide.with(context).load(filesList[position].path).placeholder(R.drawable.logo)
                .into(imageView)

            tvName.text = filesList[position].name

            imageView.setOnClickListener {
                AppUtils.preventTwoClick(imageView)
                if (mergeVideosFragment.isSelectEnabled) {
                    filesList[adapterPosition].isChecked = !filesList[adapterPosition].isChecked
                    mergeVideosFragment.checkForSelected()
                    notifyItemChanged(adapterPosition)
                } else {
                    mergeVideosFragment.playVideo(filesList[position].path)
                }
            }

            tvName.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mergeVideosFragment.showEditNameDialog(filesList[position].path, position)
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
                mergeVideosFragment.shareVideo(filesList[adapterPosition].path)
            }

        }

    }


}