package com.example.app_rpg.data.repository

import com.example.app_rpg.data.local.CharacterSheet
import com.example.app_rpg.data.local.FichaDao
import kotlinx.coroutines.flow.Flow

class FichaRepository(private val fichaDao: FichaDao) {

    fun buscarTodos(): Flow<List<CharacterSheet>> = fichaDao.buscarTodos()

    suspend fun inserir(ficha: CharacterSheet): Long = fichaDao.inserir(ficha)

    suspend fun atualizar(ficha: CharacterSheet) = fichaDao.atualizar(ficha)

    suspend fun deletar(ficha: CharacterSheet) = fichaDao.deletar(ficha)
}
