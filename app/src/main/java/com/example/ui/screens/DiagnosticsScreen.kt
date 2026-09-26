package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CopyableCodeBlock
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanPrimaryContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldStatusOk
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseStatusCritical
import com.example.viewmodel.HelpDeskViewModel

@Composable
fun DiagnosticsScreen(
    viewModel: HelpDeskViewModel,
    modifier: Modifier = Modifier
) {
    var selectedToolIndex by remember { mutableIntStateOf(0) }
    val toolTabs = listOf("System Health", "Ping Prober", "Port Scanner", "Subnet Calc", "Password & Hash", "Telemetry")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Engineering Sub-tool Tabs with smooth scrolling
        ScrollableTabRow(
            selectedTabIndex = selectedToolIndex,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CyanLight,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedToolIndex]),
                    color = CyanLight,
                    height = 3.dp
                )
            }
        ) {
            toolTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolIndex == index,
                    onClick = { selectedToolIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedToolIndex == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Active Tool Content
        when (selectedToolIndex) {
            0 -> SystemHealthView(viewModel)
            1 -> PingToolView(viewModel)
            2 -> PortScannerView(viewModel)
            3 -> SubnetCalculatorView(viewModel)
            4 -> PasswordAndHashView(viewModel)
            5 -> TelemetryView(viewModel)
        }
    }
}

@Composable
fun PingToolView(viewModel: HelpDeskViewModel) {
    val host by viewModel.pingHost.collectAsStateWithLifecycle()
    val pingResult by viewModel.pingResult.collectAsStateWithLifecycle()
    val isPinging by viewModel.isPinging.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "TCP / ICMP Socket Latency Prober",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Verify route connectivity and measure round-trip packet latency to gateway or internet hosts.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { viewModel.setPingHost(it) },
                    label = { Text("Target Hostname or IP") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ping_host_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.executePing() },
                    enabled = !isPinging && host.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("execute_ping_button")
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Probe")
                    }
                }
            }
        }

        item {
            Text("Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val presets = listOf("8.8.8.8", "1.1.1.1", "google.com", "10.0.0.1")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(presets) { p ->
                    FilterChip(
                        selected = host == p,
                        onClick = {
                            viewModel.setPingHost(p)
                            viewModel.executePing(p)
                        },
                        label = { Text(p, fontSize = 11.sp) }
                    )
                }
            }
        }

        pingResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isReachable) EmeraldContainer.copy(alpha = 0.4f) else RoseContainer.copy(alpha = 0.4f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (res.isReachable) EmeraldStatusOk else RoseStatusCritical
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.isReachable) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (res.isReachable) EmeraldStatusOk else RoseStatusCritical,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (res.isReachable) "HOST REACHABLE" else "CONNECTION TIMEOUT",
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isReachable) EmeraldStatusOk else RoseStatusCritical,
                                    fontSize = 14.sp
                                )
                            }
                            if (res.isReachable) {
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${res.latencyMs} ms",
                                        color = if (res.latencyMs < 60) EmeraldStatusOk else Color(0xFFFBBF24),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Resolved Address: ${res.ip}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Diagnostic Detail: ${res.details}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PortScannerView(viewModel: HelpDeskViewModel) {
    val host by viewModel.portScanHost.collectAsStateWithLifecycle()
    val isScanning by viewModel.isPortScanning.collectAsStateWithLifecycle()
    val results by viewModel.portScanResults.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "TCP Port Service Reachability Scanner",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Audits standard enterprise service ports (HTTP, HTTPS, SSH, DNS, RDP, SMB) with socket timeouts.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { viewModel.setPortScanHost(it) },
                    label = { Text("Target Host / IP") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("port_scan_host_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.executePortScan() },
                    enabled = !isScanning && host.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("start_port_scan_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Scan")
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            items(results) { item ->
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Port ${item.port}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.serviceName,
                                    fontSize = 12.sp,
                                    color = CyanLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = item.message,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (item.isOpen) EmeraldContainer else Color(0xFF334155))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (item.isOpen) "OPEN" else "CLOSED",
                                color = if (item.isOpen) EmeraldStatusOk else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubnetCalculatorView(viewModel: HelpDeskViewModel) {
    val ip by viewModel.subnetIp.collectAsStateWithLifecycle()
    val prefix by viewModel.subnetPrefix.collectAsStateWithLifecycle()
    val result by viewModel.subnetResult.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "IPv4 CIDR Subnet Calculator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Calculate network boundaries, netmasks, broadcast IP, and usable host count instantly.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { viewModel.setSubnetIp(it) },
                    label = { Text("IPv4 Address") },
                    modifier = Modifier
                        .weight(2f)
                        .testTag("subnet_ip_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { viewModel.setSubnetPrefix(it) },
                    label = { Text("CIDR (e.g. 24)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("subnet_cidr_input"),
                    singleLine = true
                )
            }
        }

        item {
            val prefixes = listOf("8", "16", "24", "26", "28", "30", "32")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(prefixes) { p ->
                    FilterChip(
                        selected = prefix == p,
                        onClick = { viewModel.setSubnetPrefix(p) },
                        label = { Text("/$p") }
                    )
                }
            }
        }

        result?.let { calc ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("NETWORK PARAMETERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanLight, letterSpacing = 1.sp)

                        SubnetParamRow("Network Address", calc.networkAddress)
                        SubnetParamRow("Subnet Mask", calc.subnetMask)
                        SubnetParamRow("Wildcard Mask", calc.wildcardMask)
                        SubnetParamRow("Broadcast Address", calc.broadcastAddress)
                        SubnetParamRow("First Usable Host", calc.firstUsableHost)
                        SubnetParamRow("Last Usable Host", calc.lastUsableHost)
                        SubnetParamRow("Total Usable Hosts", "${calc.usableHosts} devices")
                    }
                }
            }
        }
    }
}

@Composable
fun SubnetParamRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PasswordAndHashView(viewModel: HelpDeskViewModel) {
    val length by viewModel.passwordLength.collectAsStateWithLifecycle()
    val includeSymbols by viewModel.includeSymbols.collectAsStateWithLifecycle()
    val includeNumbers by viewModel.includeNumbers.collectAsStateWithLifecycle()
    val password by viewModel.generatedPassword.collectAsStateWithLifecycle()

    val hashInput by viewModel.hashInputText.collectAsStateWithLifecycle()
    val hashes by viewModel.computedHashes.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Enterprise Password & Hash Generator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Generate high-entropy credentials and verify SHA-256 / MD5 software checksums.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Generated Password Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GENERATE SECURE PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanLight, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = password,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanGlow,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Password", password))
                                    Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = CyanLight)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Password Length: $length characters", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = length.toFloat(),
                        onValueChange = { viewModel.setPasswordLength(it.toInt()) },
                        valueRange = 8f..32f,
                        steps = 23
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Symbols (!@#$)", fontSize = 12.sp)
                        Switch(checked = includeSymbols, onCheckedChange = { viewModel.toggleIncludeSymbols() })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include Numbers (0-9)", fontSize = 12.sp)
                        Switch(checked = includeNumbers, onCheckedChange = { viewModel.toggleIncludeNumbers() })
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.generateNewPassword() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Regenerate Credentials")
                    }
                }
            }
        }

        // Cryptographic Hash Tool
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CHECKSUM & HASH VERIFIER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanLight, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hashInput,
                        onValueChange = { viewModel.setHashInput(it) },
                        label = { Text("Input String / Payload") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("SHA-256 Hash:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CopyableCodeBlock(command = hashes.first)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("MD5 Hash:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CopyableCodeBlock(command = hashes.second)
                }
            }
        }
    }
}

@Composable
fun TelemetryView(viewModel: HelpDeskViewModel) {
    val telemetry by viewModel.deviceTelemetry.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device & Network Telemetry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real system diagnostics, interface states, and hardware metrics.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.refreshTelemetry() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanLight)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    telemetry.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(key, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                value,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
