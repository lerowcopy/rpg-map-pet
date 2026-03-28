package com.example.rpg_map_pet.presentation.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
    val landmarksCollection = remember {
        mapView.mapWindow.map.mapObjects.addCollection()
    }

    var hasCentered by rememberSaveable { mutableStateOf(false) }
    var hasRestoredCamera by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(hasCentered, uiState.landmarks) {
        Log.d("YandexMapView", hasCentered.toString())
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
        val cameraListener = com.yandex.mapkit.map.CameraListener { _, cameraPosition, _, _ ->
            val savedPos = viewModel.restoreCameraPosition()
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

    LaunchedEffect(uiState.landmarks) {
        Log.d("YandexMapView", "🗺️ Updating landmarks: ${uiState.landmarks.size}")
        landmarksCollection.clear()
        tapListeners.clear()

        uiState.landmarks.forEach { landmark ->
            val listener = com.yandex.mapkit.map.MapObjectTapListener { mapObject, _ ->
                val lm = mapObject.userData as? Landmark
                Log.d("YandexMapView", "👆 Tap on: ${lm?.name}")
                lm?.let {
                    if (viewModel.restoreCameraPosition() == null) {
                        val cameraPos = map.cameraPosition
                        viewModel.saveCameraPosition(
                            cameraPos.target.latitude,
                            cameraPos.target.longitude,
                            cameraPos.zoom
                        )
                    }
                    hasRestoredCamera = false
                    onNavigateToLandmark(it)
                }
                true
            }

            val placemark = landmarksCollection.addPlacemark().apply {
                geometry = Point(landmark.latitude, landmark.longitude)
                userData = landmark

                setIcon(
                    ImageProvider.fromBitmap(
                        createColoredDot(
                            if (landmark.isVisited) Color.GREEN else Color.RED
                        )
                    )
                )

                addTapListener(listener)
            }

            tapListeners.add(listener)
        }

        Log.d("YandexMapView", "✨ All ${uiState.landmarks.size} landmarks added to map")
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.height(60.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                IconButton(
                    modifier = Modifier.width(100.dp),
                    onClick = {
                        val cameraPos = map.cameraPosition
                        viewModel.saveCurrentCameraPosition(
                            cameraPos.target.latitude,
                            cameraPos.target.longitude,
                            cameraPos.zoom
                        )
                        Log.d("YandexMapView", cameraPos.toString())
                        onNavigateToVisited()
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Посещённые места"
                        )
                        Text(
                            text = "Посещённые",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📍 Меток: ${uiState.landmarks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🔍 Zoom: ${String.format("%.1f", uiState.cameraPosition?.zoom ?: 0f)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
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
                    .padding(16.dp)
            ) {
                Text("📍 Я здесь")
            }
        }
    }
}

private fun createColoredDot(color: Int): Bitmap {
    val size = 48
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

    val borderPaint = Paint().apply {
        this.color = Color.WHITE
        isAntiAlias = true
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 3f, borderPaint)

    return bitmap
}
