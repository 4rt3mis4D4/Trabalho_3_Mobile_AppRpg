package com.example.app_rpg

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

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
        private val musicasIniciais = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """INSERT INTO musicas (titulo, artista, genero) VALUES
                        ('Trilha da Taverna', 'Artista exemplo', 'MPB'),
                        ('Batalha Final', 'Artista exemplo', 'Rock'),
                        ('Viagem Fantástica', 'Artista exemplo', 'Pop'),
                        ('Holy', 'Justin Bieber', 'Pop')"""
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_rpg.db"
                ).addCallback(musicasIniciais).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
