package com.example.app_rpg.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app_rpg.data.dice.DiceStats
import com.example.app_rpg.data.dice.distribution
import com.example.app_rpg.data.dice.parseDiceExpression
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DiceUiState(
    val expressao: String = "2d6 + 3",
    val stats: DiceStats? = null,
    val erro: String? = null
)

class DiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    fun atualizarExpressao(expressao: String) {
        _uiState.update { it.copy(expressao = expressao) }
    }

    fun calcular() {
        parseDiceExpression(_uiState.value.expressao)
            .onSuccess { expression ->
                val stats = DiceStats(expression, expression.distribution())
                _uiState.update { it.copy(stats = stats, erro = null) }
            }
            .onFailure { failure ->
                _uiState.update { it.copy(stats = null, erro = failure.message) }
            }
    }
}
