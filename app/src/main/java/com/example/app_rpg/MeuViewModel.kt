package com.example.app_rpg

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MeuViewModel"

class MeuViewModel(application: Application) : AndroidViewModel(application) {

    private val banco by lazy { AppDatabase.getDatabase(application) }
    private val itemDao by lazy { banco.itemDao() }
    private val lojaDao by lazy { banco.lojaDao() }
    private val fichaDao by lazy { banco.fichaDao() }
    private val musicaDao by lazy { banco.musicaDao() }

    private fun executarNoBanco(mensagemErro: String, operacao: suspend () -> Unit): Job =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                operacao()
            } catch (e: Exception) {
                Log.e(TAG, mensagemErro, e)
            }
        }

    val expressionState = TextFieldState("2d6 + 3")

    var diceStats by mutableStateOf<DiceStats?>(null)
        private set

    var diceError by mutableStateOf<String?>(null)
        private set

    fun calcularDados() {
        parseDiceExpression(expressionState.text.toString())
            .onSuccess { expression ->
                diceStats = DiceStats(expression, expression.distribution())
                diceError = null
            }
            .onFailure { failure ->
                diceStats = null
                diceError = failure.message
            }
    }

    var characterSheet by mutableStateOf(CharacterSheet())
        private set

    var characterDraft by mutableStateOf(CharacterSheet())
        private set

    fun atualizarRascunhoFicha(novo: CharacterSheet) {
        characterDraft = novo
    }

    fun cancelarEdicaoFicha() {
        characterDraft = characterSheet
    }

    private var fichaCarregada = false

    suspend fun carregarFicha() {
        if (fichaCarregada) return
        executarNoBanco("Erro ao buscar ficha") {
            val ficha = fichaDao.buscarTodos().firstOrNull() ?: CharacterSheet()
            withContext(Dispatchers.Main) {
                characterSheet = ficha
                characterDraft = ficha
                fichaCarregada = true
            }
        }.join()
    }

    fun salvarFicha() {
        val rascunho = characterDraft
        executarNoBanco("Erro ao salvar ficha") {
            val ficha = if (rascunho.id == 0) {
                rascunho.copy(id = fichaDao.inserir(rascunho).toInt())
            } else {
                fichaDao.atualizar(rascunho)
                rascunho
            }
            withContext(Dispatchers.Main) {
                characterSheet = ficha
                characterDraft = ficha
            }
        }
    }

    private val _itens = mutableStateListOf<ItemRpg>()
    val itens: List<ItemRpg> = _itens

    var itemParaExcluir by mutableStateOf<ItemRpg?>(null)

    suspend fun carregarItens() {
        executarNoBanco("Erro ao buscar itens") { recarregarItens() }.join()
    }

    private suspend fun recarregarItens() {
        val todos = itemDao.buscarTodos()
        withContext(Dispatchers.Main) {
            _itens.clear()
            _itens.addAll(todos)
        }
    }

    fun excluirItem(item: ItemRpg) {
        executarNoBanco("Erro ao excluir item") {
            banco.withTransaction {
                itemDao.deletar(item)
                lojaDao.buscarTodos()
                    .filter { item.id in it.itemIds }
                    .forEach { lojaDao.atualizar(it.copy(itemIds = it.itemIds - item.id)) }
            }
            recarregarItens()
            recarregarLojas()
        }
    }

    fun salvarItem(item: ItemRpg) {
        executarNoBanco("Erro ao salvar item") {
            if (item.id == 0) itemDao.inserir(item) else itemDao.atualizar(item)
            recarregarItens()
        }
    }

    private val _lojas = mutableStateListOf<LojaRpg>()
    val lojas: List<LojaRpg> = _lojas

    var lojaParaExcluir by mutableStateOf<LojaRpg?>(null)

    suspend fun carregarLojas() {
        executarNoBanco("Erro ao buscar lojas") { recarregarLojas() }.join()
    }

    private suspend fun recarregarLojas() {
        val todas = lojaDao.buscarTodos()
        withContext(Dispatchers.Main) {
            _lojas.clear()
            _lojas.addAll(todas)
        }
    }

    fun excluirLoja(loja: LojaRpg) {
        executarNoBanco("Erro ao excluir loja") {
            lojaDao.deletar(loja)
            recarregarLojas()
        }
    }

    fun alternarVisibilidadeLoja(loja: LojaRpg) {
        executarNoBanco("Erro ao alterar visibilidade da loja") {
            lojaDao.atualizar(loja.copy(visivel = !loja.visivel))
            recarregarLojas()
        }
    }

    fun itensDaLoja(loja: LojaRpg?): List<ItemRpg> =
        _itens.filter { loja != null && it.id in loja.itemIds }

    fun removerItemDaLoja(lojaId: Int?, item: ItemRpg) {
        val loja = _lojas.firstOrNull { it.id == lojaId } ?: return
        executarNoBanco("Erro ao remover item da loja") {
            lojaDao.atualizar(loja.copy(itemIds = loja.itemIds - item.id))
            recarregarLojas()
        }
    }

    fun salvarLoja(loja: LojaRpg) {
        executarNoBanco("Erro ao salvar loja") {
            if (loja.id == 0) lojaDao.inserir(loja) else lojaDao.atualizar(loja)
            recarregarLojas()
        }
    }

    private val _musicas = mutableStateListOf<Musica>()
    val musicas: List<Musica> = _musicas

    suspend fun carregarMusicas() {
        executarNoBanco("Erro ao buscar músicas") { recarregarMusicas() }.join()
    }

    private suspend fun recarregarMusicas() {
        val todas = musicaDao.buscarTodos()
        withContext(Dispatchers.Main) {
            _musicas.clear()
            _musicas.addAll(todas)
        }
    }

    fun salvarMusica(musica: Musica) {
        executarNoBanco("Erro ao salvar música") {
            if (musica.id == 0) musicaDao.inserir(musica) else musicaDao.atualizar(musica)
            recarregarMusicas()
        }
    }

    fun excluirMusica(musica: Musica) {
        executarNoBanco("Erro ao excluir música") {
            musicaDao.deletar(musica)
            recarregarMusicas()
        }
    }

    private var itemFormKey: String? = null

    var itemNome by mutableStateOf("")
    var itemValor by mutableStateOf("")
    var itemDescricao by mutableStateOf("")
    var itemCategoria by mutableIntStateOf(0)
    var itemSalvar by mutableStateOf(false)

    fun iniciarFormularioItem(chave: String, item: ItemRpg?) {
        if (itemFormKey == chave) return
        itemFormKey = chave
        itemNome = item?.itemName ?: ""
        itemValor = item?.itemValue?.toString() ?: ""
        itemDescricao = item?.itemDescription ?: ""
        itemCategoria = listCatItem.indexOf(item?.itemCat).coerceAtLeast(0)
        itemSalvar = false
    }

    private var lojaFormKey: String? = null

    var lojaNome by mutableStateOf("")
    var lojaDescricao by mutableStateOf("")
    var lojaVisivel by mutableStateOf(true)
    var lojaItemIds by mutableStateOf<Set<Int>>(emptySet())
    var lojaImagemUri by mutableStateOf<Uri?>(null)
    var lojaErroNome by mutableStateOf(false)

    fun iniciarFormularioLoja(chave: String, loja: LojaRpg?) {
        if (lojaFormKey == chave) return
        lojaFormKey = chave
        lojaNome = loja?.nome ?: ""
        lojaDescricao = loja?.descricao ?: ""
        lojaVisivel = loja?.visivel ?: true
        lojaItemIds = loja?.itemIds.orEmpty()
        lojaImagemUri = loja?.imagemUri
        lojaErroNome = false
    }
}