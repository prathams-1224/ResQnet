package org.resqnet.sos.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.resqnet.sos.ResQNetSosApp
import org.resqnet.sos.telemetry.HardwareStatus
import org.resqnet.sos.telemetry.NetworkStatus
import org.resqnet.sos.theme.*
import org.resqnet.sos.ui.components.StatusIndicatorCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToForm: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ResQNetSosApp
    val repository = app.repository
    val deviceStatusManager = app.deviceStatusManager

    // Reactive pending count from Room DB Flow
    val pendingCount by repository.getPendingCountFlow().collectAsState(initial = 0)

    // Periodic telemetry refresher
    var telemetry by remember { mutableStateOf(deviceStatusManager.getSnapshot()) }

    LaunchedEffect(Unit) {
        while (true) {
            telemetry = deviceStatusManager.getSnapshot()
            delay(3000L) // Refresh every 3 seconds
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.networkStatus == NetworkStatus.ONLINE) ResQNetGreen else ResQNetOrange)
                        )
                        Column {
                            Text(
                                text = "ResQNet SOS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ResQNetTextPrimary
                            )
                            Text(
                                text = "Kerala Disaster Response Network",
                                fontSize = 11.sp,
                                color = ResQNetTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge(
                                        containerColor = ResQNetOrange,
                                        contentColor = ResQNetTextPrimary
                                    ) {
                                        Text(pendingCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Distress History",
                                tint = ResQNetTextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ResQNetDarkBg
                )
            )
        },
        containerColor = ResQNetDarkBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Network Mode Explanation Banner
            val isOnline = telemetry.networkStatus == NetworkStatus.ONLINE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) Color(0x1F22C55E) else Color(0x1FF97316)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isOnline) ResQNetGreen else ResQNetOrange
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) ResQNetGreen else ResQNetOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = if (isOnline) "ONLINE MODE" else "OFFLINE STORE-AND-FORWARD MODE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isOnline) ResQNetGreen else ResQNetOrange
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOnline) {
                                "Direct internet connectivity is available. SOS beacons will be sent directly to ResQNet servers and saved in your local history."
                            } else {
                                "Cellular/Wi-Fi is disconnected. SOS beacons will be stored locally in your phone's database and queued for peer-to-peer Bluetooth mesh relay."
                            },
                            fontSize = 12.sp,
                            color = ResQNetTextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Large, Prominent Emergency Panic Button
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(ResQNetRedGlow)
                    .border(2.dp, ResQNetRed.copy(alpha = 0.5f), CircleShape)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(ResQNetRed)
                        .clickable { onNavigateToForm() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Emergency Panic Button",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "EMERGENCY",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "TAP TO REPORT",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Hardware & Telemetry Status Cards
            Text(
                text = "COMMUNICATION & LOCATION STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ResQNetTextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            // Bluetooth Status
            val btStatus = telemetry.bluetoothStatus
            val (btText, btColor) = when (btStatus) {
                HardwareStatus.ENABLED -> "Bluetooth Active (Ready for Mesh)" to ResQNetGreen
                HardwareStatus.DISABLED -> "Bluetooth Disabled (Tap to enable)" to ResQNetOrange
                HardwareStatus.PERMISSION_REQUIRED -> "Bluetooth Permission Required" to ResQNetYellow
                HardwareStatus.UNSUPPORTED -> "Bluetooth Not Supported on this Device" to ResQNetTextMuted
            }
            StatusIndicatorCard(
                title = "Bluetooth Radio",
                statusText = btText,
                icon = Icons.Default.Bluetooth,
                statusColor = btColor,
                actionLabel = if (btStatus == HardwareStatus.DISABLED) "Turn On" else null,
                onActionClick = if (btStatus == HardwareStatus.DISABLED) {
                    { context.startActivity(deviceStatusManager.createOpenBluetoothSettingsIntent()) }
                } else null
            )

            // GPS Location Status
            val locStatus = telemetry.locationStatus
            val (locText, locColor) = when (locStatus) {
                HardwareStatus.ENABLED -> "GPS Hardware Ready & Active" to ResQNetGreen
                HardwareStatus.DISABLED -> "Location Service Turned Off" to ResQNetOrange
                HardwareStatus.PERMISSION_REQUIRED -> "Permission Needed for Accurate Fix" to ResQNetYellow
                HardwareStatus.UNSUPPORTED -> "Location Hardware Unavailable" to ResQNetTextMuted
            }
            StatusIndicatorCard(
                title = "GPS Satellite Location",
                statusText = locText,
                icon = Icons.Default.LocationOn,
                statusColor = locColor,
                actionLabel = when (locStatus) {
                    HardwareStatus.PERMISSION_REQUIRED -> "Grant"
                    HardwareStatus.DISABLED -> "Enable GPS"
                    else -> null
                },
                onActionClick = when (locStatus) {
                    HardwareStatus.PERMISSION_REQUIRED -> onRequestLocationPermission
                    HardwareStatus.DISABLED -> {
                        { context.startActivity(deviceStatusManager.createOpenLocationSettingsIntent()) }
                    }
                    else -> null
                }
            )

            // Internet Status
            StatusIndicatorCard(
                title = "Cellular / Wi-Fi Gateway",
                statusText = if (isOnline) "Connected to Central Gateway" else "Disconnected (Offline Mode)",
                icon = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                statusColor = if (isOnline) ResQNetGreen else ResQNetOrange
            )

            // Pending Queue Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToHistory() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ResQNetSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ResQNetBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Outbox,
                            contentDescription = null,
                            tint = if (pendingCount > 0) ResQNetOrange else ResQNetTextSecondary
                        )
                        Column {
                            Text(
                                text = "Pending Outbound SOS Queue",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ResQNetTextPrimary
                            )
                            Text(
                                text = if (pendingCount == 0) {
                                    "No unsent distress signals."
                                } else {
                                    "$pendingCount beacon(s) saved locally awaiting peer relay."
                                },
                                fontSize = 11.sp,
                                color = ResQNetTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to History",
                        tint = ResQNetTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
