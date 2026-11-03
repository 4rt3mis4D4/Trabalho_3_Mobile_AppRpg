package com.example.app_rpg.data.repository

import androidx.room.withTransaction
import com.example.app_rpg.data.local.AppDatabase
import com.example.app_rpg.data.local.ItemDao
import com.example.app_rpg.data.local.ItemRpg
import com.example.app_rpg.data.local.LojaDao
import kotlinx.coroutines.flow.Flow

class ItemRepository(
    private val itemDao: ItemDao,
    private val lojaDao: LojaDao,
    private val banco: AppDatabase
) {

    fun buscarTodos(): Flow<List<ItemRpg>> = itemDao.buscarTodos()

    suspend fun buscarPorId(id: Int): ItemRpg? = itemDao.buscarPorId(id)

    suspend fun inserir(item: ItemRpg): Long = itemDao.inserir(item)

    suspend fun atualizar(item: ItemRpg) = itemDao.atualizar(item)

    // Excluir um item também o remove de todas as lojas que o vendiam
    suspend fun deletar(item: ItemRpg) {
        banco.withTransaction {
            itemDao.deletar(item)
            lojaDao.listarTodas()
                .filter { item.id in it.itemIds }
                .forEach { lojaDao.atualizar(it.copy(itemIds = it.itemIds - item.id)) }
        }
    }
}
