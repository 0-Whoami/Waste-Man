package com.example.wasteman

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
