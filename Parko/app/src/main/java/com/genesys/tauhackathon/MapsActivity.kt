package com.genesys.tauhackathon

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.time.Duration

class MapsActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    val places = FakeAPI().fetchPlaces()
    //region - Attributes
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var mapView: MapView
    //endregion

    //region - Listeners
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }
    //endregion


    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateDurationInSeconds(start: LocalDateTime, end: LocalDateTime): Long {
        val duration = Duration.between(start, end)
        return duration.seconds
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)
        handleLocationPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleLocationPermissions() {
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            configureMap()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePins(){
        val annotationApi = mapView.annotations
        while(true){
            addAnnotationToMap()
            Thread.sleep(2000)
            annotationApi.cleanup()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configureMap() {
        val map = mapView.getMapboxMap()
        map.setCamera(
            CameraOptions.Builder()
                .zoom(17.0)
                .build()
        )

        map.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
            val myThread = Thread(Runnable {
                updatePins()
            })
            myThread.start()
        }

    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addAnnotationToMap() {
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        places.forEach { place ->
            Utilities.bitmapFromDrawableRes(
                this@MapsActivity, R.drawable.annotation
            )?.let {
                val diff = calculateDurationInSeconds(place.timestamp,LocalDateTime.now())
                val prob = 100-diff*0.5
                val text = place.title + " " + diff.toString() + "s ago" +"\n" + prob.toString() +"%"
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(place.longitude, place.latitude))
                    .withIconImage(it)
                    .withIconOffset(listOf(0.0, -20.0))
                    .withTextField(text)
                    .withTextSize(15.0)
                    .withTextColor(Color.RED)
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }

    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }
}