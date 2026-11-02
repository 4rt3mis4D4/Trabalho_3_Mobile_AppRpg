package com.example.app_rpg

import android.app.Application
import com.example.app_rpg.data.local.AppDatabase
import com.example.app_rpg.data.repository.MusicaRepository

class AppRpgApplication : Application() {

    private val banco by lazy { AppDatabase.getDatabase(this) }

    val musicaRepository by lazy { MusicaRepository(banco.musicaDao()) }
}
