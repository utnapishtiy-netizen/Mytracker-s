package com.example.pixeltracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    modifier: Modifier = Modifier
) {
    // Начальная позиция (например, Зеленоград)
    val defaultLocation = LatLng(55.9873, 37.1895)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // Пример точек маршрута для отображения трека
    val routePoints = remember {
        mutableStateListOf(
            LatLng(55.9873, 37.1895),
            LatLng(55.9880, 37.1910),
            LatLng(55.9895, 37.1930),
            LatLng(55.9910, 37.1920)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Компонент Google Maps в монохромном e-ink стиле
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Отрисовка линии трека маршрута
            if (routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    color = android.graphics.Color.WHITE,
                    width = 12f
                )
            }
        }

        // Верхняя панель заголовка в минималистичном стиле
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "МАРШРУТ ПРОГУЛКИ",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
