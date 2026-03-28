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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rpg_map_pet.domain.model.Landmark
import com.example.rpg_map_pet.presentation.map.MapUiState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.example.rpg_map_pet.presentation.map.UserLocationState
import com.example.rpg_map_pet.presentation.map.MapViewModel
import com.example.rpg_map_pet.ui.theme.RpgmappetTheme
import com.yandex.mapkit.map.PlacemarkMapObject
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Разрешения получены, инициализация происходит в AppNavHost через LaunchedEffect
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
                    AppNavHost()
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
        }
        // Инициализация локации происходит в AppNavHost через LaunchedEffect
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "map"
    ) {
        composable("map") {
            val viewModel: MapViewModel = hiltViewModel()
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
                onNavigateToLandmark = { landmark ->
                    // Кодируем ID для безопасной передачи в роуте
                    val encodedId = java.net.URLEncoder.encode(landmark.id, "UTF-8")
                    navController.navigate("landmark/$encodedId")
                }
            )
        }

        composable(
            route = "landmark/{landmarkId}"
        ) { backStackEntry ->
            val landmarkId = backStackEntry.arguments?.getString("landmarkId")
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                ?: return@composable
            val viewModel: MapViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            val landmark = uiState.landmarks.find { it.id == landmarkId }
            if (landmark != null) {
                LandmarkDetailScreen(
                    landmark = landmark,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onMarkAsVisited = {
                        viewModel.markAsVisited(landmarkId)
                    }
                )
            }
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun LandmarkDetailScreen(
    landmark: Landmark,
    onNavigateBack: () -> Unit,
    onMarkAsVisited: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Информация о метке") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = landmark.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (landmark.isVisited) {
                AssistChip(
                    onClick = { },
                    label = { Text("✓ Посещено") },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = landmark.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            if (!landmark.isVisited) {
                Button(
                    onClick = {
                        onMarkAsVisited()
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отметить как посещённое")
                }
            }
        }
    }
}

@Composable
fun YandexMapView(
    uiState: MapUiState,
    viewModel: MapViewModel,
    onNavigateToLandmark: (Landmark) -> Unit
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

    var hasCentered by rememberSaveable { mutableStateOf(false) }
    var hasRestoredCamera by rememberSaveable { mutableStateOf(false) }

    // Отслеживаем изменение камеры для загрузки меток
    DisposableEffect(map) {
        val cameraListener = com.yandex.mapkit.map.CameraListener { _, cameraPosition, _, _ ->
            // Отправляем позицию камеры в ViewModel для debounce загрузки
            // cameraPosition.target - это Point с координатами центра
            viewModel.updateCameraPositionForLoading(
                cameraPosition.target.latitude,
                cameraPosition.target.longitude,
                cameraPosition.zoom
            )
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

    // ✅ центрирование только при первом запуске
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

    // ✅ Восстановление позиции камеры после возврата с экрана метки
    LaunchedEffect(hasRestoredCamera, hasCentered) {
        val savedPos = viewModel.restoreCameraPosition()
        if (savedPos != null && !hasRestoredCamera && hasCentered) {
            map.move(
                CameraPosition(
                    Point(savedPos.latitude, savedPos.longitude),
                    savedPos.zoom, 0f, 0f
                ),
                Animation(Animation.Type.SMOOTH, 0f),
                null
            )
            hasRestoredCamera = true
        }
    }

    // Сброс флага при уходе с экрана (чтобы восстановить при следующем возврате)
    DisposableEffect(Unit) {
        onDispose {
            hasRestoredCamera = false
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
                    // Сохраняем текущую позицию камеры перед переходом
                    val cameraPos = map.cameraPosition
                    viewModel.saveCameraPosition(
                        cameraPos.target.latitude,
                        cameraPos.target.longitude,
                        cameraPos.zoom
                    )
                    // Сбрасываем флаг восстановления
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

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Счётчик меток в углу экрана
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
                }
            },
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
