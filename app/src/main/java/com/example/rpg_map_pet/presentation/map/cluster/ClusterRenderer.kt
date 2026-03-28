package com.example.rpg_map_pet.presentation.map.cluster

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider

/**
 * Рендерер для отрисовки кластеров на карте.
 */
class ClusterRenderer(
    private val collection: MapObjectCollection,
    private val onClusterTap: (Cluster) -> Unit = {}
) {
    private val placemarks = mutableListOf<PlacemarkMapObject>()
    private val tapListeners = mutableListOf<MapObjectTapListener>()

    /**
     * Отрисовать кластеры на карте.
     */
    fun render(clusters: List<Cluster>, onClusterTap: (Cluster) -> Unit = this.onClusterTap) {
        // Очищаем старые маркеры
        clear()

        // Рисуем новые кластеры
        for (cluster in clusters) {
            val placemark = collection.addPlacemark()
            placemark.geometry = cluster.center
            placemark.userData = cluster

            if (cluster.isSingle) {
                // Одиночная метка — стандартный красный пин
                placemark.setIcon(ImageProvider.fromBitmap(createDefaultPin()))
            } else {
                // Кластер — кружок с цифрой
                val icon = ClusterIconGenerator.generateIcon(cluster)
                placemark.setIcon(ImageProvider.fromBitmap(icon))
            }

            // Добавляем обработчик тапа
            val listener = MapObjectTapListener { _, _ ->
                onClusterTap(cluster)
                true
            }
            placemark.addTapListener(listener)
            tapListeners.add(listener)

            placemarks.add(placemark)
        }
    }

    /**
     * Очистить все маркеры.
     */
    fun clear() {
        // Сначала удаляем обработчики тапов
        placemarks.forEachIndexed { i, placemark ->
            placemark.removeTapListener(tapListeners.getOrNull(i) ?: return@forEachIndexed)
        }
        
        // Затем удаляем плакмарки из коллекции
        placemarks.forEach { placemark ->
            collection.remove(placemark)
        }
        
        placemarks.clear()
        tapListeners.clear()
    }

    /**
     * Создать стандартный красный пин для одиночных меток.
     */
    private fun createDefaultPin(): Bitmap {
        val size = 48
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)

        // Красный круг
        val paint = Paint().apply {
            this.color = Color.RED
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

        // Белая обводка
        val borderPaint = Paint().apply {
            this.color = Color.WHITE
            isAntiAlias = true
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 3f, borderPaint)

        return bitmap
    }
}
