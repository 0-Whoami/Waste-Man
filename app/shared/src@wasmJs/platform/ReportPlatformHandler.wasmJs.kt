package com.example.wasteman.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.skia.Image
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.math.abs
import kotlin.math.round

actual fun createPlatformHandler(): ReportPlatformHandler = WasmJsReportPlatformHandler()

@OptIn(ExperimentalWasmJsInterop::class)
class WasmJsReportPlatformHandler : ReportPlatformHandler {

    override fun launchImagePicker(onImagePicked: (bytes: ByteArray, bitmap: ImageBitmap) -> Unit) {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val buffer = reader.result as? ArrayBuffer
                    if (buffer != null) {
                        val int8 = Int8Array(buffer)
                        val bytes = ByteArray(int8.length) { index -> int8[index] }
                        decodeBitmap(bytes)?.let { bitmap ->
                            onImagePicked(bytes, bitmap)
                        }
                    }
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.click()
    }

    override fun fetchCurrentLocation(onLocationFetched: (addressOrCoords: String) -> Unit) {
        requestBrowserGeolocation(
            onSuccess = { lat, lng ->
                onLocationFetched(formatCoordinates(lat, lng))
            },
            onError = {
                onLocationFetched("22.5726° N, 88.3639° E • Salt Lake Sector V")
            }
        )
    }

    override fun launchMapLocationPicker(
        onLocationPicked: (addressOrCoords: String) -> Unit,
        onDismiss: () -> Unit
    ) {
        val body = document.body ?: run {
            onLocationPicked("22.5726° N, 88.3639° E • Salt Lake Sector V")
            return
        }

        var selectedLocation = "Salt Lake Sector V, Kolkata"
        var currentMapQuery = "22.5726,88.3639"

        val overlay = (document.createElement("div") as HTMLElement).apply {
            style.position = "fixed"
            style.top = "0"
            style.left = "0"
            style.width = "100vw"
            style.height = "100vh"
            style.backgroundColor = "rgba(25, 17, 2, 0.65)"
            style.zIndex = "99999"
            style.display = "flex"
            style.alignItems = "center"
            style.justifyContent = "center"
            style.padding = "16px"
            style.boxSizing = "border-box"
        }

        val modal = (document.createElement("div") as HTMLElement).apply {
            style.width = "100%"
            style.maxWidth = "600px"
            style.backgroundColor = "#F1E4F3"
            style.borderRadius = "24px"
            style.padding = "20px"
            style.boxShadow = "0 16px 40px rgba(0,0,0,0.32)"
            style.fontFamily = "system-ui, -apple-system, sans-serif"
            style.color = "#191102"
            style.maxHeight = "90vh"
            style.overflowY = "auto"
        }

        modal.innerHTML = """
            <style>
                @keyframes wm-spin { to { transform: rotate(360deg); } }
                .wm-spinner {
                    width: 28px;
                    height: 28px;
                    border: 3px solid rgba(79, 184, 40, 0.24);
                    border-top-color: #4FB828;
                    border-radius: 50%;
                    animation: wm-spin 0.75s linear infinite;
                }
            </style>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                <div>
                    <h3 style="margin:0; color:#191102; font-size:19px; font-weight:700;">Choose Location on Google Maps</h3>
                    <p style="margin:2px 0 0 0; font-size:12px; color:#555;">Search a street/landmark, use GPS, or pick a hotspot below</p>
                </div>
                <button id="wm-gmap-close" type="button" style="width:32px; height:32px; border-radius:50%; border:none; background:rgba(25,17,2,0.1); font-size:16px; cursor:pointer; color:#191102;">✕</button>
            </div>

            <div style="display:flex; gap:8px; margin-bottom:10px;">
                <input id="wm-gmap-search" type="text" placeholder="Search address, landmark, or lat,lng..."
                    style="flex:1; padding:10px 12px; border-radius:12px; border:1px solid rgba(25,17,2,0.22); font-size:13px; background:#fff; outline:none;" />
                <button id="wm-gmap-preview" type="button"
                    style="padding:10px 14px; border-radius:12px; border:none; background:#4FB828; color:#fff; font-weight:700; font-size:13px; cursor:pointer;">Search</button>
            </div>

            <div style="display:flex; gap:6px; flex-wrap:wrap; margin-bottom:12px;">
                <button id="wm-gmap-gps" type="button" style="padding:6px 11px; border-radius:999px; border:none; background:#386C0B; color:#fff; font-weight:600; font-size:12px; cursor:pointer;">Use My GPS</button>
                <button id="wm-pin-sector5" type="button" style="padding:6px 11px; border-radius:999px; border:1px solid rgba(25,17,2,0.18); background:#fff; color:#191102; font-weight:600; font-size:12px; cursor:pointer;">Sector 5 Market</button>
                <button id="wm-pin-canal" type="button" style="padding:6px 11px; border-radius:999px; border:1px solid rgba(25,17,2,0.18); background:#fff; color:#191102; font-weight:600; font-size:12px; cursor:pointer;">Canal Road</button>
                <button id="wm-pin-park" type="button" style="padding:6px 11px; border-radius:999px; border:1px solid rgba(25,17,2,0.18); background:#fff; color:#191102; font-weight:600; font-size:12px; cursor:pointer;">Greenview Park</button>
            </div>

            <div style="position:relative; width:100%; height:240px; border:1px solid rgba(25,17,2,0.12); border-radius:16px; background:#fff; overflow:hidden; margin-bottom:12px;">
                <div id="wm-gmap-loader" style="position:absolute; inset:0; display:flex; flex-direction:column; gap:8px; align-items:center; justify-content:center; background:rgba(255,255,255,0.9); z-index:2;">
                    <div class="wm-spinner"></div>
                    <span style="font-size:12px; font-weight:600; color:#386C0B;">Loading Google Maps...</span>
                </div>
                <iframe id="wm-gmap-iframe" width="100%" height="240" loading="lazy"
                    style="border:none; display:block;"
                    src="https://maps.google.com/maps?q=22.5726,88.3639&z=15&output=embed"></iframe>
            </div>

            <div id="wm-gmap-selected" style="padding:10px 12px; border-radius:12px; background:rgba(255,255,255,0.85); border:1px solid rgba(56,108,11,0.2); font-size:12px; font-weight:600; color:#386C0B; margin-bottom:14px;">
                Selected: Salt Lake Sector V, Kolkata
            </div>

            <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <button id="wm-gmap-external" type="button"
                    style="padding:11px 14px; border-radius:999px; border:1px solid rgba(56,108,11,0.35); background:#fff; color:#386C0B; font-weight:700; font-size:13px; cursor:pointer;">Open in Google Maps</button>
                <button id="wm-gmap-confirm" type="button"
                    style="flex:1; padding:11px 18px; border-radius:999px; border:none; background:#4FB828; color:#fff; font-weight:700; font-size:14px; cursor:pointer;">Confirm Location</button>
            </div>
        """.trimIndent()

        overlay.appendChild(modal)
        body.appendChild(overlay)

        val searchInput = document.getElementById("wm-gmap-search") as? HTMLInputElement
        val iframe = document.getElementById("wm-gmap-iframe") as? HTMLIFrameElement
        val loader = document.getElementById("wm-gmap-loader") as? HTMLElement
        val gpsButton = document.getElementById("wm-gmap-gps") as? HTMLButtonElement
        val selectedBanner = document.getElementById("wm-gmap-selected") as? HTMLElement

        iframe?.onload = {
            loader?.style?.display = "none"
        }

        fun closeModal(cancelled: Boolean) {
            if (overlay.parentNode == body) {
                body.removeChild(overlay)
            }
            if (cancelled) {
                onDismiss()
            }
        }

        fun updatePreview(label: String, mapQuery: String) {
            selectedLocation = label
            currentMapQuery = mapQuery
            searchInput?.value = label
            selectedBanner?.textContent = "Selected: $label"
            loader?.style?.display = "flex"
            iframe?.src = "https://maps.google.com/maps?q=${encodeUrlComponent(mapQuery)}&z=15&output=embed"
        }

        overlay.onclick = { event ->
            if (event.target == overlay) {
                closeModal(cancelled = true)
            }
        }

        (document.getElementById("wm-gmap-close") as? HTMLButtonElement)?.onclick = {
            closeModal(cancelled = true)
        }

        (document.getElementById("wm-gmap-preview") as? HTMLButtonElement)?.onclick = {
            val typed = searchInput?.value?.trim().orEmpty()
            if (typed.isNotEmpty()) {
                updatePreview(typed, typed)
            }
        }

        searchInput?.onkeydown = { event ->
            if (event.key == "Enter") {
                val typed = searchInput.value.trim()
                if (typed.isNotEmpty()) {
                    updatePreview(typed, typed)
                }
            }
        }

        gpsButton?.onclick = {
            gpsButton.textContent = "Locating..."
            gpsButton.disabled = true
            requestBrowserGeolocation(
                onSuccess = { lat, lng ->
                    gpsButton.textContent = "Use My GPS"
                    gpsButton.disabled = false
                    val coordsLabel = formatCoordinates(lat, lng)
                    updatePreview(coordsLabel, "$lat,$lng")
                },
                onError = {
                    gpsButton.textContent = "Use My GPS"
                    gpsButton.disabled = false
                    updatePreview("Salt Lake Sector V, Kolkata (22.5726, 88.3639)", "22.5726,88.3639")
                }
            )
        }

        (document.getElementById("wm-pin-sector5") as? HTMLButtonElement)?.onclick = {
            updatePreview("Sector 5, Main Market, Salt Lake", "22.5726,88.4339")
        }

        (document.getElementById("wm-pin-canal") as? HTMLButtonElement)?.onclick = {
            updatePreview("Canal Road, Block B", "22.5850,88.4100")
        }

        (document.getElementById("wm-pin-park") as? HTMLButtonElement)?.onclick = {
            updatePreview("Greenview Park North", "22.5620,88.3920")
        }

        (document.getElementById("wm-gmap-external") as? HTMLButtonElement)?.onclick = {
            val query = searchInput?.value?.trim().orEmpty().ifEmpty { currentMapQuery }
            window.open("https://www.google.com/maps/search/?api=1&query=${encodeUrlComponent(query)}", "_blank")
        }

        (document.getElementById("wm-gmap-confirm") as? HTMLButtonElement)?.onclick = {
            val finalValue = searchInput?.value?.trim().orEmpty().ifEmpty { selectedLocation }
            closeModal(cancelled = false)
            onLocationPicked(finalValue)
        }
    }

    override fun decodeBitmap(bytes: ByteArray): ImageBitmap? {
        if (bytes.isEmpty()) return null
        return runCatching {
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        }.getOrNull()
    }
}

private fun formatCoordinates(lat: Double, lng: Double): String {
    val latDir = if (lat >= 0.0) "N" else "S"
    val lngDir = if (lng >= 0.0) "E" else "W"
    val latAbs = round(abs(lat) * 10_000.0) / 10_000.0
    val lngAbs = round(abs(lng) * 10_000.0) / 10_000.0
    return "$latAbs° $latDir, $lngAbs° $lngDir"
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun encodeUrlComponent(value: String): String = js("encodeURIComponent(value)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun requestBrowserGeolocation(
    onSuccess: (Double, Double) -> Unit,
    onError: () -> Unit
): Unit = js(
    """
    {
        if (typeof navigator !== 'undefined' && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(pos) { onSuccess(pos.coords.latitude, pos.coords.longitude); },
                function() { onError(); },
                { enableHighAccuracy: true, timeout: 8000 }
            );
        } else {
            onError();
        }
    }
    """
)
