package com.elementary.youmerge.trimmer

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.elementary.youmerge.utils.DrawView
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView

data class TrimmerListModel(
    var croppedPath: String = "",
    var originalPath: String = "",
    var duration: Long = 0,
    var minPosition: Long = 0,
    var maxPosition: Long = 0,
    var trimmedPath: String = "",
    var drawView: DrawView?,
    var imagesPaths: MutableList<String>?,
    var photoEditor: PhotoEditor?,
    var videoWidth : Int,
    var videoHeight : Int,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!, null, null, null,
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    fun initializeDrawView(context: Context) {
        if (this.drawView == null)
            this.drawView = DrawView(context)
    }

    fun initializePhotoEditor(context: Context, photoEditorView: PhotoEditorView) {
        this.photoEditor = PhotoEditor.Builder(context, photoEditorView)
            .setPinchTextScalable(true)
//                .setDefaultTextTypeface(mTextRobotoTf)
//                .setDefaultEmojiTypeface(mEmojiTypeFace)
            .build()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(croppedPath)
        parcel.writeString(originalPath)
        parcel.writeLong(duration)
        parcel.writeLong(minPosition)
        parcel.writeLong(maxPosition)
        parcel.writeString(trimmedPath)
        parcel.writeInt(videoWidth)
        parcel.writeInt(videoHeight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrimmerListModel> {
        override fun createFromParcel(parcel: Parcel): TrimmerListModel {
            return TrimmerListModel(parcel)
        }

        override fun newArray(size: Int): Array<TrimmerListModel?> {
            return arrayOfNulls(size)
        }
    }
}