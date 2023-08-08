package com.example.thedayto

import android.app.Application
import com.example.thedayto.data.entry.AppContainer
import com.example.thedayto.data.entry.AppDataContainer

class TheDayToApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}