package com.example.points.database

import androidx.room.TypeConverter
import com.google.firebase.Timestamp

/**
 * Convertidores de tipos para Room
 * Convierte tipos complejos a tipos primitivos para SQLite
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.seconds?.times(1000)?.plus(timestamp.nanoseconds / 1000000)
    }
    
    @TypeConverter
    fun dateToTimestamp(value: Long?): Timestamp? {
        return value?.let {
            val seconds = it / 1000
            val nanoseconds = ((it % 1000) * 1000000).toInt()
            Timestamp(seconds, nanoseconds)
        }
    }
}

