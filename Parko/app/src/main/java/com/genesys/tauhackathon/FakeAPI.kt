package com.genesys.tauhackathon

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

class FakeAPI {
    private val park = "Parking"
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchPlaces(): ArrayList<Place> {
        val coordinates = mutableListOf<Pair<Double, Double>>()
        coordinates.add(Pair(32.088531, 34.790636))
        coordinates.add(Pair(32.088267, 34.791247))
        coordinates.add(Pair(32.087852, 34.791291))
        val places = ArrayList<Place>()
        for (i in coordinates.indices) {
            val place = Place(
                park,
                coordinates[i].first,
                coordinates[i].second,
                LocalDateTime.now()
            )
            places.add(place)
        }
        return places
    }
}