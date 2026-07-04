package com.pan123nextgen.android.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class Pan123Api {

    companion object {
        private const val TAG = "Pan123Api"
        private const val BASE_URL = "https://www.123pan.cn"
        private const val APP_VERSION = "2.4.0"

        val ALL_DEVICE_TYPES = listOf(
            "MI-ONE PLUS", "MI-ONE C1", "M2007J1SC", "M2011K2C", "M2102K1AC",
            "M2012K11G", "2201123C", "2206123SC", "2211133C", "2304FPN6DC",
            "23127PN0CC", "24031PN0DC", "2406APNFAG", "2407FPN8EG"
        )
        val ALL_OS_VERSIONS = listOf(
            "Android_7.1.2", "Android_8.0.0", "Android_8.1.0", "Android_9.0",
            "Android_10", "Android_11", "Android_12", "Android_13", "Android_6.0.1"
        )
        val MAX_STORAGE_CAPACITY = 2L * 1024 * 1024 * 1024 * 1024
    }

    private val gson = Gson()
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val transferClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(16, 5, TimeUnit.MINUTES))
        .build()

    // User state
    var userName: String = ""
    var password: String = ""
    var authorization: String = ""
    var deviceType: String = ALL_DEVICE_TYPES[Random().nextInt(ALL_DEVICE_TYPES.size)]
    var osVersion: String = ALL_OS_VERSIONS[Random().nextInt(ALL_OS_VERSIONS.size)]
    var loginUuid: String = UUID.randomUUID().toString().replace("-", "")
    var parentFileId: Long = 0
    val parentFileList = mutableListOf(0L)
    var fileList = mutableListOf<FileItemModel>()
    var totalFiles = 0

    private fun buildDefaultHeaders(): Headers {
        return Headers.Builder()
            .add("accept-encoding", "gzip")
            .add("content-type", "application/json")
            .add("platform", "android")
            .add("devicename", "Xiaomi")
            .add("host", "www.123pan.cn")
            .add("app-version", "61")
            .add("x-app-version", APP_VERSION)
            .add("user-agent", "123pan/v$APP_VERSION($osVersion;Xiaomi)")
            .add("osversion", osVersion)
            .add("devicetype", deviceType)
            .add("loginuuid", loginUuid)
            .apply {
                if (authorization.isNotEmpty()) {
                    add("authorization", authorization)
                }
            }
            .build()
    }

    private fun buildRequest(url: String, body: Any? = null, method: String = "GET"): Request {
        val jsonBody = if (body != null) gson.toJson(body).toRequestBody(JSON_MEDIA) else null
        return Request.Builder()
            .url(url)
            .headers(buildDefaultHeaders())
            .method(method, jsonBody)
            .build()
    }

    private suspend fun <T> execute(
        request: Request,
        parser: (Map<String, Any?>) -> T?
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: "{}"
            Log.d(TAG, "HTTP ${response.code} -> $bodyStr")
            @Suppress("UNCHECKED_CAST")
            val json = gson.fromJson(bodyStr, Map::class.java) as? Map<String, Any?> ?: mapOf()
            if (json.isEmpty() && bodyStr.isNotBlank()) {
                Log.w(TAG, "Response not a JSON object, raw (first 200): ${bodyStr.take(200)}")
            }
            val result = parser(json)
            if (result != null) Result.success(result)
            else Result.failure(Exception("API returned null/error response"))
        } catch (e: Exception) {
            Log.e(TAG, "API error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Enhanced login with debug info
    suspend fun login(user: String, pwd: String): Result<Map<String, Any?>> {
        val url = "$BASE_URL/b/api/user/sign_in"
        val data = mapOf("type" to 1, "passport" to user, "password" to pwd)
        val request = buildRequest(url, data, "POST")
        Log.d(TAG, "Login URL: $url")

        @Suppress("UNCHECKED_CAST")
        return execute(request) { json ->
            val code = (json["code"] as? Number)?.toInt() ?: -1
            if (code == 200) {
                val data = json["data"] as? Map<String, Any?>
                val token = data?.get("token") as? String ?: ""
                authorization = "Bearer $token"
                userName = user
                password = pwd
                mapOf("token" to token, "authorization" to authorization)
            } else {
                null
            }
        }
    }

    // ============== File List ==============

    suspend fun getFileList(
        fileId: Long = 0,
        page: Int = 1,
        limit: Int = 100,
        orderBy: String = "file_id",
        orderDirection: String = "desc"
    ): Result<FileListResponse> {
        val url = "$BASE_URL/api/file/list/new"
        val params = mapOf(
            "driveId" to "0",
            "limit" to limit.toString(),
            "next" to "0",
            "orderBy" to orderBy,
            "orderDirection" to orderDirection,
            "parentFileId" to fileId.toString(),
            "trashed" to "false",
            "SearchData" to "",
            "Page" to page.toString(),
            "OnlyLookAbnormalFile" to "0"
        )
        val urlBuilder = StringBuilder(url)
        params.forEach { (k, v) ->
            val prefix = if (urlBuilder.contains("?")) "&" else "?"
            urlBuilder.append("$prefix$k=$v")
        }
        val request = buildRequest(urlBuilder.toString())
        return execute(request) { json -> FileListResponse.fromMap(json) }
    }

    suspend fun loadDirectory(dirId: Long, all: Boolean = true): Pair<Int, List<FileItemModel>> {
        val maxPages = if (all) 100 else 3
        var page = 1
        var total = -1
        val allItems = mutableListOf<FileItemModel>()
        var pagesFetched = 0
        var lastTotal = -1

        while ((allItems.size < total || total == -1) && pagesFetched < maxPages) {
            val result = getFileList(fileId = dirId, page = page)
            result.onSuccess { response ->
                val data = response.data
                if (data != null) {
                    total = data.total
                    allItems.addAll(data.infoList)
                    lastTotal = data.total
                }
            }.onFailure {
                return Pair(-1, emptyList())
            }
            page++
            pagesFetched++
            if (pagesFetched % 5 == 0) {
                Log.w(TAG, "Many files in directory: ${allItems.size}/$total")
                kotlinx.coroutines.delay(3000)
            }
        }

        return Pair(0, allItems)
    }

    // ============== File Operations ==============

    suspend fun createFolder(name: String, parentId: Long): Result<Long> {
        val url = "$BASE_URL/a/api/file/upload_request"
        val data = mapOf(
            "driveId" to 0,
            "etag" to "",
            "fileName" to name,
            "parentFileId" to parentId,
            "size" to 0,
            "type" to 1,
            "duplicate" to 1,
            "NotReuse" to true,
            "event" to "newCreateFolder",
            "operateType" to 1
        )
        val request = buildRequest(url, data, "POST")
        return execute(request) { json ->
            val code = (json["code"] as? Number)?.toInt() ?: -1
            if (code == 0) {
                val data = json["data"] as? Map<String, Any?>
                val info = data?.get("Info") as? Map<String, Any?>
                (info?.get("FileId") as? Number)?.toLong()
            } else null
        }
    }

    suspend fun renameFile(fileId: Long, newName: String): Result<Boolean> {
        val url = "$BASE_URL/a/api/file/rename"
        val data = mapOf("driveId" to 0, "fileId" to fileId, "fileName" to newName)
        val request = buildRequest(url, data, "POST")
        return execute(request) { json ->
            (json["code"] as? Number)?.toInt() == 0
        }
    }

    suspend fun trashFile(fileId: Long, operation: Boolean = true): Result<Boolean> {
        val url = "$BASE_URL/a/api/file/trash"
        val payload = mapOf(
            "driveId" to 0,
            "fileTrashInfoList" to listOf(mapOf("FileId" to fileId)),
            "operation" to operation
        )
        val request = buildRequest(url, payload, "POST")
        return execute(request) { json ->
            (json["code"] as? Number)?.toInt() == 0
        }
    }

    // ============== Download ==============

    suspend fun getDownloadUrl(fileId: Long, etag: String = "", s3keyFlag: String = "", type: Int = 0, fileName: String = "", size: Long = 0): Result<String> {
        val url = "$BASE_URL/a/api/file/download_info"
        val data = mapOf(
            "driveId" to 0,
            "etag" to etag,
            "fileId" to fileId,
            "s3keyFlag" to s3keyFlag,
            "type" to type,
            "fileName" to fileName,
            "size" to size
        )
        val request = buildRequest(url, data, "POST")
        return execute(request) { json ->
            val code = (json["code"] as? Number)?.toInt() ?: -1
            if (code == 0) {
                val data = json["data"] as? Map<String, Any?>
                data?.get("DownloadUrl") as? String
            } else null
        }
    }

    suspend fun getFolderDownloadUrl(fileIds: List<Long>): Result<String> {
        val url = "$BASE_URL/a/api/file/batch_download_info"
        val data = mapOf("fileIdList" to fileIds.map { mapOf("fileId" to it) })
        val request = buildRequest(url, data, "POST")
        return execute(request) { json ->
            val code = (json["code"] as? Number)?.toInt() ?: -1
            if (code == 0) {
                val data = json["data"] as? Map<String, Any?>
                data?.get("DownloadUrl") as? String
            } else null
        }
    }

    private fun resolveDownloadUrl(url: String): String {
        try {
            val request = Request.Builder().url(url).get().build()
            val response = transferClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val pattern = Regex("href='(https?://[^']+)'")
            val match = pattern.find(body)
            if (match != null) return match.groupValues[1]
        } catch (_: Exception) {}
        return url
    }

    // ============== Upload ==============

    suspend fun uploadFile(
        filePath: String,
        fileName: String,
        fileSize: Long,
        parentId: Long,
        md5: String
    ): Result<Long> {
        // Step 1: Upload request
        val uploadUrl = "$BASE_URL/b/api/file/upload_request"
        val uploadData = mapOf(
            "driveId" to 0,
            "etag" to md5,
            "fileName" to fileName,
            "parentFileId" to parentId,
            "size" to fileSize,
            "type" to 0,
            "duplicate" to 0
        )
        var request = buildRequest(uploadUrl, uploadData, "POST")
        var result = execute(request) { json ->
            val code = (json["code"] as? Number)?.toInt() ?: -1
            if (code == 5060) return@execute null // Duplicate
            if (code != 0) return@execute null
            val data = json["data"] as? Map<String, Any?>
            data
        }
        // Step 2: S3 direct upload would need presigned URL logic
        // For simplicity, returns file ID
        return Result.failure(IOException("S3 upload not implemented in basic client"))
    }

    // ============== Share ==============

    suspend fun shareFile(fileIds: List<Long>, sharePwd: String = ""): Result<String> {
        val url = "$BASE_URL/a/api/share/create"
        val data = mapOf(
            "driveId" to 0,
            "expiration" to "2099-12-12T08:00:00+08:00",
            "fileIdList" to fileIds,
            "shareName" to "123云盘分享",
            "sharePwd" to sharePwd,
            "event" to "shareCreate"
        )
        val request = buildRequest(url, data, "POST")
        return execute(request) { json ->
            val code = (json["code"] as? Number)?.toInt() ?: -1
            if (code == 0) {
                val data = json["data"] as? Map<String, Any?>
                val shareKey = data?.get("ShareKey") as? String ?: ""
                "https://www.123pan.cn/s/$shareKey"
            } else null
        }
    }

    // ============== Utility ==============

    fun getSettingFileName(name: String): String = "123pan_nextgen_android_$name"

    fun formatFileSize(size: Long): String = FileItemModel.formatFileSize(size)
}