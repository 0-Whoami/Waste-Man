package com.example.wasteman

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
actual fun pickImageFile(onImageLoaded: (String) -> Unit) {
    try {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val files = input.files
            if (files != null && files.length > 0) {
                val file = files[0]
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        val result = reader.result.toString()
                        onImageLoaded(result)
                    }
                    reader.readAsDataURL(file)
                }
            }
        }
        input.click()
    } catch (_: Exception) {
        onImageLoaded("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
    }
}

actual fun getCurrentGpsLocation(onLocationReceived: (Double, Double) -> Unit) {
    onLocationReceived(12.9716, 77.5946)
}

actual fun openInGoogleMaps(latitude: Double, longitude: Double, address: String?) {
    try {
        val query = if (!address.isNullOrBlank()) address else "$latitude,$longitude"
        val url = "https://www.google.com/maps/search/?api=1&query=$query"
        window.open(url, "_blank")
    } catch (_: Exception) {
    }
}
