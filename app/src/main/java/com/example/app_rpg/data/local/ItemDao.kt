package com.example.app_rpg.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert
    suspend fun inserir(item: ItemRpg): Long

    @Query("SELECT * FROM itens ORDER BY id")
    fun buscarTodos(): Flow<List<ItemRpg>>

    @Update
    suspend fun atualizar(item: ItemRpg)

    @Delete
    suspend fun deletar(item: ItemRpg)

    @Query("SELECT * FROM itens WHERE id = :id")
    suspend fun buscarPorId(id: Int): ItemRpg?
}
