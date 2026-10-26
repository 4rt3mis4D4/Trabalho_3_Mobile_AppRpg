package com.example.app_rpg

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FichaDao {

    @Insert
    suspend fun inserir(ficha: CharacterSheet): Long

    @Query("SELECT * FROM fichas ORDER BY id")
    suspend fun buscarTodos(): List<CharacterSheet>

    @Update
    suspend fun atualizar(ficha: CharacterSheet)

    @Delete
    suspend fun deletar(ficha: CharacterSheet)
}
