package com.example.app_rpg.data.repository

import com.example.app_rpg.data.local.LojaDao
import com.example.app_rpg.data.local.LojaRpg
import kotlinx.coroutines.flow.Flow

class LojaRepository(private val lojaDao: LojaDao) {

    fun buscarTodos(): Flow<List<LojaRpg>> = lojaDao.buscarTodos()

    suspend fun buscarPorId(id: Int): LojaRpg? = lojaDao.buscarPorId(id)

    suspend fun inserir(loja: LojaRpg): Long = lojaDao.inserir(loja)

    suspend fun atualizar(loja: LojaRpg) = lojaDao.atualizar(loja)

    suspend fun deletar(loja: LojaRpg) = lojaDao.deletar(loja)
}
