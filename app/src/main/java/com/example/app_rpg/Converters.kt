package com.example.app_rpg

import android.net.Uri
import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun categoryToString(category: itemCategory): String = category.nome

    @TypeConverter
    fun stringToCategory(name: String): itemCategory =
        listCatItem.firstOrNull { it.nome == name } ?: listCatItem.first()

    @TypeConverter
    fun uriToString(uri: Uri?): String? = uri?.toString()

    @TypeConverter
    fun stringToUri(value: String?): Uri? = value?.let(Uri::parse)

    @TypeConverter
    fun idsToString(ids: Set<Int>): String = ids.joinToString(",")

    @TypeConverter
    fun stringToIds(value: String): Set<Int> =
        value.split(",").mapNotNull { it.toIntOrNull() }.toSet()

    @TypeConverter
    fun attributesToString(attributes: List<AtributoData>): String =
        JSONArray(attributes.map { JSONObject().put("valor", it.valor).put("label", it.label) }).toString()

    @TypeConverter
    fun stringToAttributes(value: String): List<AtributoData> =
        value.toJsonObjects().map { AtributoData(it.getString("valor"), it.getString("label")) }

    @TypeConverter
    fun statusToString(status: List<StatusData>): String =
        JSONArray(status.map { JSONObject().put("valor", it.valor).put("label", it.label) }).toString()

    @TypeConverter
    fun stringToStatus(value: String): List<StatusData> =
        value.toJsonObjects().map { StatusData(it.getString("valor"), it.getString("label")) }

    @TypeConverter
    fun skillsToString(skills: List<PericiasData>): String =
        JSONArray(
            skills.map {
                JSONObject()
                    .put("proficiencia", it.proficiencia)
                    .put("bonus", it.bonus)
                    .put("nome", it.nome)
            }
        ).toString()

    @TypeConverter
    fun stringToSkills(value: String): List<PericiasData> =
        value.toJsonObjects().map {
            PericiasData(it.getInt("proficiencia"), it.getString("bonus"), it.getString("nome"))
        }

    private fun String.toJsonObjects(): List<JSONObject> {
        val array = JSONArray(this)
        return List(array.length()) { array.getJSONObject(it) }
    }
}
