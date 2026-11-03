package com.example.app_rpg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_rpg.data.local.Musica
import com.example.app_rpg.data.local.generosMusica
import com.example.app_rpg.data.repository.MusicaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MusicaUiState(
    val musicas: List<Musica> = emptyList(),
    val carregando: Boolean = true,
    val pesquisa: String = "",
    val generoSelecionado: String? = null,
    val formularioAberto: Boolean = false,
    val musicaEmEdicaoId: Int? = null,
    val formTitulo: String = "",
    val formArtista: String = "",
    val formGenero: String = generosMusica.first(),
    val musicaParaExcluirId: Int? = null,
    val musicaAtualId: Int? = null,
    val estaTocando: Boolean = false
) {
    val musicasFiltradas: List<Musica>
        get() = musicas.filter { musica ->
            (generoSelecionado == null || musica.genero == generoSelecionado) &&
                    (pesquisa.isBlank() ||
                            musica.titulo.contains(pesquisa.trim(), ignoreCase = true) ||
                            musica.artista.contains(pesquisa.trim(), ignoreCase = true))
        }

    val musicaAtual: Musica?
        get() = musicas.firstOrNull { it.id == musicaAtualId }

    val musicaParaExcluir: Musica?
        get() = musicas.firstOrNull { it.id == musicaParaExcluirId }

    val formularioValido: Boolean
        get() = formTitulo.isNotBlank() && formArtista.isNotBlank()
}

class MusicaViewModel(private val repository: MusicaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicaUiState())
    val uiState: StateFlow<MusicaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(tratadorDeErro("Erro ao buscar músicas")) {
            repository.buscarTodos().collect { musicas ->
                _uiState.update { it.copy(musicas = musicas, carregando = false) }
            }
        }
    }

    fun atualizarPesquisa(texto: String) {
        _uiState.update { it.copy(pesquisa = texto) }
    }

    fun alternarFiltroGenero(genero: String) {
        _uiState.update {
            it.copy(generoSelecionado = if (it.generoSelecionado == genero) null else genero)
        }
    }

    fun abrirFormulario(musica: Musica? = null) {
        _uiState.update {
            it.copy(
                formularioAberto = true,
                musicaEmEdicaoId = musica?.id,
                formTitulo = musica?.titulo.orEmpty(),
                formArtista = musica?.artista.orEmpty(),
                formGenero = musica?.genero ?: generosMusica.first()
            )
        }
    }

    fun fecharFormulario() {
        _uiState.update { it.copy(formularioAberto = false) }
    }

    fun atualizarFormTitulo(titulo: String) {
        _uiState.update { it.copy(formTitulo = titulo) }
    }

    fun atualizarFormArtista(artista: String) {
        _uiState.update { it.copy(formArtista = artista) }
    }

    fun atualizarFormGenero(genero: String) {
        _uiState.update { it.copy(formGenero = genero) }
    }

    fun salvarMusica() {
        val estado = _uiState.value
        if (!estado.formularioValido) return

        val musica = Musica(
            id = estado.musicaEmEdicaoId ?: 0,
            titulo = estado.formTitulo.trim(),
            artista = estado.formArtista.trim(),
            genero = estado.formGenero
        )
        fecharFormulario()

        viewModelScope.launch(tratadorDeErro("Erro ao salvar música")) {
            if (musica.id == 0) repository.inserir(musica) else repository.atualizar(musica)
        }
    }

    fun pedirExclusao(musica: Musica) {
        _uiState.update { it.copy(musicaParaExcluirId = musica.id) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(musicaParaExcluirId = null) }
    }

    fun excluirMusica(musica: Musica) {
        _uiState.update { estado ->
            val eraAtual = estado.musicaAtualId == musica.id
            estado.copy(
                musicaParaExcluirId = null,
                musicaAtualId = if (eraAtual) null else estado.musicaAtualId,
                estaTocando = if (eraAtual) false else estado.estaTocando
            )
        }

        viewModelScope.launch(tratadorDeErro("Erro ao excluir música")) {
            repository.deletar(musica)
        }
    }

    fun tocar(musica: Musica) {
        _uiState.update {
            if (it.musicaAtualId == musica.id) {
                it.copy(estaTocando = !it.estaTocando)
            } else {
                it.copy(musicaAtualId = musica.id, estaTocando = true)
            }
        }
    }

    fun alternarReproducao() {
        _uiState.update { it.copy(estaTocando = !it.estaTocando) }
    }

    fun pararReproducao() {
        _uiState.update { it.copy(musicaAtualId = null, estaTocando = false) }
    }
}

class MusicaViewModelFactory(
    private val repository: MusicaRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicaViewModel(repository) as T
    }
}
