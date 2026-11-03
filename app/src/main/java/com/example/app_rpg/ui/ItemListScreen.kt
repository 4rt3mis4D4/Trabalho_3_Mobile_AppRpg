package com.example.app_rpg.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_rpg.ui.theme.App_rpgTheme
import com.example.app_rpg.ui.theme.LocalModePalette
import com.example.app_rpg.ui.theme.MasterPalette
import com.example.app_rpg.ui.theme.corBrancoOffWhite
import com.example.app_rpg.ui.theme.corBrancoPuro
import com.example.app_rpg.ui.theme.corCinzaEscuro
import com.example.app_rpg.ui.theme.corCinzaMedio
import com.example.app_rpg.ui.theme.corPretoPuro
import com.example.app_rpg.R
import com.example.app_rpg.data.local.ItemRpg
import com.example.app_rpg.data.local.listCatItem
import com.example.app_rpg.viewmodel.ItemUiState
import com.example.app_rpg.viewmodel.ItemViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

@Composable
fun ItemListScreen(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel,
    onItemClick: (ItemRpg) -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (ItemRpg) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ItemListContent(
        modifier = modifier,
        uiState = uiState,
        onItemClick = onItemClick,
        onAdd = onAdd,
        onEdit = onEdit,
        onPedirExclusao = viewModel::pedirExclusao,
        onCancelarExclusao = viewModel::cancelarExclusao,
        onExcluir = viewModel::excluirItem
    )
}

@Composable
private fun ItemListContent(
    modifier: Modifier = Modifier,
    uiState: ItemUiState,
    onItemClick: (ItemRpg) -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (ItemRpg) -> Unit = {},
    onPedirExclusao: (ItemRpg) -> Unit = {},
    onCancelarExclusao: () -> Unit = {},
    onExcluir: (ItemRpg) -> Unit = {}
) {
    val palette = LocalModePalette.current
    val context = LocalContext.current
    val items = uiState.itens
    val itemToDelete = uiState.itemParaExcluir

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(corPretoPuro)
    ) {
        if (!uiState.carregando && items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum item cadastrado ainda.\nToque em + para criar um item.",
                    color = corCinzaMedio,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemListCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onEdit = { onEdit(item) },
                        onDelete = { onPedirExclusao(item) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = CircleShape,
            containerColor = palette.accent,
            contentColor = corBrancoPuro
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_add),
                contentDescription = "Criar item"
            )
        }
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { onCancelarExclusao() },
            containerColor = corCinzaEscuro,
            titleContentColor = corBrancoPuro,
            textContentColor = corBrancoOffWhite,
            title = { Text("Excluir item") },
            text = { Text("Tem certeza que deseja excluir o item \"${item.itemName}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExcluir(item)
                        Toast.makeText(
                            context,
                            "Item ${item.itemName} excluído",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("EXCLUIR", color = palette.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onCancelarExclusao() }) {
                    Text("CANCELAR", color = corCinzaMedio, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ItemListCard(
    item: ItemRpg,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LocalModePalette.current

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = BorderStroke(1.dp, palette.strong)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(palette.strong),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.itemCat.iconsRes),
                    contentDescription = item.itemCat.nome,
                    tint = corBrancoPuro,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.strong)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.itemName,
                        color = corBrancoPuro,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = item.itemDescription.ifBlank { "Sem descrição" },
                    color = corBrancoOffWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.soft)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_money),
                        contentDescription = null,
                        tint = palette.surface,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.itemValue.toString(),
                        color = palette.surface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = palette.strong,
                        contentColor = corBrancoPuro
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_edit),
                        contentDescription = "Editar ${item.itemName}",
                        modifier = Modifier.size(20.dp)
                    )
                }

                FilledIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = corCinzaEscuro,
                        contentColor = palette.accent
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_delete),
                        contentDescription = "Excluir ${item.itemName}",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ItemListScreenPreview() {
    val items = listOf(
        ItemRpg(1, "Espada Longa", 150, "Lâmina de aço temperado.", listCatItem[0]),
        ItemRpg(2, "Pão de Viagem", 5, "", listCatItem[1]),
        ItemRpg(3, "Cota de Malha", 300, "Proteção leve e resistente.", listCatItem[2])
    )

    App_rpgTheme {
        CompositionLocalProvider(LocalModePalette provides MasterPalette) {
            ItemListContent(uiState = ItemUiState(itens = items, carregando = false))
        }
    }
}
