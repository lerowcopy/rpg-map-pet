package com.example.rpg_map_pet.presentation.map.cluster

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap

/**
 * Генератор иконок для кластеров.
 * Создаёт кружки с цифрами, цвет зависит от размера кластера.
 */
object ClusterIconGenerator {

    private const val ICON_SIZE = 56

    /**
     * Сгенерировать иконку для кластера.
     *
     * @param cluster кластер
     * @return Bitmap с иконкой
     */
    fun generateIcon(cluster: Cluster): Bitmap {
        val size = cluster.size

        // Цвет зависит от посещённости и размера кластера
        val color = when {
            cluster.isAllVisited -> {
                // Все метки посещённые — зелёный
                Color.parseColor("#4CAF50")
            }
            cluster.isPartiallyVisited -> {
                // Некоторые посещённые — жёлтый
                Color.parseColor("#F4B400")
            }
            else -> {
                // Ни одной посещённой — цвет от размера
                when {
                    size < 10 -> Color.parseColor("#4285F4") // Синий
                    size < 50 -> Color.parseColor("#F4B400") // Жёлтый
                    size < 100 -> Color.parseColor("#F4511E") // Оранжевый
                    else -> Color.parseColor("#D93025") // Красный
                }
            }
        }

        return createBitmapWithText(size, color)
    }

    /**
     * Создать Bitmap с цифрой внутри круга.
     */
    private fun createBitmapWithText(number: Int, color: Int): Bitmap {
        val bitmap = createBitmap(ICON_SIZE, ICON_SIZE)
        val canvas = Canvas(bitmap)

        // Рисуем круг
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        canvas.drawCircle(ICON_SIZE / 2f, ICON_SIZE / 2f, ICON_SIZE / 2f, paint)

        // Рисуем белую обводку
        val borderPaint = Paint().apply {
            this.color = Color.WHITE
            isAntiAlias = true
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(ICON_SIZE / 2f, ICON_SIZE / 2f, ICON_SIZE / 2f - 1.5f, borderPaint)

        // Рисуем текст
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            this.textAlign = Paint.Align.CENTER
            this.textSize = 24f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            // Поднимаем текст немного вверх для визуального центра
                //fakeBoldText = true
        }

        // Подбираем размер текста для больших чисел
        val text = number.toString()
        val textSize = when {
            number < 10 -> 28f
            number < 100 -> 24f
            number < 1000 -> 20f
            else -> 16f
        }
        textPaint.textSize = textSize

        // Рисуем текст по центру с точным расчётом позиции
        val fontMetrics = textPaint.fontMetricsInt
        val textY = (ICON_SIZE / 2f) - (fontMetrics.top + fontMetrics.bottom) / 2f
        canvas.drawText(text, ICON_SIZE / 2f, textY, textPaint)

        return bitmap
    }
}
