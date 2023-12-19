package com.elementary.youmerge.all_videos

data class FileListModel(
    var path: String,
    var duration: Long,
    var isChecked: Boolean,
    var name: String,
    var lastModified: Long,
    var fromImport: Boolean,
    var id: Int = 0

)
