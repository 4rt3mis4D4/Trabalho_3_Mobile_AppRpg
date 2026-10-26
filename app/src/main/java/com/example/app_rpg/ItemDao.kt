package com.example.app_rpg

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {

    @Insert
    suspend fun inserir(item: ItemRpg): Long

    @Query("SELECT * FROM itens ORDER BY id")
    suspend fun buscarTodos(): List<ItemRpg>

    @Update
    suspend fun atualizar(item: ItemRpg)

    @Delete
    suspend fun deletar(item: ItemRpg)

    @Query("SELECT * FROM itens WHERE id = :id")
    suspend fun buscarPorId(id: Int): ItemRpg?

    @Query("SELECT * FROM itens WHERE id IN (:ids) ORDER BY id")
    suspend fun buscarPorIds(ids: List<Int>): List<ItemRpg>
}
