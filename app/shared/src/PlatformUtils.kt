package com.example.wasteman

expect fun pickImageFile(onImageLoaded: (String) -> Unit)

expect fun getCurrentGpsLocation(onLocationReceived: (Double, Double) -> Unit)

expect fun openInGoogleMaps(latitude: Double, longitude: Double, address: String? = null)

expect fun showGoogleMapPicker(onLocationPicked: (lat: Double, lng: Double, street: String, landmark: String, area: String, pincode: String) -> Unit)
