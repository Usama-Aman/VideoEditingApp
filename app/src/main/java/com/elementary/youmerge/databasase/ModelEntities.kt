package com.elementary.youmerge.databasase

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImportFilesModel(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var path: String,
    var duration: Long,
    var isChecked: Boolean,
    var name: String,
    var lastModified: Long
) {

}
