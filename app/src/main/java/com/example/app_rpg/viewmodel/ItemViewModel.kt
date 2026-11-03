package com.example.app_rpg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_rpg.data.local.ItemRpg
import com.example.app_rpg.data.local.listCatItem
import com.example.app_rpg.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemUiState(
    val itens: List<ItemRpg> = emptyList(),
    val carregando: Boolean = true,
    val itemParaExcluir: ItemRpg? = null,
    val emEdicaoId: Int? = null,
    val nome: String = "",
    val valor: String = "",
    val descricao: String = "",
    val categoria: Int = 0,
    val tentouSalvar: Boolean = false
) {
    val nomeInvalido: Boolean
        get() = tentouSalvar && nome.isBlank()

    val valorInvalido: Boolean
        get() = tentouSalvar && valor.isBlank()
}

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemUiState())
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    private var formKey: String? = null

    init {
        viewModelScope.launch(tratadorDeErro("Erro ao buscar itens")) {
            repository.buscarTodos().collect { itens ->
                _uiState.update { it.copy(itens = itens, carregando = false) }
            }
        }
    }

    fun pedirExclusao(item: ItemRpg) {
        _uiState.update { it.copy(itemParaExcluir = item) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(itemParaExcluir = null) }
    }

    fun excluirItem(item: ItemRpg) {
        cancelarExclusao()
        viewModelScope.launch(tratadorDeErro("Erro ao excluir item")) {
            repository.deletar(item)
        }
    }

    // A chave garante que o formulário só é preenchido uma vez por abertura,
    // para que girar o celular não apague o que foi digitado
    fun abrirFormulario(chave: String, itemId: Int?) {
        if (formKey == chave) return
        formKey = chave
        preencherFormulario(null, itemId)
        if (itemId == null) return

        viewModelScope.launch(tratadorDeErro("Erro ao buscar item $itemId")) {
            val item = repository.buscarPorId(itemId)
            preencherFormulario(item, item?.id)
        }
    }

    private fun preencherFormulario(item: ItemRpg?, emEdicaoId: Int?) {
        _uiState.update {
            it.copy(
                emEdicaoId = emEdicaoId,
                nome = item?.itemName.orEmpty(),
                valor = item?.itemValue?.toString().orEmpty(),
                descricao = item?.itemDescription.orEmpty(),
                categoria = listCatItem.indexOf(item?.itemCat).coerceAtLeast(0),
                tentouSalvar = false
            )
        }
    }

    fun atualizarNome(nome: String) {
        _uiState.update { it.copy(nome = nome) }
    }

    fun atualizarValor(valor: String) {
        _uiState.update { it.copy(valor = valor.filter(Char::isDigit).take(9)) }
    }

    fun atualizarDescricao(descricao: String) {
        _uiState.update { it.copy(descricao = descricao) }
    }

    fun atualizarCategoria(categoria: Int) {
        _uiState.update { it.copy(categoria = categoria) }
    }

    // Retorna false quando o formulário é inválido, e a tela continua aberta
    fun salvarItem(): Boolean {
        _uiState.update { it.copy(tentouSalvar = true) }

        val estado = _uiState.value
        val valor = estado.valor.toIntOrNull()
        if (estado.nome.isBlank() || valor == null) return false

        val item = ItemRpg(
            id = estado.emEdicaoId ?: 0,
            itemName = estado.nome.trim(),
            itemValue = valor,
            itemDescription = estado.descricao.trim(),
            itemCat = listCatItem[estado.categoria]
        )

        viewModelScope.launch(tratadorDeErro("Erro ao salvar item")) {
            if (item.id == 0) repository.inserir(item) else repository.atualizar(item)
        }
        return true
    }
}

class ItemViewModelFactory(
    private val repository: ItemRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemViewModel(repository) as T
    }
}
