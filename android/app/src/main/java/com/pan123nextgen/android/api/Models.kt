package com.pan123nextgen.android.api

import com.google.gson.annotations.SerializedName

data class ApiReturn(
    val code: Int = -1,
    val apiCode: Int = -1,
    val message: String = "",
    val data: Any? = null
)

data class DeviceModel(
    val os: String,
    val type: String
)

data class UserInfoModel(
    val userName: String,
    val password: String,
    val uuid: String,
    val authorization: String,
    val device: DeviceModel
)

data class FileItemModel(
    @SerializedName("FileId") val fileId: Long = 0,
    @SerializedName("FileName") val fileName: String = "",
    @SerializedName("Type") val type: Int = 0,
    @SerializedName("Size") val size: Long = 0,
    @SerializedName("CreateAt") val createAt: Long = 0,
    @SerializedName("UpdateAt") val updateAt: Long = 0,
    @SerializedName("Hidden") val hidden: Boolean = false,
    @SerializedName("Etag") val etag: String = "",
    @SerializedName("S3KeyFlag") val s3keyFlag: String = "",
    @SerializedName("ContentType") val contentType: String = "",
    @SerializedName("ParentFileId") val parentFileId: Long = 0,
    @SerializedName("PinYin") val pinYin: String = "",
    @SerializedName("StarredStatus") val starredStatus: Boolean = false,
    @SerializedName("DownloadUrl") val downloadUrl: String? = null
) {
    fun isDir(): Boolean = type == 1

    fun formattedSize(): String = formatFileSize(size)

    companion object {
        fun fromMap(json: Map<String, Any?>): FileItemModel {
            return FileItemModel(
                fileId = (json["FileId"] as? Number)?.toLong() ?: 0,
                fileName = json["FileName"] as? String ?: "",
                type = (json["Type"] as? Number)?.toInt() ?: 0,
                size = (json["Size"] as? Number)?.toLong() ?: 0,
                createAt = (json["CreateAt"] as? Number)?.toLong() ?: 0,
                updateAt = (json["UpdateAt"] as? Number)?.toLong() ?: 0,
                hidden = json["Hidden"] as? Boolean ?: false,
                etag = json["Etag"] as? String ?: "",
                s3keyFlag = json["S3KeyFlag"] as? String ?: "",
                contentType = json["ContentType"] as? String ?: "",
                parentFileId = (json["ParentFileId"] as? Number)?.toLong() ?: 0,
                pinYin = json["PinYin"] as? String ?: "",
                starredStatus = json["StarredStatus"] as? Boolean ?: false
            )
        }

        fun formatFileSize(size: Long): String {
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var s = size.toDouble()
            var i = 0
            while (s >= 1024.0 && i < units.size - 1) {
                s /= 1024.0
                i++
            }
            return "%.2f %s".format(s, units[i])
        }
    }
}

data class FileListData(
    @SerializedName("Next") val next: String = "-1",
    @SerializedName("Len") val len: Int = 0,
    @SerializedName("Total") val total: Int = 0,
    @SerializedName("IsFirst") val isFirst: Boolean = false,
    @SerializedName("InfoList") val infoList: List<FileItemModel> = emptyList()
)

data class FileListResponse(
    val code: Int = -1,
    val message: String = "",
    val data: FileListData? = null
) {
    companion object {
        fun fromMap(json: Map<String, Any?>): FileListResponse {
            val dataMap = json["data"] as? Map<String, Any?>
            val infoList = (dataMap?.get("InfoList") as? List<Map<String, Any?>>)?.map {
                FileItemModel.fromMap(it)
            } ?: emptyList()
            val data = FileListData(
                next = dataMap?.get("Next") as? String ?: "-1",
                len = (dataMap?.get("Len") as? Number)?.toInt() ?: 0,
                total = (dataMap?.get("Total") as? Number)?.toInt() ?: 0,
                isFirst = dataMap?.get("IsFirst") as? Boolean ?: false,
                infoList = infoList
            )
            return FileListResponse(
                code = (json["code"] as? Number)?.toInt() ?: -1,
                message = json["message"] as? String ?: "",
                data = data
            )
        }
    }
}

data class TransferTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: TaskType,
    val name: String,
    val size: Long,
    val localPath: String? = null,
    val fileId: Long? = null,
    val savePath: String? = null,
    val targetDirId: Long? = null,
    val currentDirId: Long = 0,
    var progress: Int = 0,
    var status: TaskStatus = TaskStatus.WAITING
)

enum class TaskType { UPLOAD, DOWNLOAD }
enum class TaskStatus {
    WAITING, UPLOADING, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

data class UploadRequest(
    val driveId: Int = 0,
    val etag: String,
    val fileName: String,
    val parentFileId: Long,
    val size: Long,
    val type: Int = 0,
    val duplicate: Int = 0,
    val NotReuse: Boolean = true,
    val event: String = "newCreateFolder",
    val operateType: Int = 1
)

// Settings data classes
data class AppSettings(
    var defaultDownloadPath: String = "",
    var askDownloadLocation: Boolean = true,
    var multiThreadDownload: Boolean = true,
    var downloadSpeedLimit: Int = 0,
    var uploadSpeedLimit: Int = 0,
    var proxyEnabled: Boolean = false,
    var proxyType: String = "http",
    var proxyHost: String = "",
    var proxyPort: Int = 0,
    var proxyUsername: String = "",
    var proxyPassword: String = ""
)