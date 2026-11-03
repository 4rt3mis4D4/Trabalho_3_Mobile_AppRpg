package com.example.app_rpg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_rpg.ui.theme.*
import com.example.app_rpg.data.local.Musica
import com.example.app_rpg.data.local.generosMusica
import com.example.app_rpg.viewmodel.MusicaUiState
import com.example.app_rpg.viewmodel.MusicaViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun Playlist(modifier: Modifier = Modifier, viewModel: MusicaViewModel) {
    val palette = LocalModePalette.current
    val estadoScroll = rememberLazyListState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val musicasFiltradas = uiState.musicasFiltradas
    val musicaAtual = uiState.musicaAtual

    // Column externa somente para separar
    // a área rolável da barra de reprodução.
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(corPretoPuro)
    ) {

        //Scroll
        LazyColumn(
            state = estadoScroll,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),

            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = 16.dp
            ),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Título
            item {
                Text(
                    text = "Playlist",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = corBrancoPuro
                )
            }

            // Pesquisa
            item {
                TextField(
                    value = uiState.pesquisa,
                    onValueChange = viewModel::atualizarPesquisa,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Pesquisar música ou artista")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = corCinzaEscuro,
                        unfocusedContainerColor = corCinzaEscuro,
                        focusedTextColor = corBrancoPuro,
                        unfocusedTextColor = corBrancoPuro,
                        focusedPlaceholderColor = corCinzaMedio,
                        unfocusedPlaceholderColor = corCinzaMedio
                    )
                )
            }

            // Filtros Pop, Rock e MPB
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    generosMusica.forEach { genero ->
                        FilterChip(
                            selected = uiState.generoSelecionado == genero,
                            onClick = { viewModel.alternarFiltroGenero(genero) },
                            label = { Text(genero) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = corCinzaEscuro,
                                labelColor = corBrancoPuro,
                                selectedContainerColor = palette.accent,
                                selectedLabelColor = corPretoPuro
                            )
                        )
                    }
                }
            }

            // Botão adicionar música
            item {
                Button(
                    onClick = { viewModel.abrirFormulario() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accent
                    )
                ) {
                    Text("+ Adicionar música")
                }
            }

            // Mensagem se nenhuma música for encontrada
            if (!uiState.carregando && musicasFiltradas.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma música encontrada.",
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = corCinzaMedio
                    )
                }
            }

            // Cards das músicas
            items(
                items = musicasFiltradas,
                key = { it.id }
            ) { musica ->

                MusicaCard(
                    musica = musica,

                    tocando = uiState.musicaAtualId == musica.id &&
                            uiState.estaTocando,

                    pausada = uiState.musicaAtualId == musica.id &&
                            !uiState.estaTocando,

                    onTocar = { viewModel.tocar(musica) },

                    onEditar = { viewModel.abrirFormulario(musica) },

                    onExcluir = { viewModel.pedirExclusao(musica) }
                )
            }
        }

        // Barra fixa de reprodução simulada
        if (musicaAtual != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = corCinzaEscuro
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = if (uiState.estaTocando) {
                            "♫ Tocando agora (simulação)"
                        } else {
                            "⏸ Música pausada (simulação)"
                        },
                        color = palette.accent,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = musicaAtual.titulo,
                        color = corBrancoPuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = musicaAtual.artista,
                        color = corCinzaMedio
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = viewModel::alternarReproducao,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accent
                            )
                        ) {
                            Text(
                                if (uiState.estaTocando) {
                                    "Pausar"
                                } else {
                                    "Continuar"
                                }
                            )
                        }

                        TextButton(
                            onClick = viewModel::pararReproducao
                        ) {
                            Text(
                                "Parar",
                                color = corBrancoPuro
                            )
                        }
                    }
                }
            }
        }
    }

    // Formulário de cadastro e edição
    if (uiState.formularioAberto) {
        FormularioMusica(
            uiState = uiState,
            onTituloChange = viewModel::atualizarFormTitulo,
            onArtistaChange = viewModel::atualizarFormArtista,
            onGeneroChange = viewModel::atualizarFormGenero,
            onCancelar = viewModel::fecharFormulario,
            onSalvar = viewModel::salvarMusica
        )
    }

    // Confirmação de exclusão
    uiState.musicaParaExcluir?.let { musica ->

        AlertDialog(
            onDismissRequest = viewModel::cancelarExclusao,

            title = {
                Text("Excluir música?")
            },

            text = {
                Text("Deseja excluir '${musica.titulo}'?")
            },

            confirmButton = {
                TextButton(
                    onClick = { viewModel.excluirMusica(musica) }
                ) {
                    Text("Excluir")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = viewModel::cancelarExclusao
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MusicaCard(
    musica: Musica,
    tocando: Boolean,
    pausada: Boolean,
    onTocar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val palette = LocalModePalette.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = corCinzaEscuro)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(palette.soft, RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("♫", fontSize = 28.sp, color = corBrancoPuro)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(musica.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = corBrancoPuro)
                    Text(musica.artista, color = corCinzaMedio)
                    Text("Gênero: ${musica.genero}", color = corBrancoPuro)
                    if (tocando || pausada) {
                        Text(if (tocando) "Tocando (simulação)" else "Pausada", color = palette.accent)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTocar,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent)
                ) { Text(if (tocando) "Pausar" else if (pausada) "Continuar" else "Tocar") }
                TextButton(onClick = onEditar) { Text("Editar", color = corBrancoPuro) }
                TextButton(onClick = onExcluir) { Text("Excluir", color = corBrancoPuro) }
            }
        }
    }
}

@Composable
private fun FormularioMusica(
    uiState: MusicaUiState,
    onTituloChange: (String) -> Unit,
    onArtistaChange: (String) -> Unit,
    onGeneroChange: (String) -> Unit,
    onCancelar: () -> Unit,
    onSalvar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (uiState.musicaEmEdicaoId == null) "Adicionar música" else "Editar música") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.formTitulo,
                    onValueChange = onTituloChange,
                    label = { Text("Nome da música") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.formArtista,
                    onValueChange = onArtistaChange,
                    label = { Text("Artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Gênero")
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    generosMusica.forEach { opcao ->
                        FilterChip(
                            selected = uiState.formGenero == opcao,
                            onClick = { onGeneroChange(opcao) },
                            label = { Text(opcao) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = uiState.formularioValido,
                onClick = onSalvar
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
