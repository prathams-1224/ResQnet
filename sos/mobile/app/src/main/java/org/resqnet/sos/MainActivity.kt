package org.resqnet.sos

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.resqnet.sos.theme.ResQNetDarkBg
import org.resqnet.sos.theme.ResQNetSosTheme
import org.resqnet.sos.ui.form.SosFormScreen
import org.resqnet.sos.ui.history.SosHistoryScreen
import org.resqnet.sos.ui.home.HomeScreen
import org.resqnet.sos.ui.navigation.NavRoutes

class MainActivity : ComponentActivity() {

    // Permission launcher for Location
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            Toast.makeText(this, "Location permission granted for SOS geo-tagging.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Location denied. SOS can still be dispatched, but without GPS coordinates.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Permission launcher for Bluetooth (Android 12+)
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val connectGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else true

        if (!connectGranted) {
            Toast.makeText(
                this,
                "Bluetooth permission denied. Mesh peer discovery may be limited.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request initial Bluetooth permissions on Android 12+ if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }

        setContent {
            ResQNetSosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ResQNetDarkBg
                ) {
                    ResQNetNavHost(
                        onRequestLocationPermission = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResQNetNavHost(
    onRequestLocationPermission: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        composable(NavRoutes.Home.route) {
            HomeScreen(
                onNavigateToForm = {
                    navController.navigate(NavRoutes.SosForm.route)
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.SosHistory.route)
                },
                onRequestLocationPermission = onRequestLocationPermission
            )
        }

        composable(NavRoutes.SosForm.route) {
            SosFormScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSosSubmitted = {
                    navController.navigate(NavRoutes.SosHistory.route) {
                        popUpTo(NavRoutes.Home.route)
                    }
                },
                onRequestLocationPermission = onRequestLocationPermission
            )
        }

        composable(NavRoutes.SosHistory.route) {
            SosHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
