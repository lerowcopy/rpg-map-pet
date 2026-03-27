package com.example.rpg_map_pet.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Класс для импорта данных из GeoJSON файла в базу данных
 */
class GeoJsonImporter(private val context: Context) {

    private val gson = Gson()

    /**
     * Импортирует точки интереса из GeoJSON файла в базу данных
     */
    suspend fun importToDatabase(landmarkDao: LandmarkDao): ImportResult {
        return try {
            val featureCollection = parseGeoJson()
            val landmarks = featureCollection.toLandmarkEntities()
            
            val existingCount = landmarkDao.getLandmarksCount()
            if (existingCount == 0) {
                landmarkDao.insertLandmarks(landmarks)
                ImportResult.Success(landmarks.size)
            } else {
                ImportResult.AlreadyImported(existingCount)
            }
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Парсит GeoJSON файл из assets
     */
    private fun parseGeoJson(): GeoJsonDto {
        val jsonString = context.assets.open("export.geojson")
            .bufferedReader()
            .use { it.readText() }
        
        return gson.fromJson(jsonString, GeoJsonDto::class.java)
    }

    /**
     * DTO для десериализации GeoJSON
     */
    private data class GeoJsonDto(
        @SerializedName("type") val type: String,
        @SerializedName("features") val features: List<FeatureDto>
    )

    /**
     * DTO для Feature из GeoJSON
     */
    private data class FeatureDto(
        @SerializedName("type") val type: String,
        @SerializedName("id") val id: String,
        @SerializedName("properties") val properties: Map<String, Any?>,
        @SerializedName("geometry") val geometry: GeometryDto
    )

    /**
     * DTO для Geometry из GeoJSON
     */
    private data class GeometryDto(
        @SerializedName("type") val type: String,
        @SerializedName("coordinates") val coordinates: List<Double>
    )

    /**
     * Конвертирует FeatureCollection в список LandmarkEntity
     */
    private fun GeoJsonDto.toLandmarkEntities(): List<LandmarkEntity> {
        return features.mapNotNull { feature ->
            feature.toLandmarkEntity()
        }
    }

    /**
     * Конвертирует Feature в LandmarkEntity
     */
    private fun FeatureDto.toLandmarkEntity(): LandmarkEntity? {
        val geometry = geometry
        val coordinates = geometry.coordinates
        
        if (geometry.type != "Point" || coordinates.size < 2) return null
        
        val properties = properties
        val id = id.ifEmpty { return null }
        
        // Получаем название (приоритет: name:ru -> name -> локализация)
        val name = properties["name:ru"] as? String
            ?: properties["name"] as? String
            ?: properties["name:en"] as? String
            ?: "Без названия"
        
        // Определяем тип места
        val type = determineType(properties)
        
        // Описание
        val description = buildDescription(properties)
        
        // Награда XP (зависит от типа)
        val xpReward = determineXpReward(type)
        
        return LandmarkEntity(
            id = id,
            name = name,
            latitude = coordinates[1],  // GeoJSON: [longitude, latitude]
            longitude = coordinates[0],
            type = type,
            description = description,
            isCompleted = false,
            xpReward = xpReward
        )
    }

    /**
     * Определяет тип места по свойствам GeoJSON
     */
    private fun determineType(properties: Map<String, Any?>): String {
        return when {
            properties["historic"] == "monument" -> "monument"
            properties["historic"] == "memorial" -> "memorial"
            properties["historic"] == "cannon" -> "cannon"
            properties["tourism"] == "attraction" -> "attraction"
            properties["tourism"] == "museum" -> "museum"
            properties["amenity"] == "ferry_terminal" -> "ferry_terminal"
            properties["building"] != null -> "building"
            properties["historic"] != null -> "historic"
            else -> "poi"
        }
    }

    /**
     * Строит описание из свойств GeoJSON
     */
    private fun buildDescription(properties: Map<String, Any?>): String {
        val parts = mutableListOf<String>()
        
        // Wikipedia ссылка
        val wikipedia = properties["wikipedia"] as? String
        if (wikipedia != null) {
            parts.add("Wikipedia: $wikipedia")
        }
        
        // Wikidata
        val wikidata = properties["wikidata"] as? String
        if (wikidata != null) {
            parts.add("Wikidata: $wikidata")
        }
        
        // Инскрипция (надпись на памятнике)
        val inscription = properties["inscription:ru"] as? String
            ?: properties["inscription"] as? String
        if (inscription != null) {
            parts.add(inscription)
        }
        
        // Архитектор
        val architect = properties["architect"] as? String
        if (architect != null) {
            parts.add("Архитектор: $architect")
        }
        
        // Художник/скульптор
        val artist = properties["artist_name"] as? String
        if (artist != null) {
            parts.add("Скульптор: $artist")
        }
        
        // Дата создания
        val startDate = properties["start_date"] as? String
        if (startDate != null) {
            parts.add("Дата: $startDate")
        }
        
        return parts.joinToString(" | ")
    }

    /**
     * Определяет награду XP в зависимости от типа места
     */
    private fun determineXpReward(type: String): Int {
        return when (type) {
            "monument" -> 150
            "memorial" -> 100
            "museum" -> 200
            "attraction" -> 120
            "historic" -> 100
            else -> 80
        }
    }

    /**
     * Результат импорта
     */
    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class AlreadyImported(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
