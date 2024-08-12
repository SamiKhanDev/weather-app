package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherappTheme
import com.example.weatherapp.weatherscreen.SplashScreen
import com.example.weatherapp.weatherscreen.WeatherViewModel
import com.example.weatherapp.weatherscreen.weatherScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLastKnownLocationAndCurrentLocation()
            } else {
                setContent {
                    WeatherAppContent("New York")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastKnownLocationAndCurrentLocation()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLastKnownLocationAndCurrentLocation() {
        getLastKnownLocation { lastKnownLocation ->
            getCurrentLocation(
                onGetCurrentLocationSuccess = { currentLocation ->
                    val locationString = if (currentLocation != null) {
                        "${currentLocation.first},${currentLocation.second}"
                    } else {
                        lastKnownLocation?.let { "${it.latitude},${it.longitude}" } ?: "New York"
                    }
                    setContent {
                        WeatherAppContent(locationString)
                    }
                },
                onGetCurrentLocationFailed = {
                    val locationString = lastKnownLocation?.let { "${it.latitude},${it.longitude}" } ?: "New York"
                    setContent {
                        WeatherAppContent(locationString)
                    }
                }
            )
        }
    }

    private fun getLastKnownLocation(onSuccess: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onSuccess(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onSuccess(location)
        }.addOnFailureListener {
            onSuccess(null)
        }
    }

    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>?) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        if (areLocationPermissionsGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                location?.let {
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                } ?: run {
                    onGetCurrentLocationSuccess(null)
                }
            }.addOnFailureListener { exception ->
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun WeatherAppContent(location: String) {
    val weatherViewModel: WeatherViewModel = viewModel()
    var showSplashScreen by remember { mutableStateOf(true) }

    WeatherappTheme {
        if (showSplashScreen) {
            SplashScreen(onSplashEnd = {
                showSplashScreen = false
            })
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                weatherScreen(weatherViewModel, location)
            }
        }
    }
}
