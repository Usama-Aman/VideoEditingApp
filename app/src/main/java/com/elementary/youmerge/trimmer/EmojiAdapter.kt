package com.elementary.youmerge.trimmer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.youmerge.R
import com.elementary.youmerge.viewpager.MergeViewPagerActivity

class EmojiAdapter(_trimmerListActivity: MergeViewPagerActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val eArray = arrayOf(
        0x1F600, 0x1F603, 0x1F604, 0x1F601, 0x1F606, 0x1F605, 0x1F923, 0x1F602, 0x1F61A, 0x1F619,
        0x1F642, 0x1F643, 0x1F609, 0x1F60A, 0x1F607, 0x1F60B, 0x1F60D, 0x1F929, 0x1F618, 0x1F617,
        0x1F61C, 0x1F92A, 0x1F61D, 0x1F911, 0x1F917, 0x1F92B, 0x1F914, 0x1F910, 0x1F928, 0x1F610,
        0x1F611, 0x1F636, 0x1F60F, 0x1F644, 0x1F62C, 0x1F925, 0x1F60C, 0x1F62A, 0x1F634, 0x1F637,
        0x1F927, 0x1F92C, 0x1F608, 0x1F47B, 0x1F635, 0x1F92F, 0x1F920, 0x1F649, 0x1F64A, 0x1F60E,
        0x1F913, 0x1F9D0, 0x1F615, 0x1F641, 0x1F62F, 0x1F632, 0x1F633, 0x1F97A, 0x1F626, 0x1F627,
        0x1F622, 0x1F62D, 0x1F631, 0x1F616, 0x1F61E, 0x1F613, 0x1F629, 0x1F62B, 0x1F971, 0x1F624
    )
    private lateinit var context: Context
    private var trimmerListActivity = _trimmerListActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return Item(LayoutInflater.from(context).inflate(R.layout.item_emoji, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Item)
            holder.bind(position)
    }

    override fun getItemCount(): Int = eArray.size

    inner class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val emojiTextView: TextView = itemView.findViewById(R.id.emojiTextView)

        fun bind(position: Int) {

            emojiTextView.text = String(Character.toChars(eArray[position]))

            emojiTextView.setOnClickListener {
                trimmerListActivity.createEmojiSticker(String(Character.toChars(eArray[position])),true)
            }

        }

        fun getEmojiByUnicode(unicode: Int): String? {
            return String(Character.toChars(unicode))
        }

    }
}