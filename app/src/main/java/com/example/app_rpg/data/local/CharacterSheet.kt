package com.example.app_rpg.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

data class AtributoData(val valor: String, val label: String)
data class StatusData(val valor: String, val label: String)
data class PericiasData(val proficiencia: Int, val bonus: String, val nome: String)

@Entity(tableName = "fichas")
data class CharacterSheet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val level: String = "",
    val characterClass: String = "",
    val race: String = "",
    val attributes: List<AtributoData> = atributosMock,
    val status: List<StatusData> = statusMock,
    val skills: List<PericiasData> = periciaMock
)

val atributosMock = listOf(
    AtributoData("1", "For"),
    AtributoData("1", "Des"),
    AtributoData("1", "Con"),
    AtributoData("1", "Int"),
    AtributoData("1", "Sab"),
    AtributoData("1", "Car")
)

val statusMock = listOf(
    StatusData("56", "HP"),
    StatusData("23", "Armad"),
    StatusData("17", "Iniciat"),
    StatusData("9m", "Desloc")
)

val periciaMock = listOf(
    PericiasData(proficiencia = 2, bonus = "2", nome = "Perícia X"),
    PericiasData(proficiencia = 0, bonus = "0", nome = "Perícia Y"),
    PericiasData(proficiencia = 1, bonus = "4", nome = "Perícia Z"),
    PericiasData(proficiencia = 2, bonus = "2", nome = "Perícia A"),
    PericiasData(proficiencia = 0, bonus = "0", nome = "Perícia B"),
    PericiasData(proficiencia = 1, bonus = "4", nome = "Perícia C"),
    PericiasData(proficiencia = 2, bonus = "2", nome = "Perícia U"),
    PericiasData(proficiencia = 0, bonus = "0", nome = "Perícia I"),
    PericiasData(proficiencia = 1, bonus = "4", nome = "Perícia V")
)
