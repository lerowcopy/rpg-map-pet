package com.example.rpg_map_pet.presentation.map.cluster

/**
 * Интерфейс алгоритма кластеризации.
 */
interface ClusterAlgorithm {
    /**
     * Скластеризовать набор элементов.
     *
     * @param items элементы для кластеризации
     * @param zoom текущий уровень зума карты
     * @return список кластеров
     */
    fun cluster(items: List<ClusterItem>, zoom: Int): List<Cluster>
}

/**
 * Конфигурация кластеризации.
 */
data class ClusterConfig(
    val minZoomForClustering: Int = 12,      // До какого zoom кластеризовать
    val gridSize: Int = 60,                  // Размер ячейки в пикселях
    val minClusterSize: Int = 2              // Мин. меток для кластера
)
