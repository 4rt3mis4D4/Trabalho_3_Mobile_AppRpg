package com.example.app_rpg

import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MeuViewModel : ViewModel() {

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

    fun salvarFicha() {
        characterSheet = characterDraft
    }

    private val _itens = mutableStateListOf<ItemRpg>()
    val itens: List<ItemRpg> = _itens

    var itemParaExcluir by mutableStateOf<ItemRpg?>(null)

    fun excluirItem(item: ItemRpg) {
        _itens.removeAll { it.id == item.id }
        _lojas.replaceAll { it.copy(itemIds = it.itemIds - item.id) }
    }

    fun salvarItem(item: ItemRpg) {
        val index = _itens.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            _itens[index] = item
        } else {
            _itens.add(item.copy(id = (_itens.maxOfOrNull { it.id } ?: 0) + 1))
        }
    }

    private val _lojas = mutableStateListOf<LojaRpg>()
    val lojas: List<LojaRpg> = _lojas

    var lojaParaExcluir by mutableStateOf<LojaRpg?>(null)

    fun excluirLoja(loja: LojaRpg) {
        _lojas.removeAll { it.id == loja.id }
    }

    fun alternarVisibilidadeLoja(loja: LojaRpg) {
        val indice = _lojas.indexOfFirst { it.id == loja.id }
        if (indice >= 0) {
            _lojas[indice] = loja.copy(visivel = !loja.visivel)
        }
    }

    fun itensDaLoja(loja: LojaRpg?): List<ItemRpg> =
        _itens.filter { loja != null && it.id in loja.itemIds }

    fun removerItemDaLoja(lojaId: Int?, item: ItemRpg) {
        val index = _lojas.indexOfFirst { it.id == lojaId }
        if (index >= 0) {
            _lojas[index] = _lojas[index].copy(itemIds = _lojas[index].itemIds - item.id)
        }
    }

    fun salvarLoja(loja: LojaRpg) {
        val indice = _lojas.indexOfFirst { it.id == loja.id }
        if (indice >= 0) {
            _lojas[indice] = loja
        } else {
            _lojas.add(loja.copy(id = (_lojas.maxOfOrNull { it.id } ?: 0) + 1))
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