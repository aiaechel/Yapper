package com.yapper.Yapper.utils

import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import com.yapper.Yapper.YapperApplication

class SharedPreferencesProvider {
    companion object {
        val sharedPreferences : SharedPreferences by lazy {
            PreferenceManager.getDefaultSharedPreferences(YapperApplication.getApplicationContext())
        }
    }
}