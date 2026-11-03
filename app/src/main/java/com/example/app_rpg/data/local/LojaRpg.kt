package com.example.app_rpg.data.local

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lojas")
data class LojaRpg(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome : String,
    val descricao: String,
    val imagemUri: Uri?,
    val visivel: Boolean,
    val itemIds: Set<Int> = emptySet()
)
