package com.pan123nextgen.android.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pan123nextgen.android.api.AppSettings

class ConfigManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pan123_nextgen_config", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Saved accounts
    fun getAccounts(): MutableMap<String, Map<String, String>> {
        val json = prefs.getString("accounts", "{}") ?: "{}"
        val type = object : TypeToken<MutableMap<String, Map<String, String>>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    fun saveAccounts(accounts: Map<String, Map<String, String>>) {
        prefs.edit().putString("accounts", gson.toJson(accounts)).apply()
    }

    fun getCurrentAccount(): String = prefs.getString("currentAccount", "") ?: ""

    fun setCurrentAccount(name: String) {
        prefs.edit().putString("currentAccount", name).apply()
    }

    fun saveAccount(name: String, info: Map<String, String>, setCurrent: Boolean = true) {
        val accounts = getAccounts()
        accounts[name] = info
        saveAccounts(accounts)
        if (setCurrent) setCurrentAccount(name)
    }

    fun getAccount(name: String): Map<String, String>? = getAccounts()[name]

    fun getAccountNames(): List<String> = getAccounts().keys.toList()

    fun removeAccount(name: String) {
        val accounts = getAccounts()
        accounts.remove(name)
        saveAccounts(accounts)
        if (getCurrentAccount() == name) setCurrentAccount("")
    }

    // Settings
    private val settingsCache: AppSettings by lazy { loadSettings() }

    private fun loadSettings(): AppSettings {
        val json = prefs.getString("settings", null)
        return if (json != null) {
            try {
                gson.fromJson(json, AppSettings::class.java)
            } catch (_: Exception) {
                AppSettings()
            }
        } else AppSettings()
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().putString("settings", gson.toJson(settings)).apply()
        settingsCache.defaultDownloadPath = settings.defaultDownloadPath
        settingsCache.askDownloadLocation = settings.askDownloadLocation
        settingsCache.multiThreadDownload = settings.multiThreadDownload
        settingsCache.downloadSpeedLimit = settings.downloadSpeedLimit
        settingsCache.uploadSpeedLimit = settings.uploadSpeedLimit
        settingsCache.proxyEnabled = settings.proxyEnabled
        settingsCache.proxyType = settings.proxyType
        settingsCache.proxyHost = settings.proxyHost
        settingsCache.proxyPort = settings.proxyPort
        settingsCache.proxyUsername = settings.proxyUsername
        settingsCache.proxyPassword = settings.proxyPassword
    }

    fun getSettings(): AppSettings = settingsCache

    fun getSettingString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun setSettingString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getSettingInt(key: String, default: Int = 0): Int =
        prefs.getInt(key, default)

    fun setSettingInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getSettingBool(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun setSettingBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        @Volatile
        private var instance: ConfigManager? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = ConfigManager(context.applicationContext)
            }
        }

        fun getInstance(): ConfigManager {
            return instance ?: throw IllegalStateException("ConfigManager not initialized. Call ConfigManager.init(context) first.")
        }
    }
}