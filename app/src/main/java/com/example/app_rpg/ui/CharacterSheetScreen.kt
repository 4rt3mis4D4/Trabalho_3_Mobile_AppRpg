package com.example.app_rpg.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_rpg.ui.theme.LocalModePalette
import com.example.app_rpg.ui.theme.corBrancoOffWhite
import com.example.app_rpg.ui.theme.corBrancoPuro
import com.example.app_rpg.ui.theme.corCinzaEscuro
import com.example.app_rpg.ui.theme.corCinzaMedio
import com.example.app_rpg.ui.theme.corPretoPuro
import com.example.app_rpg.data.local.AtributoData
import com.example.app_rpg.data.local.PericiasData
import com.example.app_rpg.data.local.StatusData
import com.example.app_rpg.data.local.CharacterSheet
import com.example.app_rpg.viewmodel.FichaViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FichaPersonagemContentPreview() {
    FichaPersonagemContent(Modifier, CharacterSheet())
}

@Composable
fun FichaPersonagemScreen(
    modifier: Modifier,
    viewModel: FichaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FichaPersonagemContent(
        modifier = modifier,
        draft = uiState.rascunho,
        onNameChange = viewModel::atualizarNome,
        onLevelChange = viewModel::atualizarLevel,
        onClassChange = viewModel::atualizarClasse,
        onRaceChange = viewModel::atualizarRaca,
        onAtributoChange = viewModel::atualizarAtributo,
        onStatusChange = viewModel::atualizarStatus,
        onProficienciaClick = viewModel::alternarProficiencia,
        onBonusChange = viewModel::atualizarBonusPericia,
        onNomePericiaChange = viewModel::atualizarNomePericia,
        onCancel = viewModel::cancelarEdicao,
        onSave = viewModel::salvarFicha
    )
}

@Composable
private fun FichaPersonagemContent(
    modifier: Modifier,
    draft: CharacterSheet,
    onNameChange: (String) -> Unit = {},
    onLevelChange: (String) -> Unit = {},
    onClassChange: (String) -> Unit = {},
    onRaceChange: (String) -> Unit = {},
    onAtributoChange: (Int, String) -> Unit = { _, _ -> },
    onStatusChange: (Int, String) -> Unit = { _, _ -> },
    onProficienciaClick: (Int) -> Unit = {},
    onBonusChange: (Int, String) -> Unit = { _, _ -> },
    onNomePericiaChange: (Int, String) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(corPretoPuro)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(
            name = draft.name,
            level = draft.level,
            onNameChange = onNameChange,
            onLevelChange = onLevelChange
        )
        ClasseRacaSection(
            characterClass = draft.characterClass,
            race = draft.race,
            onClassChange = onClassChange,
            onRaceChange = onRaceChange
        )
        AtributosSection(
            attributes = draft.attributes,
            onChange = onAtributoChange
        )
        StatusSection(
            status = draft.status,
            onChange = onStatusChange
        )
        PericiaSection(
            skills = draft.skills,
            onProficienciaClick = onProficienciaClick,
            onBonusChange = onBonusChange,
            onNomeChange = onNomePericiaChange
        )
        BotoesAcaoSection(
            onCancel = {
                focusManager.clearFocus()
                onCancel()
            },
            onSave = {
                focusManager.clearFocus()
                onSave()
                Toast.makeText(context, "Ficha salva com sucesso", Toast.LENGTH_SHORT).show()
            }
        )
    }
}


@Composable
fun HeaderSection(
    name: String,
    level: String,
    onNameChange: (String) -> Unit,
    onLevelChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CampoTexto(
            value = name,
            onValueChange = onNameChange,
            label = "Nome",
            modifier = Modifier.weight(2f)
        )
        CampoTexto(
            value = level,
            onValueChange = onLevelChange,
            label = "Level",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun ClasseRacaSection(
    characterClass: String,
    race: String,
    onClassChange: (String) -> Unit,
    onRaceChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CampoTexto(
            value = characterClass,
            onValueChange = onClassChange,
            label = "Classe",
            modifier = Modifier.weight(1f)
        )
        CampoTexto(
            value = race,
            onValueChange = onRaceChange,
            label = "Raça",
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun AtributosSection(
    attributes: List<AtributoData>,
    onChange: (Int, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        attributes.withIndex().chunked(3).forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                linha.forEach { (index, atributo) ->
                    CirculoAtributo(
                        valor = atributo.valor,
                        label = atributo.label,
                        onValueChange = { novo -> onChange(index, novo) }
                    )
                }
            }
        }
    }
}


@Composable
fun StatusSection(
    status: List<StatusData>,
    onChange: (Int, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        status.forEachIndexed { index, item ->
            CardStatus(
                valor = item.valor,
                label = item.label,
                onValueChange = { novo -> onChange(index, novo) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun PericiaSection(
    skills: List<PericiasData>,
    onProficienciaClick: (Int) -> Unit,
    onBonusChange: (Int, String) -> Unit,
    onNomeChange: (Int, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = corCinzaEscuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            skills.withIndex().chunked((skills.size + 1) / 2).forEach { coluna ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    coluna.forEach { (index, pericia) ->
                        LinhaPericia(
                            proficiencia = pericia.proficiencia,
                            bonus = pericia.bonus,
                            nome = pericia.nome,
                            onProficienciaClick = { onProficienciaClick(index) },
                            onBonusChange = { novo -> onBonusChange(index, novo) },
                            onNomeChange = { novo -> onNomeChange(index, novo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BotoesAcaoSection(onCancel: () -> Unit, onSave: () -> Unit) {
    val palette = LocalModePalette.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = corCinzaEscuro,
                contentColor = corBrancoOffWhite
            )
        ) {
            Text(
                text = "Cancelar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.accent,
                contentColor = corBrancoPuro
            )
        ) {
            Text(
                text = "Salvar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val palette = LocalModePalette.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        singleLine = true,
        cursorBrush = SolidColor(palette.accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = corCinzaMedio),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        }
    )
}


@Composable
fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(corCinzaEscuro)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = corCinzaMedio,
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
        SheetTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = corBrancoOffWhite,
                fontWeight = FontWeight.Medium
            ),
            placeholder = label,
            keyboardType = keyboardType,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun CirculoAtributo(valor: String, label: String, onValueChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(corCinzaEscuro),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SheetTextField(
                    value = valor,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = corBrancoPuro,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    ),
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.width(48.dp)
                )
                Text(
                    text = label,
                    color = corCinzaMedio,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 13.sp
                )
            }
        }
    }
}


@Composable
fun CardStatus(
    valor: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = corCinzaEscuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SheetTextField(
                value = valor,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = corBrancoPuro,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                ),
                placeholder = "-",
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = label,
                color = corCinzaMedio,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 13.sp
            )
        }
    }
}


@Composable
fun LinhaPericia(
    proficiencia: Int,
    bonus: String,
    nome: String,
    onProficienciaClick: () -> Unit,
    onBonusChange: (String) -> Unit,
    onNomeChange: (String) -> Unit
) {
    val palette = LocalModePalette.current
    val corIndicador = when {
        proficiencia >= 2 -> palette.accent
        proficiencia == 1 -> corCinzaMedio
        else -> corCinzaEscuro.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(corIndicador)
                .border(
                    width = 1.5.dp,
                    color = corCinzaMedio.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clickable(
                    role = Role.Button,
                    onClickLabel = "Alterar proficiência de $nome",
                    onClick = onProficienciaClick
                )
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(corPretoPuro.copy(alpha = 0.35f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            SheetTextField(
                value = bonus,
                onValueChange = onBonusChange,
                textStyle = TextStyle(
                    color = corBrancoOffWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                placeholder = "0",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.width(28.dp)
            )
        }

        SheetTextField(
            value = nome,
            onValueChange = onNomeChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = corBrancoOffWhite,
                fontWeight = FontWeight.Normal
            ),
            placeholder = "Perícia",
            modifier = Modifier.weight(1f)
        )
    }
}
