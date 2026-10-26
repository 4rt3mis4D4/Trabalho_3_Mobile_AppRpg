package com.example.app_rpg

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.app_rpg.ui.theme.*

private val generos = listOf("Pop", "Rock", "MPB")

data class Musica(
    val id: Long,
    val titulo: String,
    val artista: String,
    val genero: String
)

// Simulação em memória, substituir pelo banco depois.
private object PlaylistEmMemoria {
    val musicas = mutableStateListOf(
        Musica(1, "Trilha da Taverna", "Artista exemplo", "MPB"),
        Musica(2, "Batalha Final", "Artista exemplo", "Rock"),
        Musica(3, "Viagem Fantástica", "Artista exemplo", "Pop")
    )
    private var proximoId = 4L

    fun salvar(id: Long?, titulo: String, artista: String, genero: String) {
        if (id == null) {
            musicas.add(Musica(proximoId++, titulo, artista, genero))
        } else {
            val indice = musicas.indexOfFirst { it.id == id }
            if (indice >= 0) musicas[indice] = Musica(id, titulo, artista, genero)
        }
    }

    fun excluir(id: Long) {
        musicas.removeAll { it.id == id }
    }
}

@Composable
fun Playlist(modifier: Modifier = Modifier) {
    val palette = LocalModePalette.current
    var pesquisa by rememberSaveable { mutableStateOf("") }
    var generoSelecionado by rememberSaveable { mutableStateOf<String?>(null) }
    var formularioAberto by remember { mutableStateOf(false) }
    var musicaEmEdicao by remember { mutableStateOf<Musica?>(null) }
    var musicaParaExcluir by remember { mutableStateOf<Musica?>(null) }

    // Reprodução simulada: nenhuma mídia é carregada ou executada.
    var musicaAtualId by rememberSaveable { mutableStateOf<Long?>(null) }
    var estaTocando by rememberSaveable { mutableStateOf(false) }
    val musicaAtual = PlaylistEmMemoria.musicas.firstOrNull { it.id == musicaAtualId }

    val musicasFiltradas = PlaylistEmMemoria.musicas.filter { musica ->
        (generoSelecionado == null || musica.genero == generoSelecionado) &&
                (pesquisa.isBlank() ||
                        musica.titulo.contains(pesquisa.trim(), ignoreCase = true) ||
                        musica.artista.contains(pesquisa.trim(), ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(corPretoPuro)
            .padding(24.dp)
    ) {
        Text(
            text = "Playlist",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = corBrancoPuro
        )
        Spacer(Modifier.height(16.dp))

        TextField(
            value = pesquisa,
            onValueChange = { pesquisa = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Pesquisar música ou artista") },
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

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            generos.forEach { genero ->
                FilterChip(
                    selected = generoSelecionado == genero,
                    onClick = {
                        generoSelecionado = if (generoSelecionado == genero) null else genero
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

        Button(
            onClick = {
                musicaEmEdicao = null
                formularioAberto = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent)
        ) {
            Text("+ Adicionar música")
        }
        Spacer(Modifier.height(12.dp))

        // Ocupa o espaço disponível e rola independentemente da barra inferior.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (musicasFiltradas.isEmpty()) {
                item {
                    Text(
                        "Nenhuma música encontrada.",
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = corCinzaMedio
                    )
                }
            }
            items(musicasFiltradas, key = { it.id }) { musica ->
                MusicaCard(
                    musica = musica,
                    tocando = musicaAtualId == musica.id && estaTocando,
                    pausada = musicaAtualId == musica.id && !estaTocando,
                    onTocar = {
                        if (musicaAtualId == musica.id) {
                            estaTocando = !estaTocando
                        } else {
                            musicaAtualId = musica.id
                            estaTocando = true
                        }
                    },
                    onEditar = {
                        musicaEmEdicao = musica
                        formularioAberto = true
                    },
                    onExcluir = { musicaParaExcluir = musica }
                )
            }
        }

        if (musicaAtual != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = corCinzaEscuro)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        if (estaTocando) "♫ Tocando agora (simulação)" else "⏸ Música pausada (simulação)",
                        color = palette.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        musicaAtual.titulo,
                        color = corBrancoPuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(musicaAtual.artista, color = corCinzaMedio)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { estaTocando = !estaTocando },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accent)
                        ) {
                            Text(if (estaTocando) "Pausar" else "Continuar")
                        }
                        TextButton(onClick = {
                            musicaAtualId = null
                            estaTocando = false
                        }) {
                            Text("Parar", color = corBrancoPuro)
                        }
                    }
                }
            }
        }
    }

    if (formularioAberto) {
        FormularioMusica(
            musica = musicaEmEdicao,
            onCancelar = { formularioAberto = false },
            onSalvar = { titulo, artista, genero ->
                PlaylistEmMemoria.salvar(musicaEmEdicao?.id, titulo, artista, genero)
                formularioAberto = false
            }
        )
    }

    musicaParaExcluir?.let { musica ->
        AlertDialog(
            onDismissRequest = { musicaParaExcluir = null },
            title = { Text("Excluir música?") },
            text = { Text("Deseja excluir '${musica.titulo}'?") },
            confirmButton = {
                TextButton(onClick = {
                    PlaylistEmMemoria.excluir(musica.id)
                    if (musicaAtualId == musica.id) {
                        musicaAtualId = null
                        estaTocando = false
                    }
                    musicaParaExcluir = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { musicaParaExcluir = null }) { Text("Cancelar") }
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
    var titulo by remember(musica) { mutableStateOf(musica?.titulo.orEmpty()) }
    var artista by remember(musica) { mutableStateOf(musica?.artista.orEmpty()) }
    var genero by remember(musica) { mutableStateOf(musica?.genero ?: generos.first()) }

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
