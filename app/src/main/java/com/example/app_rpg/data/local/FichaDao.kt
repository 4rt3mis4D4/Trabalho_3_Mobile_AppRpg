package com.example.app_rpg.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FichaDao {

    @Insert
    suspend fun inserir(ficha: CharacterSheet): Long

    @Query("SELECT * FROM fichas ORDER BY id")
    fun buscarTodos(): Flow<List<CharacterSheet>>

    @Update
    suspend fun atualizar(ficha: CharacterSheet)

    @Delete
    suspend fun deletar(ficha: CharacterSheet)
}
