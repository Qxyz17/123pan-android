package com.pan123nextgen.android

import android.app.Application
import com.pan123nextgen.android.data.ConfigManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ConfigManager.init(this)
    }
}