package com.example.points.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para caché de POIs
 * Permite acceso offline a POIs vistos recientemente
 */
@Entity(tableName = "cached_pois")
data class CachedPOI(
    @PrimaryKey
    val poiId: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val direccion: String,
    val lat: Double,
    val lon: Double,
    val calificacion: Double,
    val totalCalificaciones: Int,
    val imagenUrl: String?,
    val estado: String,
    val fechaCreacionMillis: Long,
    val fechaActualizacionMillis: Long,
    val fechaCache: Long = System.currentTimeMillis(),
    val jsonData: String // Datos completos en JSON para deserialización
)

