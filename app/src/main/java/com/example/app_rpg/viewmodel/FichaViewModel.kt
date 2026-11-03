package com.example.app_rpg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_rpg.data.local.CharacterSheet
import com.example.app_rpg.data.repository.FichaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FichaUiState(
    val ficha: CharacterSheet = CharacterSheet(),
    val rascunho: CharacterSheet = CharacterSheet(),
    val carregando: Boolean = true
)

private fun <T> List<T>.replaceAt(index: Int, value: T) =
    toMutableList().also { it[index] = value }

class FichaViewModel(private val repository: FichaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FichaUiState())
    val uiState: StateFlow<FichaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(tratadorDeErro("Erro ao buscar ficha")) {
            repository.buscarTodos().collect { fichas ->
                val ficha = fichas.firstOrNull() ?: CharacterSheet()
                _uiState.update { estado ->
                    // Um novo valor do banco não pode apagar o que ainda está sendo digitado
                    val semAlteracoes = estado.carregando || estado.rascunho == estado.ficha
                    estado.copy(
                        ficha = ficha,
                        rascunho = if (semAlteracoes) ficha else estado.rascunho,
                        carregando = false
                    )
                }
            }
        }
    }

    private fun atualizarRascunho(alterar: (CharacterSheet) -> CharacterSheet) {
        _uiState.update { it.copy(rascunho = alterar(it.rascunho)) }
    }

    fun atualizarNome(nome: String) = atualizarRascunho { it.copy(name = nome) }

    fun atualizarLevel(level: String) = atualizarRascunho {
        it.copy(level = level.filter(Char::isDigit).take(2))
    }

    fun atualizarClasse(classe: String) = atualizarRascunho { it.copy(characterClass = classe) }

    fun atualizarRaca(raca: String) = atualizarRascunho { it.copy(race = raca) }

    fun atualizarAtributo(index: Int, valor: String) = atualizarRascunho { ficha ->
        val atributo = ficha.attributes[index].copy(valor = valor.filter(Char::isDigit).take(2))
        ficha.copy(attributes = ficha.attributes.replaceAt(index, atributo))
    }

    fun atualizarStatus(index: Int, valor: String) = atualizarRascunho { ficha ->
        val status = ficha.status[index].copy(valor = valor.take(4))
        ficha.copy(status = ficha.status.replaceAt(index, status))
    }

    fun alternarProficiencia(index: Int) = atualizarRascunho { ficha ->
        val pericia = ficha.skills[index]
        val alterada = pericia.copy(proficiencia = (pericia.proficiencia + 1) % 3)
        ficha.copy(skills = ficha.skills.replaceAt(index, alterada))
    }

    fun atualizarBonusPericia(index: Int, bonus: String) = atualizarRascunho { ficha ->
        val valido = bonus.filterIndexed { i, c -> c.isDigit() || (i == 0 && c == '-') }.take(3)
        ficha.copy(skills = ficha.skills.replaceAt(index, ficha.skills[index].copy(bonus = valido)))
    }

    fun atualizarNomePericia(index: Int, nome: String) = atualizarRascunho { ficha ->
        ficha.copy(skills = ficha.skills.replaceAt(index, ficha.skills[index].copy(nome = nome)))
    }

    fun cancelarEdicao() {
        _uiState.update { it.copy(rascunho = it.ficha) }
    }

    fun salvarFicha() {
        val rascunho = _uiState.value.rascunho
        viewModelScope.launch(tratadorDeErro("Erro ao salvar ficha")) {
            val salva = if (rascunho.id == 0) {
                rascunho.copy(id = repository.inserir(rascunho).toInt())
            } else {
                repository.atualizar(rascunho)
                rascunho
            }
            _uiState.update { estado ->
                estado.copy(
                    ficha = salva,
                    rascunho = if (estado.rascunho == rascunho) salva else estado.rascunho.copy(id = salva.id)
                )
            }
        }
    }
}

class FichaViewModelFactory(
    private val repository: FichaRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FichaViewModel(repository) as T
    }
}
