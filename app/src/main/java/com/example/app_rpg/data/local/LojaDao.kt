package com.example.app_rpg.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LojaDao {

    @Insert
    suspend fun inserir(loja: LojaRpg): Long

    @Query("SELECT * FROM lojas ORDER BY id")
    fun buscarTodos(): Flow<List<LojaRpg>>

    @Query("SELECT * FROM lojas ORDER BY id")
    suspend fun listarTodas(): List<LojaRpg>

    @Update
    suspend fun atualizar(loja: LojaRpg)

    @Delete
    suspend fun deletar(loja: LojaRpg)

    @Query("SELECT * FROM lojas WHERE id = :id")
    suspend fun buscarPorId(id: Int): LojaRpg?
}
