package com.example.wasteman.platform

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.example.wasteman.theme.EcoDarkGreen

interface ReportPlatformHandler {
    fun launchImagePicker(onImagePicked: (bytes: ByteArray, bitmap: ImageBitmap) -> Unit)
    fun fetchCurrentLocation(onLocationFetched: (addressOrCoords: String) -> Unit)
    fun launchMapLocationPicker(onLocationPicked: (addressOrCoords: String) -> Unit)
    fun decodeBitmap(bytes: ByteArray): ImageBitmap? = null
}

expect fun createPlatformHandler(): ReportPlatformHandler

class DemoPlatformHandler : ReportPlatformHandler {
    override fun launchImagePicker(onImagePicked: (bytes: ByteArray, bitmap: ImageBitmap) -> Unit) {
        val demoBitmap = createDemoBitmap(EcoDarkGreen, width = 200, height = 120)
        onImagePicked(ByteArray(0), demoBitmap)
    }

    override fun fetchCurrentLocation(onLocationFetched: (addressOrCoords: String) -> Unit) {
        onLocationFetched("22.5726° N, 88.3639° E • Salt Lake Sector V")
    }

    override fun launchMapLocationPicker(onLocationPicked: (addressOrCoords: String) -> Unit) {
        onLocationPicked("Salt Lake Sector V, Kolkata (22.5726, 88.3639)")
    }
}

fun createDemoBitmap(color: Color, width: Int = 120, height: Int = 120): ImageBitmap =
    ImageBitmap(width, height).apply {
        Canvas(this).drawRect(
            left = 0f,
            top = 0f,
            right = width.toFloat(),
            bottom = height.toFloat(),
            paint = Paint().apply { this.color = color }
        )
    }
