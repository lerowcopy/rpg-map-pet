package com.example.rpg_map_pet.presentation.map.cluster

import com.yandex.mapkit.geometry.Point
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

/**
 * Сеточный алгоритм кластеризации.
 * Разбивает карту на ячейки и группирует метки внутри каждой ячейки.
 */
class GridBasedClusterAlgorithm(
    private val config: ClusterConfig = ClusterConfig()
) : ClusterAlgorithm {

    /**
     * Скластеризовать набор элементов.
     */
    override fun cluster(items: List<ClusterItem>, zoom: Int): List<Cluster> {
        // Если zoom больше порога, не кластеризуем
        if (zoom > config.minZoomForClustering) {
            return items.map { item ->
                Cluster(item.point, listOf(item), zoom)
            }
        }

        // Размер сетки зависит от zoom
        val gridSize = calculateGridSize(zoom)

        // Группируем элементы по ячейкам сетки
        val grid = mutableMapOf<GridKey, MutableList<ClusterItem>>()

        for (item in items) {
            val key = getGridKey(item.point, gridSize)
            val list = grid.getOrPut(key) { mutableListOf() }
            list.add(item)
        }

        // Создаём кластеры из ячеек
        val clusters = mutableListOf<Cluster>()

        for ((_, clusterItems) in grid) {
            if (clusterItems.size >= config.minClusterSize) {
                // Это кластер
                val center = ClusterCalculator.calculateCenter(clusterItems)
                clusters.add(Cluster(center, clusterItems, zoom))
            } else {
                // Одиночные метки
                for (item in clusterItems) {
                    clusters.add(Cluster(item.point, listOf(item), zoom))
                }
            }
        }

        return clusters
    }

    /**
     * Расчитать размер сетки в градусах для данного zoom.
     * Чем меньше zoom, тем больше размер ячейки.
     */
    private fun calculateGridSize(zoom: Int): Double {
        // Базовый размер на zoom 13
        val baseSize = 0.01

        // Каждый уровень zoom уменьшает размер в 2 раза
        val scaleFactor = 2.0.pow(13 - zoom)

        return baseSize * scaleFactor
    }

    /**
     * Получить ключ ячейки сетки для точки.
     */
    private fun getGridKey(point: Point, gridSize: Double): GridKey {
        val latIndex = (point.latitude / gridSize).floorToInt()
        val lonIndex = (point.longitude / gridSize).floorToInt()
        return GridKey(latIndex, lonIndex)
    }

    /**
     * Ключ ячейки сетки.
     */
    private data class GridKey(
        val latIndex: Int,
        val lonIndex: Int
    )

    /**
     * Округлить Double вниз до Int.
     */
    private fun Double.floorToInt(): Int {
        return floor(this).toInt()
    }
}
