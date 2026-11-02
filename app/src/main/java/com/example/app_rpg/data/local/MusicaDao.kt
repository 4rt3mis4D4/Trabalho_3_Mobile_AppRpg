package com.example.app_rpg.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicaDao {

    @Insert
    suspend fun inserir(musica: Musica): Long

    @Query("SELECT * FROM musicas ORDER BY id")
    fun buscarTodos(): Flow<List<Musica>>

    @Update
    suspend fun atualizar(musica: Musica)

    @Delete
    suspend fun deletar(musica: Musica)
}
