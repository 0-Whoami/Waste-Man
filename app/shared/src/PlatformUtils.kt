package com.example.wasteman

expect fun pickImageFile(onImageLoaded: (String) -> Unit)

expect fun getCurrentGpsLocation(onLocationReceived: (Double, Double) -> Unit)

expect fun openInGoogleMaps(latitude: Double, longitude: Double, address: String? = null)
