package com.example.app_rpg.data.repository

import com.example.app_rpg.data.local.Musica
import com.example.app_rpg.data.local.MusicaDao
import kotlinx.coroutines.flow.Flow

class MusicaRepository(private val musicaDao: MusicaDao) {

    fun buscarTodos(): Flow<List<Musica>> = musicaDao.buscarTodos()

    suspend fun inserir(musica: Musica): Long = musicaDao.inserir(musica)

    suspend fun atualizar(musica: Musica) = musicaDao.atualizar(musica)

    suspend fun deletar(musica: Musica) = musicaDao.deletar(musica)
}
