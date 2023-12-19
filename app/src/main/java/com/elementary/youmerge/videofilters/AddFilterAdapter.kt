package com.elementary.youmerge.videofilters


import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.youmerge.R
import com.elementary.youmerge.utils.AppUtils
import com.elementary.youmerge.viewpager.MergeFragment
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter

class AddFilterAdapter(
    private val mergeFragment: MergeFragment,
    private val filePath: String
) : RecyclerView.Adapter<AddFilterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, filePath)
    }

    override fun getItemCount(): Int {
        return FilterType.createFilterList().size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val thumbnails: AppCompatImageView = itemView.findViewById(R.id.thumbnails)
        private val textFilterName: AppCompatTextView = itemView.findViewById(R.id.textFilterName)

        fun bind(position: Int, filePath: String) {

            itemView.apply {
                setOnTouchListener(MotionTouchListener())
                setOnClickListener {
                    AppUtils.preventTwoClick(itemView)
                    mergeFragment.onFilterItemClick(position)
                }
            }

            var bitmap = ThumbnailUtils.createVideoThumbnail(
                filePath,
                MediaStore.Video.Thumbnails.MICRO_KIND
            )

            val filterFile = arrayListOf<String>()
            filterFile.addAll(
                listOf(
                    "acv/afterglow.acv",
                    "acv/alice_in_wonderland.acv",
                    "acv/ambers.acv",
                    "acv/aurora.acv",
                    "acv/blue_poppies.acv",
                    "acv/blue_yellow_field.acv",
                    "acv/carousel.acv",
                    "acv/cold_desert.acv",
                    "acv/cold_heart.acv",
                    "acv/digital_film.acv",
                    "acv/documentary.acv",
                    "acv/electric.acv",
                    "acv/ghosts_in_your_head.acv",
                    "acv/good_luck_charm.acv",
                    "acv/green_envy.acv",
                    "acv/hummingbirds.acv",
                    "acv/kiss_kiss.acv",
                    "acv/left_hand_blues.acv",
                    "acv/light_parades.acv",
                    "acv/lullabye.acv",
                    "acv/moth_wings.acv",
                    "acv/moth_wings.acv",
                    "acv/old_postcards_01.acv",
                    "acv/old_postcards_02.acv",
                    "acv/peacock_feathers.acv",
                    "acv/pistol.acv",
                    "acv/ragdoll.acv",
                    "acv/rose_thorns_01.acv",
                    "acv/rose_thorns_02.acv",
                    "acv/set_you_free.acv",
                    "acv/snow_white.acv",
                    "acv/toes_in_the_ocean.acv",
                    "acv/wild_at_heart.acv",
                    "acv/window_warmth.acv"
                )
            )

            val gpuImage = GPUImage(itemView.context)
            gpuImage.setImage(bitmap)

            val gpuFilter = GPUImageToneCurveFilter()

            when (position) {
                1 -> gpuImage.setFilter(GPUImageGrayscaleFilter())
                else -> {
                    if (position > 1) {
                        val inputFilter = itemView.context.assets.open(filterFile[position - 2])
                        gpuFilter.setFromCurveFileInputStream(inputFilter)
                        inputFilter.close()

                        gpuImage.setFilter(gpuFilter)
                    }
                }
            }

            try {
                bitmap = gpuImage.bitmapWithFilterApplied
                itemView.apply {
                    thumbnails.setImageBitmap(bitmap)
                    textFilterName.text = when (position) {
                        0 -> "normal"
                        1 -> "grayscale"
                        else -> filterFile[position - 2].drop(4).dropLast(4)
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

        }
    }
}