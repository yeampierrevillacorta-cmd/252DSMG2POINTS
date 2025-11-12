package com.example.points.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

/**
 * Entidad Room para POIs favoritos almacenados localmente
 * Implementa almacenamiento local para la Unidad 5 de Android Basics
 */
@Entity(tableName = "favorite_pois")
data class FavoritePOI(
    @PrimaryKey
    val poiId: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val direccion: String,
    val lat: Double,
    val lon: Double,
    val calificacion: Double,
    val imagenUrl: String?,
    val fechaAgregado: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)

