package com.genesys.tauhackathon

import java.sql.Timestamp
import java.time.LocalDateTime

data class Place(
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: LocalDateTime
)
