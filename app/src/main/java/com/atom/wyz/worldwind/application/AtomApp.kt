package com.atom.wyz.worldwind.application

import android.app.Application
import android.util.Log

class AtomApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.e("WorldWind" , "AtomApp") ;
    }
}