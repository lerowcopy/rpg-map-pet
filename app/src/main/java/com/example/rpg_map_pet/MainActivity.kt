package com.example.rpg_map_pet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.presentation.model.MapUiState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.example.rpg_map_pet.presentation.model.UserLocationState
import com.example.rpg_map_pet.presentation.ui.MapViewModel
import com.example.rpg_map_pet.presentation.ui.isLocationEnabled
import com.example.rpg_map_pet.ui.theme.RpgmappetTheme
import com.yandex.mapkit.map.PlacemarkMapObject
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            checkLocationEnabled()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkLocationPermission()

        setContent {
            RpgmappetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocation != PackageManager.PERMISSION_GRANTED || 
            coarseLocation != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            checkLocationEnabled()
        }
    }

    private fun checkLocationEnabled() {
        val viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        if (!isLocationEnabled(this)) {
            viewModel.showEnableGpsDialog()
        } else {
            viewModel.hideEnableGpsDialog()
            viewModel.loadUserLocation()
            viewModel.startLocationUpdates()
        }
    }
}

@Composable
fun MapScreen() {
    val viewModel = ViewModelProvider(LocalViewModelStoreOwner.current!!)[MapViewModel::class.java]
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

    // Диалог информации о достопримечательности
    val selectedLandmark = uiState.selectedLandmark
    if (uiState.showLandmarkInfo && selectedLandmark != null) {
        LandmarkInfoDialog(
            landmark = selectedLandmark,
            onDismiss = {
                viewModel.dismissLandmarkInfo()
            },
            onMarkAsVisited = {
                viewModel.markAsVisited(selectedLandmark.id)
                viewModel.dismissLandmarkInfo()
            }
        )
    }
    
    // Логирование для отладки
    LaunchedEffect(uiState.showLandmarkInfo, selectedLandmark) {
        Log.d("MapScreen", "showLandmarkInfo: ${uiState.showLandmarkInfo}, selectedLandmark: ${selectedLandmark?.name}")
    }

    YandexMapView(
        uiState = uiState,
        onLandmarkClick = { landmark ->
            viewModel.selectLandmark(landmark)
        },
        onCenterOnUser = {
            viewModel.centerOnUserLocation()
        }
    )
}

@Composable
fun EnableGpsDialog(
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

@SuppressLint("DefaultLocale")
@Composable
fun LandmarkInfoDialog(
    landmark: com.example.rpg_map_pet.domain.model.Landmark,
    onDismiss: () -> Unit,
    onMarkAsVisited: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = landmark.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (landmark.isVisited) {
                    Badge(
                        modifier = Modifier.padding(bottom = 12.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("✓ Посещено")
                    }
                }

                Text(
                    text = landmark.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Широта: ${String.format("%.6f", landmark.latitude)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Долгота: ${String.format("%.6f", landmark.longitude)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!landmark.isVisited) {
                        Button(
                            onClick = onMarkAsVisited,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Отметить как посещённое")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}

@Composable
fun YandexMapView(
    uiState: MapUiState,
    onLandmarkClick: (Landmark) -> Unit,
    onCenterOnUser: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle

    val mapView = remember { MapView(context) }

    val map = remember { mapView.mapWindow.map }

    val tapListeners = remember {
        mutableStateListOf<com.yandex.mapkit.map.MapObjectTapListener>()
    }
    val landmarksCollection = remember {
        mapView.mapWindow.map.mapObjects.addCollection()
    }

    var hasCentered by remember { mutableStateOf(false) }

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

    // ✅ центрирование
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
        }
    }

    // ✅ обновление камеры
    uiState.cameraPosition?.let { pos ->
        LaunchedEffect(pos.latitude, pos.longitude) {
            map.move(
                CameraPosition(
                    Point(pos.latitude, pos.longitude),
                    pos.zoom, 0f, 0f
                ),
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
        }
    }

    LaunchedEffect(uiState.landmarks) {
        Log.d("YandexMapView", "Updating landmarks: ${uiState.landmarks.size}")

        landmarksCollection.clear()
        tapListeners.clear()

        uiState.landmarks.forEach { landmark ->

            val listener = com.yandex.mapkit.map.MapObjectTapListener { mapObject, _ ->
                val lm = mapObject.userData as? Landmark
                Log.d("YandexMapView", "Tap on: ${lm?.name}")
                lm?.let { onLandmarkClick(it) }
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

        Log.d("YandexMapView", "All landmarks added")
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = onCenterOnUser,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            enabled = uiState.userLocation is UserLocationState.Success
        ) {
            Text("📍 Я здесь")
        }
    }
}

fun createColoredDot(color: Int): Bitmap {
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
