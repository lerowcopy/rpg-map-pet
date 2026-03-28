package com.example.rpg_map_pet.data.repository

import android.util.Log
import com.example.rpg_map_pet.core.utils.calculateDistance
import com.example.rpg_map_pet.data.local.GeoJsonImporter
import com.example.rpg_map_pet.data.local.LandmarkDao
import com.example.rpg_map_pet.data.local.LandmarkEntity
import com.example.rpg_map_pet.domain.landmark.ImportResult
import com.example.rpg_map_pet.domain.landmark.LandmarkRepository
import com.example.rpg_map_pet.domain.model.Landmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LandmarkRepositoryImpl @Inject constructor(
    private val landmarkDao: LandmarkDao,
    private val geoJsonImporter: GeoJsonImporter
) : LandmarkRepository {

    override fun getLandmarks(): Flow<List<Landmark>> =
        landmarkDao.getAllLandmarks().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getActiveLandmarks(): Flow<List<Landmark>> =
        landmarkDao.getActiveLandmarks().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getCompletedLandmarks(): Flow<List<Landmark>> =
        landmarkDao.getCompletedLandmarks().map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getLandmarksInBoundingBox(
        minLatitude: Double,
        maxLatitude: Double,
        minLongitude: Double,
        maxLongitude: Double
    ): Flow<List<Landmark>> =
        landmarkDao.getLandmarksInBoundingBox(
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getLandmarksInRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Float
    ): Flow<List<Landmark>> =
        landmarkDao.getLandmarksInRadius(latitude, longitude, radiusMeters).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun getLandmarkById(landmarkId: String): Landmark? {
        return landmarkDao.getLandmarkById(landmarkId)?.toDomainModel()
    }

    override suspend fun markAsVisited(landmarkId: String) {
        landmarkDao.markAsCompleted(landmarkId)
    }

    override suspend fun markAsNotVisited(landmarkId: String) {
        val landmark = landmarkDao.getLandmarkById(landmarkId)
        if (landmark != null) {
            landmarkDao.updateLandmark(landmark.copy(isCompleted = false))
        }
    }

    override fun isUserNearLandmark(userLat: Double, userLng: Double, landmark: Landmark): Boolean {
        val distance = calculateDistance(userLat, userLng, landmark.latitude, landmark.longitude)
        return distance <= landmark.radius
    }

    override suspend fun importFromGeoJson(): ImportResult {
        return when (val result = geoJsonImporter.importToDatabase(landmarkDao)) {
            is GeoJsonImporter.ImportResult.AlreadyImported -> ImportResult.AlreadyImported
            is GeoJsonImporter.ImportResult.Success -> ImportResult.Success(result.count)
            is GeoJsonImporter.ImportResult.Error -> ImportResult.Error(result.message)
        }
    }

    override suspend fun getLandmarksCount(): Int {
        return landmarkDao.getLandmarksCount()
    }

    private fun LandmarkEntity.toDomainModel(): Landmark {
        return Landmark(
            id = id,
            name = name,
            description = description,
            latitude = latitude,
            longitude = longitude,
            radius = 50f, // Default radius for landmarks
            isVisited = isCompleted,
            visitedAt = if (isCompleted) System.currentTimeMillis() else null
        )
    }
}

// ============================================================================
// РЕЗЕРВНЫЙ ВАРИАНТ: Явный список достопримечательностей (если GeoJSON не нужен)
// ============================================================================
/*
@Singleton
class LandmarkRepositoryImpl @Inject constructor(
    private val landmarkDao: LandmarkDao,
    private val geoJsonImporter: GeoJsonImporter
) : LandmarkRepository {

    private val moscowLandmarks = listOf(
        // Красная площадь и окрестности
        LandmarkEntity("node/311999397", "Царь-колокол", "Самый большой колокол в мире", 55.7507674, 37.6184371, "attraction", "Wikipedia: ru:Царь-колокол | Wikidata: Q147875", false, 120),
        LandmarkEntity("node/311999396", "Царь-пушка", "Памятник русской артиллерии", 55.7514667, 37.6179211, "cannon", "Wikipedia: ru:Царь-пушка | Wikidata: Q147881", false, 120),
        LandmarkEntity("node/250525422", "Покорителям космоса", "Монумент покорителям космоса", 55.8226761, 37.6397188, "monument", "Wikipedia: ru:Покорителям космоса | Wikidata: Q2299609", false, 150),
        LandmarkEntity("node/253124200", "Юрию Долгорукому", "Памятник основателю Москвы", 55.7618096, 37.6101609, "memorial", "Wikidata: Q4343424", false, 100),
        LandmarkEntity("node/416562790", "А. С. Пушкину", "Памятник поэту на Пушкинской площади", 55.7653354, 37.6054856, "memorial", "Wikidata: Q4343030", false, 100),
        
        // Храмы
        LandmarkEntity("msk_002", "Храм Василия Блаженного", "Православный храм, памятник архитектуры XVI века", 55.752520, 37.623060, "attraction", "", false, 200),
        LandmarkEntity("msk_003", "Московский Кремль", "Резиденция Президента РФ", 55.752020, 37.617500, "attraction", "", false, 200),
        LandmarkEntity("msk_014", "Храм Христа Спасителя", "Кафедральный собор РПЦ", 55.748500, 37.605300, "attraction", "", false, 200),
        
        // Театры
        LandmarkEntity("msk_009", "Большой Театр", "Один из крупнейших театров оперы и балета", 55.760300, 37.618600, "attraction", "", false, 150),
        LandmarkEntity("msk_010", "Малый Театр", "Старейший драматический театр Москвы", 55.757800, 37.620500, "attraction", "", false, 150),
        
        // Парки
        LandmarkEntity("msk_011", "Парк Зарядье", "Современный парк в центре Москвы", 55.751200, 37.625500, "attraction", "", false, 100),
        LandmarkEntity("msk_013", "Парк Горького", "Центральный парк культуры и отдыха", 55.729700, 37.601500, "attraction", "", false, 100),
        
        // Музеи
        LandmarkEntity("msk_016", "Третьяковская Галерея", "Крупнейший музей русского искусства", 55.741500, 37.620800, "museum", "", false, 200),
        LandmarkEntity("msk_017", "Пушкинский Музей", "Музей изобразительных искусств", 55.747800, 37.604500, "museum", "", false, 200),
        
        // ВДНХ
        LandmarkEntity("msk_026", "ВДНХ", "Выставка достижений народного хозяйства", 55.826500, 37.637500, "attraction", "", false, 150),
        LandmarkEntity("msk_028", "Космос (монумент)", "Памятник покорителям космоса", 55.820000, 37.640000, "monument", "", false, 150),
        
        // Воробьёвы горы
        LandmarkEntity("msk_024", "Воробьёвы Горы", "Смотровая площадка", 55.719500, 37.539000, "attraction", "", false, 100),
        LandmarkEntity("msk_025", "МГУ", "Московский Государственный Университет", 55.703500, 37.530000, "building", "", false, 100),
        
        // Москва-Сити
        LandmarkEntity("msk_029", "Москва-Сити", "Деловой центр", 55.749500, 37.537500, "attraction", "", false, 120),
        
        // Усадьбы
        LandmarkEntity("msk_033", "Царицыно", "Дворцово-парковый ансамбль", 55.607500, 37.670000, "attraction", "", false, 150),
        LandmarkEntity("msk_034", "Коломенское", "Музей-заповедник", 55.667500, 37.675000, "attraction", "", false, 150)
    )

    override fun getLandmarks(): Flow<List<Landmark>> = flow {
        emit(moscowLandmarks.map { it.toDomainModel() })
    }

    override fun getActiveLandmarks(): Flow<List<Landmark>> = flow {
        emit(moscowLandmarks.map { it.toDomainModel() })
    }

    override fun getCompletedLandmarks(): Flow<List<Landmark>> = flow {
        emit(emptyList())
    }

    override suspend fun getLandmarkById(landmarkId: String): Landmark? {
        return moscowLandmarks.find { it.id == landmarkId }?.toDomainModel()
    }

    override suspend fun markAsVisited(landmarkId: String) {
        // В резервном варианте просто логируем
        Log.d("LandmarkRepository", "Marked as visited: $landmarkId")
    }

    override suspend fun markAsNotVisited(landmarkId: String) {
        Log.d("LandmarkRepository", "Marked as not visited: $landmarkId")
    }

    override fun isUserNearLandmark(userLat: Double, userLng: Double, landmark: Landmark): Boolean {
        val distance = calculateDistance(userLat, userLng, landmark.latitude, landmark.longitude)
        return distance <= landmark.radius
    }

    override suspend fun importFromGeoJson(): GeoJsonImporter.ImportResult {
        return GeoJsonImporter.ImportResult.AlreadyImported(moscowLandmarks.size)
    }

    override suspend fun getLandmarksCount(): Int {
        return moscowLandmarks.size
    }

    private fun LandmarkEntity.toDomainModel(): Landmark {
        return Landmark(
            id = id,
            name = name,
            description = description,
            latitude = latitude,
            longitude = longitude,
            radius = 50f,
            isVisited = isCompleted,
            visitedAt = if (isCompleted) System.currentTimeMillis() else null
        )
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val earthRadius = 6371000
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }
}
*/
