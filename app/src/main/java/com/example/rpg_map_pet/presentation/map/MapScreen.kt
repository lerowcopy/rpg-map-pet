package com.example.rpg_map_pet.presentation.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.presentation.map.MapViewModel
import com.example.rpg_map_pet.presentation.map.MapUiState
import com.example.rpg_map_pet.presentation.map.UserLocationState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.example.rpg_map_pet.presentation.map.cluster.Cluster
import com.example.rpg_map_pet.presentation.map.cluster.ClusterRenderer
import com.yandex.mapkit.map.CameraListener

@Composable
fun MapScreen(
    onNavigateToLandmark: (Landmark) -> Unit,
    onNavigateToVisited: () -> Unit
) {
    val viewModel: MapViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val showEnableGpsDialog by viewModel.showEnableGpsDialog.collectAsState()
    val context = LocalContext.current

    if (showEnableGpsDialog) {
        EnableGpsDialog(
            onConfirm = {
                viewModel.hideEnableGpsDialog()
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            onDismiss = {
                viewModel.hideEnableGpsDialog()
            }
        )
    }

    YandexMapView(
        uiState = uiState,
        viewModel = viewModel,
        onNavigateToLandmark = onNavigateToLandmark,
        onNavigateToVisited = onNavigateToVisited
    )
}

@Composable
private fun EnableGpsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("GPS выключен") },
        text = { Text("Для работы приложения необходимо включить геолокацию. Включить GPS?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Включить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun YandexMapView(
    uiState: MapUiState,
    viewModel: MapViewModel,
    onNavigateToLandmark: (Landmark) -> Unit,
    onNavigateToVisited: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember { MapView(context) }
    val map = remember { mapView.mapWindow.map }

    val tapListeners = remember {
        mutableStateListOf<com.yandex.mapkit.map.MapObjectTapListener>()
    }

    // Коллекция для кластеров
    val clusterCollection = remember {
        mapView.mapWindow.map.mapObjects.addCollection()
    }
    val clusterRenderer = remember {
        ClusterRenderer(clusterCollection)
    }

    var hasCentered by rememberSaveable { mutableStateOf(false) }
    var hasRestoredCamera by rememberSaveable { mutableStateOf(false) }

    // Обработчик тапа по кластеру
    val onClusterTap: (Cluster) -> Unit = { cluster ->
        if (cluster.isSingle) {
            // Одиночная метка — переходим к деталям
            val landmark = cluster.items.first().landmark
            if (viewModel.restoreCameraPosition() == null) {
                val cameraPos = map.cameraPosition
                viewModel.saveCameraPosition(
                    cameraPos.target.latitude,
                    cameraPos.target.longitude,
                    cameraPos.zoom
                )
            }
            hasRestoredCamera = false
            onNavigateToLandmark(landmark)
        } else {
            // Кластер — зумим на него
            map.move(
                com.yandex.mapkit.map.CameraPosition(
                    cluster.center,
                    map.cameraPosition.zoom + 2,
                    0f,
                    0f
                ),
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
        }
    }

    // Коллекция для маркера местоположения пользователя
    val userLocationCollection = remember {
        mapView.mapWindow.map.mapObjects.addCollection()
    }

    LaunchedEffect(uiState.userLocation) {
        val loc = uiState.userLocation
        if (loc is UserLocationState.Success && !hasCentered) {
            map.move(
                CameraPosition(
                    Point(loc.latitude, loc.longitude),
                    16f, 0f, 0f
                ),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
            hasCentered = true
            Log.d("YandexMapView", "🎯 Centered on user location")
        }
    }

    // Обновление маркера местоположения пользователя
    LaunchedEffect(uiState.userLocationPoint) {
        userLocationCollection.clear()
        uiState.userLocationPoint?.let { point ->
            userLocationCollection.addPlacemark().apply {
                geometry = point
                setIcon(ImageProvider.fromBitmap(createUserLocationMarker()))
            }
            Log.d("YandexMapView", "📍 User location marker updated: ${point.latitude}, ${point.longitude}")
        }
    }

    LaunchedEffect(hasCentered, uiState.landmarks) {
        if (hasCentered && uiState.landmarks.isNotEmpty()) {
            val savedPos = viewModel.restoreCameraPosition()
            if (savedPos != null && !hasRestoredCamera) {
                Log.d("YandexMapView", savedPos.toString())
                map.move(
                    CameraPosition(
                        Point(savedPos.latitude, savedPos.longitude),
                        savedPos.zoom, 0f, 0f
                    ),
                    Animation(Animation.Type.SMOOTH, 0f),
                    null
                )
                hasRestoredCamera = true
                viewModel.cameraPositionRestored()
                Log.d(
                    "YandexMapView",
                    "🔄 Camera restored to ${savedPos.latitude}, ${savedPos.longitude}, zoom=${savedPos.zoom}"
                )
            }
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasRestoredCamera = false
                Log.d("YandexMapView", "🔄 hasRestoredCamera reset")
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(map) {
        val cameraListener = CameraListener { _, cameraPosition, _, _ ->
            val savedPos = viewModel.restoreCameraPosition()
            Log.d("camera", savedPos.toString())
            if (savedPos == null) {
                viewModel.updateCameraPositionForLoading(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude,
                    cameraPosition.zoom
                )
            } else {
                viewModel.updateCameraPositionForLoading(
                    savedPos.latitude,
                    savedPos.longitude,
                    savedPos.zoom
                )
            }
        }

        map.addCameraListener(cameraListener)

        onDispose {
            map.removeCameraListener(cameraListener)
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    mapView.onStart()
                    MapKitFactory.getInstance().onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> {}
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.clusters) {
        Log.d("YandexMapView", "🗺️ Updating clusters: ${uiState.clusters.size}")
        clusterRenderer.render(uiState.clusters, onClusterTap)
        tapListeners.clear()

        val clusterCount = uiState.clusters.count { !it.isSingle }
        val singleCount = uiState.clusters.count { it.isSingle }
        Log.d("YandexMapView", "✨ Rendered: $clusterCount clusters, $singleCount single markers")
    }

    Scaffold { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // Плавающая кнопка "Я здесь"
            FloatingActionButton(
                onClick = {
                    val loc = uiState.userLocation
                    if (loc is UserLocationState.Success) {
                        map.move(
                            CameraPosition(
                                Point(loc.latitude, loc.longitude),
                                16f, 0f, 0f
                            ),
                            Animation(Animation.Type.SMOOTH, 0.5f),
                            null
                        )
                    } else {
                        Log.d("YandexMapView", "📍 Button clicked, loading location...")
                        viewModel.loadUserLocation()
                        viewModel.startLocationUpdates()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Я здесь",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Плавающая кнопка "Посещённые места"
            FloatingActionButton(
                onClick = {
                    val cameraPos = map.cameraPosition
                    viewModel.saveCurrentCameraPosition(
                        cameraPos.target.latitude,
                        cameraPos.target.longitude,
                        cameraPos.zoom
                    )
                    Log.d("YandexMapView", "🗺️ Navigate to visited: $cameraPos")
                    onNavigateToVisited()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(25.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Посещённые места",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Ссылка на условия использования Яндекс Карт
            val termsUrl = "https://yandex.ru/legal/maps_api/"
            val annotatedString = remember {
                androidx.compose.ui.text.AnnotatedString("Условия использования")
            }
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(end = 8.dp, bottom = 4.dp)
                    .wrapContentWidth()
            )
        }
    }
}

/**
 * Создать маркер местоположения пользователя (синяя точка с белой обводкой).
 */
private fun createUserLocationMarker(): Bitmap {
    val size = 48
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    // Синий круг
    val bluePaint = Paint().apply {
        this.color = Color.parseColor("#4285F4") // Google Blue
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, bluePaint)

    // Белая обводка
    val borderPaint = Paint().apply {
        this.color = Color.WHITE
        isAntiAlias = true
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, borderPaint)

    return bitmap
}
