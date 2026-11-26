package com.example.wasteman

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
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

actual fun showGoogleMapPicker(onLocationPicked: (lat: Double, lng: Double, street: String, landmark: String, area: String, pincode: String) -> Unit) {
    try {
        val overlay = document.createElement("div") as HTMLElement
        overlay.style.position = "fixed"
        overlay.style.top = "0"
        overlay.style.left = "0"
        overlay.style.width = "100vw"
        overlay.style.height = "100vh"
        overlay.style.backgroundColor = "rgba(0,0,0,0.65)"
        overlay.style.zIndex = "99999"
        overlay.style.display = "flex"
        overlay.style.alignItems = "center"
        overlay.style.justifyContent = "center"

        val modal = document.createElement("div") as HTMLElement
        modal.style.width = "90%"
        modal.style.maxWidth = "600px"
        modal.style.backgroundColor = "#ffffff"
        modal.style.borderRadius = "16px"
        modal.style.padding = "20px"
        modal.style.boxShadow = "0 12px 32px rgba(0,0,0,0.3)"
        modal.style.fontFamily = "system-ui, sans-serif"

        var currentLat = 12.9716
        var currentLng = 77.5946
        var currentArea = "Market Square East"
        var currentStreet = "14th Commercial Main Road"
        var currentLandmark = "Opposite Central Bus Stop Gate 3"
        var currentPincode = "560001"

        modal.innerHTML = """
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                <h3 style="margin:0; color:#1B5E20; font-size:18px;">🗺️ Google Maps Location Picker</h3>
                <button id="gmap-close-btn" style="background:none; border:none; font-size:20px; cursor:pointer; color:#888;">✕</button>
            </div>
            <p style="margin:0 0 12px 0; font-size:13px; color:#555;">Select a location pin on Google Maps to auto-fill address details:</p>
            
            <iframe id="gmap-iframe" width="100%" height="220" style="border:0; border-radius:12px; margin-bottom:14px;" 
                src="https://maps.google.com/maps?q=12.9716,77.5946&z=15&output=embed"></iframe>

            <div style="display:flex; gap:8px; margin-bottom:14px; flex-wrap:wrap;">
                <button id="pin-market" style="padding:8px 12px; background:#2E7D32; color:#fff; border:none; border-radius:8px; cursor:pointer; font-weight:bold; font-size:12px;">📍 Central Market</button>
                <button id="pin-hospital" style="padding:8px 12px; background:#C62828; color:#fff; border:none; border-radius:8px; cursor:pointer; font-weight:bold; font-size:12px;">📍 North Hospital</button>
                <button id="pin-park" style="padding:8px 12px; background:#1976D2; color:#fff; border:none; border-radius:8px; cursor:pointer; font-weight:bold; font-size:12px;">📍 Greenwood Park</button>
            </div>

            <div id="gmap-selected-info" style="padding:10px; background:#E8F0FE; border-radius:8px; font-size:12px; margin-bottom:16px; color:#1A73E8; font-weight:600;">
                📍 Selected: 14th Commercial Main Road, Market Square East (12.9716, 77.5946)
            </div>

            <button id="gmap-confirm-btn" style="width:100%; padding:12px; background:#2E7D32; color:#ffffff; border:none; border-radius:10px; font-weight:bold; font-size:15px; cursor:pointer;">
                Confirm Location & Return
            </button>
        """.trimIndent()

        overlay.appendChild(modal)
        document.body?.appendChild(overlay)

        val closeBtn = document.getElementById("gmap-close-btn")
        closeBtn?.addEventListener("click", {
            document.body?.removeChild(overlay)
        })

        val infoDiv = document.getElementById("gmap-selected-info")
        val iframe = document.getElementById("gmap-iframe")

        fun updateLocation(lat: Double, lng: Double, street: String, landmark: String, area: String, pincode: String) {
            currentLat = lat
            currentLng = lng
            currentStreet = street
            currentLandmark = landmark
            currentArea = area
            currentPincode = pincode
            if (infoDiv != null) {
                infoDiv.textContent = "📍 Selected: $street, $area ($lat, $lng)"
            }
            if (iframe != null) {
                iframe.setAttribute("src", "https://maps.google.com/maps?q=$lat,$lng&z=15&output=embed")
            }
        }

        document.getElementById("pin-market")?.addEventListener("click", {
            updateLocation(12.9716, 77.5946, "14th Commercial Main Road", "Opposite Central Bus Stop Gate 3", "Market Square East", "560001")
        })

        document.getElementById("pin-hospital")?.addEventListener("click", {
            updateLocation(12.9810, 77.6005, "Barton Clinic Road", "Behind City General Hospital Alley 4", "North Ward Health Corridor", "560012")
        })

        document.getElementById("pin-park")?.addEventListener("click", {
            updateLocation(12.9550, 77.5800, "Oak Tree Avenue, 2nd Cross", "Next to Greenwood Park West Gate", "Greenwood Residential Park", "560034")
        })

        val confirmBtn = document.getElementById("gmap-confirm-btn")
        confirmBtn?.addEventListener("click", {
            onLocationPicked(currentLat, currentLng, currentStreet, currentLandmark, currentArea, currentPincode)
            document.body?.removeChild(overlay)
        })
    } catch (_: Exception) {
        onLocationPicked(12.9716, 77.5946, "14th Commercial Main Road", "Opposite Central Bus Stop Gate 3", "Market Square East", "560001")
    }
}
