package com.example.rpg_map_pet.data.repository

import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.domain.repository.LandmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class LandmarkRepositoryImpl @Inject constructor() : LandmarkRepository {

    private var cachedLandmarks: List<Landmark> = emptyList()
    private var lastLoadedLat: Double = 0.0
    private var lastLoadedLng: Double = 0.0
    private val minDistanceForReload = 0.01 // ~1км

    override fun getLandmarks(): Flow<List<Landmark>> = flow {
        emit(cachedLandmarks)
    }

    override suspend fun markAsVisited(landmarkId: String) {
        val index = cachedLandmarks.indexOfFirst { it.id == landmarkId }
        if (index != -1) {
            cachedLandmarks = cachedLandmarks.map {
                if (it.id == landmarkId) {
                    it.copy(isVisited = true, visitedAt = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    override fun isUserNearLandmark(userLat: Double, userLng: Double, landmark: Landmark): Boolean {
        val distance = calculateDistance(userLat, userLng, landmark.latitude, landmark.longitude)
        return distance <= landmark.radius
    }

    suspend fun loadLandmarksNearby(lat: Double, lng: Double): Result<List<Landmark>> {
        // Проверяем, что это Москва (в пределах 20км от центра)
        if (!isMoscow(lat, lng)) {
            return Result.success(emptyList())
        }

        if (cachedLandmarks.isNotEmpty() && 
            distanceBetween(lat, lng, lastLoadedLat, lastLoadedLng) < minDistanceForReload) {
            return Result.success(cachedLandmarks)
        }

        cachedLandmarks = moscowLandmarks
        lastLoadedLat = lat
        lastLoadedLng = lng
        
        return Result.success(cachedLandmarks)
    }
    
    private fun isMoscow(lat: Double, lng: Double): Boolean {
        // Центр Москвы: 55.75, 37.62
        // Радиус ~20км = ~0.18 градусов
        val centerLat = 55.75
        val centerLng = 37.62
        val radius = 0.18
        
        return kotlin.math.abs(lat - centerLat) < radius && 
               kotlin.math.abs(lng - centerLng) < radius
    }
    
    // База достопримечательностей Москвы
    private val moscowLandmarks = listOf(
        // Красная площадь и окрестности
        Landmark("msk_001", "Красная Площадь", "Главная площадь России", 55.753930, 37.620795),
        Landmark("msk_002", "Храм Василия Блаженного", "Православный храм, памятник архитектуры XVI века", 55.752520, 37.623060),
        Landmark("msk_003", "Московский Кремль", "Резиденция Президента РФ", 55.752020, 37.617500),
        Landmark("msk_004", "ГУМ", "Главный Универсальный Магазин", 55.754570, 37.621320),
        Landmark("msk_005", "Мавзолей Ленина", "Усыпальница В.И. Ленина", 55.754200, 37.620100),
        Landmark("msk_006", "Казанский Собор", "Православный храм на Красной площади", 55.756300, 37.622500),
        Landmark("msk_007", "Исторический Музей", "Крупнейший национальный исторический музей", 55.755600, 37.621000),
        Landmark("msk_008", "Лобное Место", "Памятник архитектуры XVI века", 55.753500, 37.622000),
        
        // Театральная площадь
        Landmark("msk_009", "Большой Театр", "Один из крупнейших театров оперы и балета", 55.760300, 37.618600),
        Landmark("msk_010", "Малый Театр", "Старейший драматический театр Москвы", 55.757800, 37.620500),
        
        // Парки
        Landmark("msk_011", "Парк Зарядье", "Современный парк в центре Москвы", 55.751200, 37.625500),
        Landmark("msk_012", "Александровский Сад", "Парк у Кремлёвской стены", 55.751500, 37.615000),
        Landmark("msk_013", "Парк Горького", "Центральный парк культуры и отдыха", 55.729700, 37.601500),
        
        // Храмы
        Landmark("msk_014", "Храм Христа Спасителя", "Кафедральный собор РПЦ", 55.748500, 37.605300),
        Landmark("msk_015", "Собор Непорочного Зачатия", "Католический кафедральный собор", 55.757200, 37.590500),
        
        // Музеи
        Landmark("msk_016", "Третьяковская Галерея", "Крупнейший музей русского искусства", 55.741500, 37.620800),
        Landmark("msk_017", "Пушкинский Музей", "Музей изобразительных искусств", 55.747800, 37.604500),
        Landmark("msk_018", "Музей Современного Искусства GARAGE", "Музей современного искусства", 55.730500, 37.595500),
        
        // Памятники и мемориалы
        Landmark("msk_019", "Могила Неизвестного Солдата", "Мемориал у Кремлёвской стены", 55.751000, 37.612500),
        Landmark("msk_020", "Памятник Жукову", "Конный памятник маршалу", 55.756500, 37.620000),
        
        // Улицы и площади
        Landmark("msk_021", "Старый Арбат", "Пешеходная улица", 55.748500, 37.595000),
        Landmark("msk_022", "Тверская Улица", "Главная улица Москвы", 55.761000, 37.610000),
        Landmark("msk_023", "Манежная Площадь", "Площадь в центре Москвы", 55.756000, 37.617000),
        
        // Воробьёвы горы
        Landmark("msk_024", "Воробьёвы Горы", "Смотровая площадка", 55.719500, 37.539000),
        Landmark("msk_025", "МГУ", "Московский Государственный Университет", 55.703500, 37.530000),
        
        // ВДНХ
        Landmark("msk_026", "ВДНХ", "Выставка достижений народного хозяйства", 55.826500, 37.637500),
        Landmark("msk_027", "Фонтан Дружбы Народов", "Главный фонтан ВДНХ", 55.825000, 37.638000),
        Landmark("msk_028", "Космос (монумент)", "Памятник покорителям космоса", 55.820000, 37.640000),
        
        // Москва-Сити
        Landmark("msk_029", "Москва-Сити", "Деловой центр", 55.749500, 37.537500),
        Landmark("msk_030", "Башня Федерация", "Одна из высоток Москва-Сити", 55.748500, 37.535500),
        
        // Монастыри
        Landmark("msk_031", "Новодевичий Монастырь", "Женский монастырь, объект ЮНЕСКО", 55.726500, 37.556000),
        Landmark("msk_032", "Троице-Сергиева Лавра", "Крупнейший мужской монастырь", 55.760000, 37.625000),
        
        // Усадьбы
        Landmark("msk_033", "Царицыно", "Дворцово-парковый ансамбль", 55.607500, 37.670000),
        Landmark("msk_034", "Коломенское", "Музей-заповедник", 55.667500, 37.675000),
        Landmark("msk_035", "Кусково", "Усадьба Шереметевых", 55.755000, 37.735000),
        
        // Другие достопримечательности
        Landmark("msk_036", "Останкинская Телебашня", "Высочайшая телебашня в Европе", 55.819500, 37.610500),
        Landmark("msk_037", "Цирк Никулина", "Старейший цирк России", 55.759000, 37.625500),
        Landmark("msk_038", "Московский Планетарий", "Один из крупнейших планетариев", 55.755500, 37.590000),
        Landmark("msk_039", "Дом Пашкова", "Памятник архитектуры XVIII века", 55.748000, 37.610000),
        Landmark("msk_040", "Гостиница Украина", "Одна из семи сталинских высоток", 55.748500, 37.575000)
    )

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

    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = lat2 - lat1
        val dLng = lng2 - lng1
        return sqrt(dLat * dLat + dLng * dLng)
    }
}
