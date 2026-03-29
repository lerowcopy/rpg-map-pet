package com.example.rpg_map_pet.presentation.quest

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.core.content.FileProvider
import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.presentation.map.LandmarkPhoto
import com.example.rpg_map_pet.presentation.map.MapViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkDetailScreen(
    landmarkId: String,
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val landmark = uiState.landmarks.find { it.id == landmarkId }

    // Загружаем фотографии при открытии экрана
    LaunchedEffect(landmarkId) {
        viewModel.loadLandmarkPhotos(landmarkId)
    }

    val scope = rememberCoroutineScope()

    // Переменная для хранения текущего файла фотографии
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    // Лаунчер для съёмки через камеру
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentPhotoFile?.let { photoFile ->
                viewModel.addLandmarkPhoto(landmarkId, photoFile.absolutePath)
                currentPhotoFile = null
            }
        }
    }

    // Запрос разрешения на камеру
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraPermissionGranted = isGranted
        if (isGranted) {
            val photoFile = createCameraPhotoFile(context, landmarkId)
            currentPhotoFile = photoFile
            photoFile.parentFile?.mkdirs()
            val photoUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        }
    }

    // Лаунчер для выбора нескольких фото из галереи
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri?> ->
        val photosDir = File(context.filesDir, "landmark_photos")
        photosDir.mkdirs()
        val safeLandmarkId = landmarkId.replace('/', '_')
        val baseTime = System.currentTimeMillis()

        // Копируем и сохраняем все файлы, затем добавляем в БД
        scope.launch {
            uris.forEachIndexed { index, uri ->
                uri?.let { selectedUri ->
                    val photoFile = File(photosDir, "${safeLandmarkId}_${baseTime + (index * 1000)}.jpg")

                    context.contentResolver.openInputStream(selectedUri)?.use { input ->
                        photoFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val photoPath = photoFile.absolutePath
                    viewModel.addLandmarkPhoto(landmarkId, photoPath)
                }
            }
        }
    }

    landmark?.let {
        LandmarkDetailContent(
            landmark = it,
            onAddPhotoClick = {
                photoPickerLauncher.launch("image/*")
            },
            onCameraClick = {
                val photoFile = createCameraPhotoFile(context, landmarkId)
                currentPhotoFile = photoFile
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        photoFile.parentFile?.mkdirs()
                        val photoUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        cameraLauncher.launch(photoUri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                } else {
                    photoFile.parentFile?.mkdirs()
                    val photoUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )
                    cameraLauncher.launch(photoUri)
                }
            },
            onNavigateBack = onNavigateBack,
            viewModel = viewModel
        )
    } ?: run {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Информация о метке") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Метка не найдена")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandmarkDetailContent(
    landmark: Landmark,
    onAddPhotoClick: () -> Unit,
    onCameraClick: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel
) {
    val photos by viewModel.landmarkPhotos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Информация о метке") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Название метки с типом
            item {
                val displayName = formatLandmarkName(landmark.name, landmark.type)
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Описание
            item {
                Text(
                    text = landmark.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Координаты
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📍 Широта: ${String.format("%.6f", landmark.latitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 Долгота: ${String.format("%.6f", landmark.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Кнопки добавления фото
            if (landmark.isVisited) {
                // Кнопка "Добавить фото"
                item {
                    Button(
                        onClick = onAddPhotoClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Добавить фото",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Кнопка "Запечатлеть момент"
                item {
                    Button(
                        onClick = onCameraClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Запечатлеть момент",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                // Сетка фотографий (2 колонки)
                if (photos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Фотографии (${photos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Создаём сетку 2xN для фотографий
                    val gridItems = (photos.size + 1) / 2 // количество строк
                    for (row in 0 until gridItems) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val firstIndex = row * 2
                                val secondIndex = firstIndex + 1

                                // Первая фотография в ряду
                                if (firstIndex < photos.size) {
                                    val photo = photos[firstIndex]
                                    Box(modifier = Modifier.weight(1f)) {
                                        PhotoThumbnail(
                                            photoPath = photo.path,
                                            onDeleteClick = {
                                                viewModel.deleteLandmarkPhoto(photo.id)
                                            }
                                        )
                                    }
                                }

                                // Вторая фотография в ряду
                                if (secondIndex < photos.size) {
                                    val photo = photos[secondIndex]
                                    Box(modifier = Modifier.weight(1f)) {
                                        PhotoThumbnail(
                                            photoPath = photo.path,
                                            onDeleteClick = {
                                                viewModel.deleteLandmarkPhoto(photo.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photoPath: String,
    onDeleteClick: () -> Unit
) {
    Box {
        AsyncImage(
            model = File(photoPath),
            contentDescription = "Фотография метки",
            modifier = Modifier
                .size(width = 160.dp, height = 240.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить фото",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Создать новый файл для фотографии с камеры.
 * Уникальное имя файла гарантирует, что каждая фотография будет сохранена отдельно.
 */
private fun createCameraPhotoFile(context: android.content.Context, landmarkId: String): File {
    val photosDir = File(context.filesDir, "landmark_photos")
    photosDir.mkdirs()
    val safeLandmarkId = landmarkId.replace('/', '_')
    val timestamp = System.currentTimeMillis()
    val randomSuffix = (1000..9999).random()
    return File(photosDir, "camera_${safeLandmarkId}_${timestamp}_${randomSuffix}.jpg")
}

/**
 * Отформатировать название метки с добавлением типа.
 */
private fun formatLandmarkName(name: String, type: String): String {
    if (type.isBlank()) return name

    val typeText = when (type.lowercase()) {
        "monument" -> "Памятник"
        "memorial" -> "Мемориал"
        "bust" -> "Бюст"
        "statue" -> "Статуя"
        "attraction" -> "Достопримечательность"
        "building" -> "Здание"
        "church" -> "Храм"
        "cathedral" -> "Собор"
        "museum" -> "Музей"
        "park" -> "Парк"
        "square" -> "Площадь"
        "bridge" -> "Мост"
        "fountain" -> "Фонтан"
        "ruins" -> "Руины"
        "castle" -> "Замок"
        "tower" -> "Башня"
        else -> type.replaceFirstChar { it.uppercaseChar() }
    }

    return "$typeText: $name"
}
