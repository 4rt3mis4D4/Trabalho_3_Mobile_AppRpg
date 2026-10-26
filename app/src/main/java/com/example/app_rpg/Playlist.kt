package com.example.app_rpg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.app_rpg.ui.theme.*

private val generos = listOf("Pop", "Rock", "MPB")

@Entity(tableName = "musicas")
data class Musica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val artista: String,
    val genero: String
)

// Simulação em memória, substituir pelo banco depois.
private object PlaylistEmMemoria {
    val musicas = mutableStateListOf(
        Musica(1, "Trilha da Taverna", "Artista exemplo", "MPB"),
        Musica(2, "Batalha Final", "Artista exemplo", "Rock"),
        Musica(3, "Viagem Fantástica", "Artista exemplo", "Pop"),
        Musica(4, "Holy", "Justin Bieber", "Pop")
    )
    private var proximoId = 5

    fun salvar(id: Int?, titulo: String, artista: String, genero: String) {
        if (id == null) {
            musicas.add(Musica(proximoId++, titulo, artista, genero))
        } else {
            val indice = musicas.indexOfFirst { it.id == id }
            if (indice >= 0) musicas[indice] = Musica(id, titulo, artista, genero)
        }
    }

    fun excluir(id: Int) {
        musicas.removeAll { it.id == id }
    }
}


@Composable
fun Playlist(modifier: Modifier = Modifier) {
    val palette = LocalModePalette.current
    val estadoScroll = rememberLazyListState()

    var pesquisa by rememberSaveable { mutableStateOf("") }
    var generoSelecionado by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var formularioAberto by rememberSaveable {
        mutableStateOf(false)
    }
    var musicaEmEdicaoId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    var musicaParaExcluirId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    val musicaEmEdicao = PlaylistEmMemoria.musicas.firstOrNull { it.id == musicaEmEdicaoId }
    val musicaParaExcluir = PlaylistEmMemoria.musicas.firstOrNull { it.id == musicaParaExcluirId }

    // Reprodução simulada
    var musicaAtualId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }
    var estaTocando by rememberSaveable {
        mutableStateOf(false)
    }

    val musicaAtual = PlaylistEmMemoria.musicas.firstOrNull {
        it.id == musicaAtualId
    }

    val musicasFiltradas = PlaylistEmMemoria.musicas.filter { musica ->
        (generoSelecionado == null ||
                musica.genero == generoSelecionado) &&
                (pesquisa.isBlank() ||
                        musica.titulo.contains(
                            pesquisa.trim(),
                            ignoreCase = true
                        ) ||
                        musica.artista.contains(
                            pesquisa.trim(),
                            ignoreCase = true
                        ))
    }

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
                    value = pesquisa,
                    onValueChange = { pesquisa = it },
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
                    generos.forEach { genero ->
                        FilterChip(
                            selected = generoSelecionado == genero,
                            onClick = {
                                generoSelecionado =
                                    if (generoSelecionado == genero) {
                                        null
                                    } else {
                                        genero
                                    }
                            },
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
                    onClick = {
                        musicaEmEdicaoId = null
                        formularioAberto = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accent
                    )
                ) {
                    Text("+ Adicionar música")
                }
            }

            // Mensagem se nenhuma música for encontrada
            if (musicasFiltradas.isEmpty()) {
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

                    tocando = musicaAtualId == musica.id &&
                            estaTocando,

                    pausada = musicaAtualId == musica.id &&
                            !estaTocando,

                    onTocar = {
                        if (musicaAtualId == musica.id) {
                            estaTocando = !estaTocando
                        } else {
                            musicaAtualId = musica.id
                            estaTocando = true
                        }
                    },

                    onEditar = {
                        musicaEmEdicaoId = musica.id
                        formularioAberto = true
                    },

                    onExcluir = {
                        musicaParaExcluirId = musica.id
                    }
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
                        text = if (estaTocando) {
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
                            onClick = {
                                estaTocando = !estaTocando
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accent
                            )
                        ) {
                            Text(
                                if (estaTocando) {
                                    "Pausar"
                                } else {
                                    "Continuar"
                                }
                            )
                        }

                        TextButton(
                            onClick = {
                                musicaAtualId = null
                                estaTocando = false
                            }
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
    if (formularioAberto) {
        FormularioMusica(
            musica = musicaEmEdicao,

            onCancelar = {
                formularioAberto = false
            },

            onSalvar = { titulo, artista, genero ->
                PlaylistEmMemoria.salvar(
                    musicaEmEdicao?.id,
                    titulo,
                    artista,
                    genero
                )
                formularioAberto = false
            }
        )
    }

    // Confirmação de exclusão
    musicaParaExcluir?.let { musica ->

        AlertDialog(
            onDismissRequest = {
                musicaParaExcluirId = null
            },

            title = {
                Text("Excluir música?")
            },

            text = {
                Text("Deseja excluir '${musica.titulo}'?")
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        PlaylistEmMemoria.excluir(musica.id)

                        if (musicaAtualId == musica.id) {
                            musicaAtualId = null
                            estaTocando = false
                        }

                        musicaParaExcluirId = null
                    }
                ) {
                    Text("Excluir")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        musicaParaExcluirId = null
                    }
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
    musica: Musica?,
    onCancelar: () -> Unit,
    onSalvar: (String, String, String) -> Unit
) {
    var titulo by rememberSaveable(musica) { mutableStateOf(musica?.titulo.orEmpty()) }
    var artista by rememberSaveable(musica) { mutableStateOf(musica?.artista.orEmpty()) }
    var genero by rememberSaveable(musica) { mutableStateOf(musica?.genero ?: generos.first()) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (musica == null) "Adicionar música" else "Editar música") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Nome da música") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artista,
                    onValueChange = { artista = it },
                    label = { Text("Artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Gênero")
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    generos.forEach { opcao ->
                        FilterChip(
                            selected = genero == opcao,
                            onClick = { genero = opcao },
                            label = { Text(opcao) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = titulo.isNotBlank() && artista.isNotBlank(),
                onClick = { onSalvar(titulo.trim(), artista.trim(), genero) }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
