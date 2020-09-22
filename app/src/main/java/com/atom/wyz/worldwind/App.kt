package com.atom.wyz.worldwind

import android.app.Application

class App : Application(){

    companion object{
        private lateinit var instance: App
        fun getInstance(): Application {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}