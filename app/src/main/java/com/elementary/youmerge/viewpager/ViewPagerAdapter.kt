package com.elementary.youmerge.viewpager

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.youmerge.trimmer.TrimmerListModel
import java.util.*
import kotlin.collections.ArrayList


class ViewPagerAdapter(
    _videosList: ArrayList<TrimmerListModel>,
    _fragmentsList: MutableList<MergeFragment>,
    _positionsList: MutableList<Int>,
    _fragmentManager: FragmentManager
) : FragmentPagerAdapter(
    _fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    val fragmentsList = _fragmentsList
    private val positionsList = _positionsList

    override fun getCount(): Int = fragmentsList.size

    override fun getItem(position: Int): Fragment = fragmentsList[position]

    override fun getItemPosition(`object`: Any): Int {
        return if (`object` is MergeFragment) {
            if (fragmentsList.contains(`object`)) {
                fragmentsList.indexOf(`object`)
            } else {
                POSITION_NONE
            }
        } else super.getItemPosition(`object`)
    }

    override fun getItemId(position: Int): Long {
        return positionsList[position].toLong()
    }

    fun delPage(position: Int) {
        fragmentsList.removeAt(position)
        positionsList.remove(position)
        notifyDataSetChanged()
    }

    fun moveItems(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(fragmentsList, i, i + 1)
                Collections.swap(positionsList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(fragmentsList, i, i - 1)
                Collections.swap(positionsList, i, i - 1)
            }
        }
        notifyDataSetChanged()
    }


}