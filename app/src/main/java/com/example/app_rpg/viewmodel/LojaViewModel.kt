package com.example.app_rpg.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_rpg.data.local.ItemRpg
import com.example.app_rpg.data.local.LojaRpg
import com.example.app_rpg.data.repository.ItemRepository
import com.example.app_rpg.data.repository.LojaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LojaUiState(
    val lojas: List<LojaRpg> = emptyList(),
    val itensDisponiveis: List<ItemRpg> = emptyList(),
    val carregando: Boolean = true,
    val lojaParaExcluir: LojaRpg? = null,
    val lojaAtualId: Int? = null,
    val emEdicaoId: Int? = null,
    val nome: String = "",
    val descricao: String = "",
    val visivel: Boolean = true,
    val itemIds: Set<Int> = emptySet(),
    val imagemUri: Uri? = null,
    val erroNome: Boolean = false
) {
    val lojaAtual: LojaRpg?
        get() = lojas.firstOrNull { it.id == lojaAtualId }

    // Vem das duas listas observadas, então acompanha sozinho qualquer mudança nas tabelas
    val itensDaLojaAtual: List<ItemRpg>
        get() = lojaAtual?.let { loja -> itensDisponiveis.filter { it.id in loja.itemIds } }.orEmpty()

    fun lojasExibidas(isMaster: Boolean): List<LojaRpg> =
        if (isMaster) lojas else lojas.filter { it.visivel }
}

class LojaViewModel(
    private val lojaRepository: LojaRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LojaUiState())
    val uiState: StateFlow<LojaUiState> = _uiState.asStateFlow()

    private var formKey: String? = null

    init {
        viewModelScope.launch(tratadorDeErro("Erro ao buscar lojas")) {
            lojaRepository.buscarTodos().collect { lojas ->
                _uiState.update { it.copy(lojas = lojas, carregando = false) }
            }
        }
        viewModelScope.launch(tratadorDeErro("Erro ao buscar itens das lojas")) {
            itemRepository.buscarTodos().collect { itens ->
                _uiState.update { it.copy(itensDisponiveis = itens) }
            }
        }
    }

    fun pedirExclusao(loja: LojaRpg) {
        _uiState.update { it.copy(lojaParaExcluir = loja) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(lojaParaExcluir = null) }
    }

    fun excluirLoja(loja: LojaRpg) {
        cancelarExclusao()
        viewModelScope.launch(tratadorDeErro("Erro ao excluir loja")) {
            lojaRepository.deletar(loja)
        }
    }

    fun alternarVisibilidade(loja: LojaRpg) {
        viewModelScope.launch(tratadorDeErro("Erro ao alterar visibilidade da loja")) {
            lojaRepository.atualizar(loja.copy(visivel = !loja.visivel))
        }
    }

    fun selecionarLoja(id: Int) {
        _uiState.update { it.copy(lojaAtualId = id) }
    }

    fun removerItemDaLoja(item: ItemRpg) {
        val loja = _uiState.value.lojaAtual ?: return
        viewModelScope.launch(tratadorDeErro("Erro ao remover item da loja")) {
            lojaRepository.atualizar(loja.copy(itemIds = loja.itemIds - item.id))
        }
    }

    // A chave garante que o formulário só é preenchido uma vez por abertura,
    // para que girar o celular não apague o que foi digitado
    fun abrirFormulario(chave: String, lojaId: Int?) {
        if (formKey == chave) return
        formKey = chave
        preencherFormulario(null, lojaId)
        if (lojaId == null) return

        viewModelScope.launch(tratadorDeErro("Erro ao buscar loja $lojaId")) {
            val loja = lojaRepository.buscarPorId(lojaId)
            preencherFormulario(loja, loja?.id)
        }
    }

    private fun preencherFormulario(loja: LojaRpg?, emEdicaoId: Int?) {
        _uiState.update {
            it.copy(
                emEdicaoId = emEdicaoId,
                nome = loja?.nome.orEmpty(),
                descricao = loja?.descricao.orEmpty(),
                visivel = loja?.visivel ?: true,
                itemIds = loja?.itemIds.orEmpty(),
                imagemUri = loja?.imagemUri,
                erroNome = false
            )
        }
    }

    fun atualizarNome(nome: String) {
        _uiState.update { it.copy(nome = nome, erroNome = it.erroNome && nome.isBlank()) }
    }

    fun atualizarDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao) }
    }

    fun atualizarVisivel(visivel: Boolean) {
        _uiState.update { it.copy(visivel = visivel) }
    }

    fun atualizarImagem(uri: Uri) {
        _uiState.update { it.copy(imagemUri = uri) }
    }

    fun alternarItem(id: Int) {
        _uiState.update {
            it.copy(itemIds = if (id in it.itemIds) it.itemIds - id else it.itemIds + id)
        }
    }

    // Retorna false quando o formulário é inválido, e a tela continua aberta
    fun salvarLoja(): Boolean {
        val estado = _uiState.value
        if (estado.nome.isBlank()) {
            _uiState.update { it.copy(erroNome = true) }
            return false
        }

        val loja = LojaRpg(
            id = estado.emEdicaoId ?: 0,
            nome = estado.nome.trim(),
            descricao = estado.descricao.trim(),
            imagemUri = estado.imagemUri,
            visivel = estado.visivel,
            itemIds = estado.itemIds
        )

        viewModelScope.launch(tratadorDeErro("Erro ao salvar loja")) {
            if (loja.id == 0) lojaRepository.inserir(loja) else lojaRepository.atualizar(loja)
        }
        return true
    }
}

class LojaViewModelFactory(
    private val lojaRepository: LojaRepository,
    private val itemRepository: ItemRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LojaViewModel(lojaRepository, itemRepository) as T
    }
}
