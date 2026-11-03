package com.example.pixeltracker.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.service.LocationTrackingService
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    
    val routeEntities by db.trackerDao().getAllRoutePoints().collectAsState(initial = emptyList())
    val routePoints = routeEntities.map { LatLng(it.latitude, it.longitude) }

    val defaultLocation = if (routePoints.isNotEmpty()) routePoints.last() else LatLng(55.9873, 37.1895)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    var isTracking by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            if (routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    color = android.graphics.Color.WHITE,
                    width = 12f
                )
            }
        }

        // Верхняя панель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "МАРШРУТ GPS",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Нижние кнопки управления треком в e-ink стиле
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .border(1.dp, Color.White)
                    .clickable {
                        val intent = Intent(context, LocationTrackingService::class.java)
                        if (isTracking) {
                            context.stopService(intent)
                            isTracking = false
                        } else {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                            isTracking = true
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (isTracking) "СТОП" else "СТАРТ",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .border(1.dp, Color.White)
                    .clickable {
                        coroutineScope.launch(Dispatchers.IO) {
                            db.trackerDao().clearRoutePoints()
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "ОЧИСТИТЬ",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
