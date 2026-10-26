package com.example.app_rpg

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ItemRpg::class, LojaRpg::class, CharacterSheet::class, Musica::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun lojaDao(): LojaDao
    abstract fun fichaDao(): FichaDao
    abstract fun musicaDao(): MusicaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_rpg.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
