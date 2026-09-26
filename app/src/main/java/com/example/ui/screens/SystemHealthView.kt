package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HealthCheckGrade
import com.example.data.HealthCheckItem
import com.example.data.MemoryHealthStatus
import com.example.data.NetworkHealthStatus
import com.example.data.SystemHealthReport
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberStatusWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanPrimaryContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldStatusOk
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseStatusCritical
import com.example.viewmodel.HelpDeskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SystemHealthView(
    viewModel: HelpDeskViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.systemHealthReport.collectAsStateWithLifecycle()
    val isScanning by viewModel.isHealthScanning.collectAsStateWithLifecycle()

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val lastScanTime = remember(report?.scannedAtTimestamp) {
        report?.scannedAtTimestamp?.let { dateFormat.format(Date(it)) } ?: "Just now"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Diagnostic Header and Scan Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System Health Diagnostics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Live memory usage monitoring & network connectivity status",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.refreshSystemHealth() },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("scan_system_health_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auditing...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Overall Health Score Hero Banner
        item {
            val score = report?.overallScorePercent ?: 100
            val scoreColor = when {
                score >= 85 -> EmeraldStatusOk
                score >= 65 -> AmberStatusWarning
                else -> RoseStatusCritical
            }
            val statusLabel = when {
                score >= 85 -> "All Systems Operational & Nominal"
                score >= 65 -> "System Warning - Elevated Resource Load"
                else -> "System Health Alert - Critical Constraints"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overall_health_score_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(scoreColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (score >= 85) Icons.Default.HealthAndSafety else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = scoreColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SYSTEM HEALTH SCORE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanLight,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = statusLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "$score%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = scoreColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = scoreColor,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last diagnostic telemetry synchronized at $lastScanTime",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Section 1: Memory Usage Monitoring Card
        report?.memory?.let { memory ->
            item {
                MemoryMonitoringSection(memory = memory)
            }
        }

        // Section 2: Network Connectivity Status Card
        report?.network?.let { network ->
            item {
                NetworkConnectivitySection(network = network)
            }
        }

        // Section 3: Diagnostic Check Matrix Table
        report?.checks?.let { checkList ->
            item {
                Text(
                    text = "Automated Health Audit Matrix",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(checkList) { check ->
                HealthAuditItemCard(item = check)
            }
        }
    }
}

@Composable
fun MemoryMonitoringSection(memory: MemoryHealthStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_monitoring_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = CyanLight, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Memory Usage Monitoring",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time physical RAM, JVM heap & storage",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (memory.isLowMemory) RoseContainer else EmeraldContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (memory.isLowMemory) "LOW MEMORY" else "RAM HEALTHY",
                        color = if (memory.isLowMemory) RoseStatusCritical else EmeraldStatusOk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Physical RAM Gauge Bar
            val ramColor = when {
                memory.usedRamPercent >= 85 -> RoseStatusCritical
                memory.usedRamPercent >= 70 -> AmberStatusWarning
                else -> EmeraldStatusOk
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Physical RAM Load",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${memory.usedRamPercent}% (${memory.usedRamMb} MB / ${memory.totalRamMb} MB)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ramColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (memory.usedRamPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ramColor,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // RAM Stats Details Table
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HealthMetricRow("Available Free RAM", "${memory.availableRamMb} MB", EmeraldStatusOk)
                    HealthMetricRow("Low Memory Threshold", "${memory.lowMemThresholdMb} MB", Color(0xFFCBD5E1))
                    HealthMetricRow("Allocated JVM Heap", "${memory.jvmTotalHeapMb} MB (Max: ${memory.jvmMaxHeapMb} MB)", CyanLight)
                    HealthMetricRow("Free JVM Heap Headroom", "${memory.jvmFreeHeapMb} MB", EmeraldStatusOk)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Internal Storage Usage Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Internal Flash Storage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${memory.usedStorageGb} GB / ${memory.totalStorageGb} GB (${memory.usedStoragePercent}%)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (memory.usedStoragePercent > 85) AmberStatusWarning else CyanLight
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (memory.usedStoragePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CyanLight,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${memory.availableStorageGb} GB free and accessible for applications & cache logs",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NetworkConnectivitySection(network: NetworkHealthStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("network_connectivity_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (network.isConnected) Color(0xFF064E3B) else RoseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (network.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (network.isConnected) EmeraldStatusOk else RoseStatusCritical,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Network Connectivity Status",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = network.transportType,
                            fontSize = 11.sp,
                            color = CyanLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    color = if (network.isConnected) EmeraldContainer else RoseContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (network.isConnected) EmeraldStatusOk else RoseStatusCritical)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (network.isConnected) "ONLINE" else "OFFLINE",
                            color = if (network.isConnected) EmeraldStatusOk else RoseStatusCritical,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Network telemetry breakdown
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HealthMetricRow("Active Interface IPv4", network.localIp, CyanLight)
                    HealthMetricRow(
                        "DNS Resolver Status",
                        if (network.dnsResolvingOk) "Operational (${network.dnsLatencyMs}ms)" else "Resolution Failed",
                        if (network.dnsResolvingOk) EmeraldStatusOk else RoseStatusCritical
                    )
                    HealthMetricRow("Gateway & Upstream DNS", network.gatewayDns, Color(0xFFCBD5E1))
                    HealthMetricRow(
                        "Metered Network Policy",
                        if (network.isMetered) "Metered (Bandwidth constrained)" else "Unmetered (High-Speed LAN)",
                        Color(0xFFCBD5E1)
                    )
                    if (network.linkDownstreamBandwidthKbps > 0) {
                        HealthMetricRow(
                            "Link Downstream Bandwidth",
                            "${network.linkDownstreamBandwidthKbps / 1000} Mbps",
                            CyanLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (network.isConnected) EmeraldContainer.copy(alpha = 0.3f) else RoseContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (network.hasInternetCapability) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (network.hasInternetCapability) EmeraldStatusOk else RoseStatusCritical,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = network.statusSummary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (network.hasInternetCapability) EmeraldStatusOk else RoseStatusCritical
                    )
                }
            }
        }
    }
}

@Composable
fun HealthAuditItemCard(item: HealthCheckItem) {
    val (badgeBg, badgeText) = when (item.grade) {
        HealthCheckGrade.OPTIMAL -> Pair(EmeraldContainer, EmeraldStatusOk)
        HealthCheckGrade.WARNING -> Pair(AmberContainer, AmberStatusWarning)
        HealthCheckGrade.CRITICAL -> Pair(RoseContainer, RoseStatusCritical)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.metricValue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanLight
                )
                Text(
                    text = item.details,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = item.grade.label.uppercase(),
                    color = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun HealthMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
