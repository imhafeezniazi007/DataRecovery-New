package com.example.datarecovery.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.datarecovery.R


class AppPreferences(val appContext: Context) {

    companion object{

        var appPreferences:AppPreferences ?= null

        fun getInstance(context: Context):AppPreferences{
            if(appPreferences == null){
                appPreferences = AppPreferences(context)
            }
            return appPreferences!!
        }
    }

    fun setTheme(theme: Int) {
        val editor = sharedPref().edit()
        editor.putInt("Theme", theme)
        editor.apply()
    }
    val getTheme: Int
        get() = sharedPref().getInt("Theme", 0)

    fun setFirstTimeUser(firstTimeUser: Boolean) {
        val editor = sharedPref().edit()
        editor.putBoolean("FirstTimeUser", firstTimeUser)
        editor.apply()
    }
    val isFirstTimeUser: Boolean
        get() = sharedPref().getBoolean("FirstTimeUser", true)

    fun setAppPurchased(firstTimeUser: Boolean) {
        val editor = sharedPref().edit()
        editor.putBoolean("AppPurchased", firstTimeUser)
        editor.apply()
    }
    val isAppPurchased: Boolean
        get() = sharedPref().getBoolean("AppPurchased", false)

    fun setDriveLoginEmail(email: String) {
        val editor = sharedPref().edit()
        editor.putString("DriveLoginEmail", email)
        editor.apply()
    }
    val getDriveLoginEmail: String
        get() = sharedPref().getString("DriveLoginEmail","")!!

    fun setDriveLoginName(email: String) {
        val editor = sharedPref().edit()
        editor.putString("DriveLoginName", email)
        editor.apply()
    }
    val getDriveLoginName: String
        get() = sharedPref().getString("DriveLoginName","")!!

    fun sharedPref(): SharedPreferences {
        return appContext.getSharedPreferences(
        appContext.getString(R.string.app_preferences),
        Context.MODE_PRIVATE
        )
    }


}