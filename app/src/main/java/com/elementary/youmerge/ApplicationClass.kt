package com.elementary.youmerge

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.util.Log

class ApplicationClass : Application() {

    companion object {
        var isPhone = false
    }

    override fun onCreate() {
        super.onCreate()

        isPhone = this.resources.getBoolean(R.bool.isPhone)

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("Config", "changed")
    }

}