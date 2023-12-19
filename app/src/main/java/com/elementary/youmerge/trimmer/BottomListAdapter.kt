package com.elementary.youmerge.trimmer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elementary.youmerge.R
import com.elementary.youmerge.viewpager.MergeViewPagerActivity
import java.util.*
import kotlin.collections.ArrayList


class BottomListAdapter(
    _videosList: ArrayList<TrimmerListModel>,
    _trimmerListActivity: MergeViewPagerActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemMoveCallback.ItemTouchHelperContract {

    private lateinit var context: Context
    private val videosList = _videosList
    private val trimmerListActivity = _trimmerListActivity

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return Item(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_trimmer_videos_bottom, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        if (holder is Item) {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int = videosList.size

    inner class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemBottomVideo: ConstraintLayout =
            itemView.findViewById(R.id.itemBottomVideo)
        private val imageView: ImageView =
            itemView.findViewById(R.id.imageView)

        fun bind(position: Int) {
            Glide.with(context).load(videosList[position].originalPath).thumbnail(0.1f)
                .into(imageView)

            if (selectedPosition == position)
                itemBottomVideo.background =
                    context.resources.getDrawable(R.drawable.selectedvideoborder, null)
            else
                itemBottomVideo.background = null

            itemBottomVideo.setOnClickListener {
                trimmerListActivity.viewPager.setCurrentItem(adapterPosition, true)
            }


        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(videosList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(videosList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        trimmerListActivity.viewPagerAdapter.moveItems(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: Item?) {
    }

    override fun onRowClear(myViewHolder: Item?) {
    }
}