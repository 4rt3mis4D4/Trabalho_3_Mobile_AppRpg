package com.example.app_rpg

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

data class itemCategory(
    val nome: String,
    @param:DrawableRes val iconsRes: Int
)

@Entity(tableName = "itens")
data class ItemRpg(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemName: String,
    val itemValue: Int,
    val itemDescription: String,
    val itemCat: itemCategory
)

val listCatItem = listOf(
    itemCategory("Armas", R.drawable.icon_armament),
    itemCategory("Alimentos", R.drawable.icon_food),
    itemCategory("Armaduras", R.drawable.icon_armor),
    itemCategory("Ferramentas", R.drawable.icon_tools),
    itemCategory("Montarias", R.drawable.icon_mount)
)