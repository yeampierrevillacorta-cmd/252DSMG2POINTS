package com.example.points.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val category: String?,
    val fechaBusqueda: Long = System.currentTimeMillis(),
    val resultados: Int = 0
)

