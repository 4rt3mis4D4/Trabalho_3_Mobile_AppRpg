package com.example.app_rpg.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "musicas")
data class Musica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val artista: String,
    val genero: String
)
