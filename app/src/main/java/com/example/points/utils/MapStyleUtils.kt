package com.example.points.utils

import android.content.Context
import com.example.points.R
import com.google.android.gms.maps.model.MapStyleOptions

object MapStyleUtils {
    
    fun getMapStyleWithoutPOI(context: Context): MapStyleOptions? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.map_style_no_poi)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            MapStyleOptions(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
