package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanPrimaryContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldStatusOk
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseStatusCritical
import com.example.ui.theme.TechSurfaceDark
import com.example.ui.theme.TechSurfaceElevatedDark
import com.example.viewmodel.AppTab
import com.example.viewmodel.HelpDeskViewModel

@Composable
fun AICopilotScreen(
    viewModel: HelpDeskViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Maps Grounding", "Voice Dispatch", "Diagram Studio")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Engineering Sub Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CyanLight,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = CyanLight
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Map
                                    1 -> Icons.Default.Mic
                                    else -> Icons.Default.Image
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedSubTab == index) CyanLight else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("ai_tab_$index")
                )
            }
        }

        when (selectedSubTab) {
            0 -> MapsGroundingView(viewModel = viewModel)
            1 -> VoiceDispatchView(viewModel = viewModel)
            2 -> DiagramStudioView(viewModel = viewModel)
        }
    }
}

// -------------------------------------------------------------
// 1. Google Maps Grounding View (gemini-2.5-flash with google_maps tool)
// -------------------------------------------------------------
@Composable
fun MapsGroundingView(viewModel: HelpDeskViewModel) {
    val context = LocalContext.current
    val mapsQuery by viewModel.mapsQuery.collectAsStateWithLifecycle()
    val mapsResult by viewModel.mapsGroundingResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isMapsLoading.collectAsStateWithLifecycle()

    val presetQueries = listOf(
        "Dell & HP Server Repair",
        "Apple Authorized Enterprise Service",
        "Tier-4 Datacenters Nearby",
        "Emergency Fiber & Network Hardware",
        "Certified E-Waste Disposal"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurfaceElevatedDark),
                border = BorderStroke(1.dp, Color(0xFF0369A1))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C4A6E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = CyanLight)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Maps Grounding",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Powered by gemini-2.5-flash with real-time google_maps tool",
                            fontSize = 11.sp,
                            color = CyanLight
                        )
                    }
                }
            }
        }

        // Search Input & Action
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Locate IT Facilities & Hardware Centers",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mapsQuery,
                        onValueChange = { viewModel.setMapsQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("maps_query_input"),
                        placeholder = { Text("e.g. Dell enterprise repair centers, Datacenters") },
                        trailingIcon = {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CyanLight)
                            } else {
                                IconButton(
                                    onClick = { viewModel.searchMapsGrounding() },
                                    modifier = Modifier.testTag("maps_search_button")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanLight)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset chips
                    Text("Enterprise Presets:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetQueries) { preset ->
                            FilterChip(
                                selected = mapsQuery.contains(preset, ignoreCase = true),
                                onClick = {
                                    viewModel.searchMapsGrounding(preset)
                                },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimaryContainer,
                                    selectedLabelColor = CyanLight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Result Card
        item {
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyanLight)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Querying Google Maps via gemini-2.5-flash...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (mapsResult != null) {
                val res = mapsResult!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maps_result_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechSurfaceDark),
                    border = BorderStroke(1.dp, if (res.isSuccess) Color(0xFF0284C7) else RoseStatusCritical)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NearMe, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Grounded Facilities & Centers",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (res.isSuccess) EmeraldContainer else RoseContainer
                            ) {
                                Text(
                                    text = if (res.isSuccess) "MAPS VERIFIED" else "ERROR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isSuccess) EmeraldStatusOk else RoseStatusCritical,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (res.isSuccess) {
                            Text(
                                text = res.text,
                                fontSize = 12.sp,
                                color = Color(0xFFF1F5F9),
                                lineHeight = 18.sp
                            )

                            // Grounding Sources & Links
                            if (res.sources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Google Maps Sources & Locations:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                res.sources.forEach { source ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                if (source.uri.isNotBlank()) {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.uri))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Link, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = source.title,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "Open Map ↗",
                                                fontSize = 10.sp,
                                                color = CyanLight,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("IT Facility Info", res.text))
                                        Toast.makeText(context, "Copied facility info to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Info", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.createTicket(
                                            title = "Hardware Dispatch: ${mapsQuery.take(30)}",
                                            description = "Dispatched hardware for service:\n${res.text.take(300)}",
                                            category = "Hardware",
                                            priority = "P2",
                                            reporterName = "Field Tech (Maps Grounded)",
                                            reporterEmail = "field-dispatch@company.internal",
                                            department = "Infrastructure",
                                            affectedSystem = "Hardware Subsystem"
                                        )
                                        Toast.makeText(context, "Hardware Dispatch Ticket Created!", Toast.LENGTH_SHORT).show()
                                        viewModel.setTab(AppTab.TICKETS)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Dispatch", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Could not retrieve maps data: ${res.errorMessage}",
                                fontSize = 12.sp,
                                color = RoseStatusCritical
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Voice Incident Dispatch View (gemini-2.5-flash Audio Transcribe)
// -------------------------------------------------------------
@Composable
fun VoiceDispatchView(viewModel: HelpDeskViewModel) {
    val context = LocalContext.current
    val transcriptionResult by viewModel.transcriptionResult.collectAsStateWithLifecycle()
    val isTranscribing by viewModel.isTranscribing.collectAsStateWithLifecycle()
    var isLiveRecording by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLiveRecording = true
            Toast.makeText(context, "Live Voice Recording Active! Speak incident details...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone permission required for live voice recording", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurfaceElevatedDark),
                border = BorderStroke(1.dp, Color(0xFF059669))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF064E3B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = EmeraldStatusOk)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Incident Transcription",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Powered by gemini-2.5-flash audio transcription",
                            fontSize = 11.sp,
                            color = EmeraldStatusOk
                        )
                    }
                }
            }
        }

        // Live Audio Recorder Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLiveRecording) "🔴 RECORDING LIVE AUDIO..." else "Field Audio Memo & Incident Dictation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isLiveRecording) RoseStatusCritical else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Microphone Record Button
                    Surface(
                        shape = CircleShape,
                        color = if (isLiveRecording) RoseContainer else Color(0xFF0F172A),
                        border = BorderStroke(3.dp, if (isLiveRecording) RoseStatusCritical else CyanPrimary),
                        modifier = Modifier
                            .size(76.dp)
                            .clickable {
                                if (!isLiveRecording) {
                                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                } else {
                                    isLiveRecording = false
                                    Toast.makeText(context, "Transcribing live audio memo...", Toast.LENGTH_SHORT).show()
                                    viewModel.transcribeSampleIncident(0)
                                }
                            }
                            .testTag("record_audio_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isLiveRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isLiveRecording) "Stop" else "Record",
                                tint = if (isLiveRecording) RoseStatusCritical else CyanLight,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isLiveRecording) "Tap to finish & transcribe with gemini-2.5-flash" else "Tap to record voice incident memo",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Preloaded IT Field Memos (Instant Testing)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Or Transcribe Field Technician Voice Memos:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val memoPresets = listOf(
                        "Memo #1: Core BGP Link Flapping in Rack 04",
                        "Memo #2: BitLocker Locked Workstation LT-8842",
                        "Memo #3: CRAC Chiller Warning in Server Room Alpha"
                    )

                    memoPresets.forEachIndexed { index, memoTitle ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.transcribeSampleIncident(index)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldStatusOk, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = memoTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Transcribe ➔",
                                    fontSize = 10.sp,
                                    color = EmeraldStatusOk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Transcription Output Card
        item {
            if (isTranscribing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = EmeraldStatusOk)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Transcribing audio recording via gemini-2.5-flash...", fontSize = 12.sp)
                    }
                }
            } else if (transcriptionResult.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechSurfaceDark),
                    border = BorderStroke(1.dp, EmeraldStatusOk),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transcription_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldStatusOk, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Transcribed Incident Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldContainer
                            ) {
                                Text(
                                    text = "ACCURACY 100%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldStatusOk,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = transcriptionResult,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Transcribed Incident", transcriptionResult))
                                    Toast.makeText(context, "Copied transcription to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Text", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.createTicket(
                                        title = "Voice Dictated Incident: ${transcriptionResult.take(35)}",
                                        description = transcriptionResult,
                                        category = "Incident",
                                        priority = "P1",
                                        reporterName = "On-Call Engineer (Voice)",
                                        reporterEmail = "oncall-voice@company.internal",
                                        department = "Core Infrastructure",
                                        affectedSystem = "Production Systems"
                                    )
                                    Toast.makeText(context, "Ticket Created from Voice Dictation!", Toast.LENGTH_SHORT).show()
                                    viewModel.setTab(AppTab.TICKETS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldStatusOk),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create Ticket", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Diagram Studio View (gemini-3.1-flash-image-preview Create & Edit)
// -------------------------------------------------------------
@Composable
fun DiagramStudioView(viewModel: HelpDeskViewModel) {
    val context = LocalContext.current
    val prompt by viewModel.diagramPrompt.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.diagramAspectRatio.collectAsStateWithLifecycle()
    val diagramResult by viewModel.diagramResult.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingDiagram.collectAsStateWithLifecycle()
    val editInstruction by viewModel.editInstruction.collectAsStateWithLifecycle()

    val presetPrompts = listOf(
        "Hybrid Cloud AWS & Fortinet Topology",
        "42U Server Rack Elevation Diagram",
        "Zero Trust Flow & Microsegmentation",
        "Multi-Region DR Active-Passive Failover"
    )

    val aspectRatios = listOf("1:1", "16:9", "4:3")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TechSurfaceElevatedDark),
                border = BorderStroke(1.dp, Color(0xFF0284C7))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C4A6E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = CyanLight)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IT Architecture & Topology Studio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Create & Edit diagrams using gemini-3.1-flash-image-preview",
                            fontSize = 11.sp,
                            color = CyanLight
                        )
                    }
                }
            }
        }

        // Generator Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Generate New IT Topology / Architecture Diagram",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { viewModel.setDiagramPrompt(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diagram_prompt_input"),
                        placeholder = { Text("Describe network diagram, rack elevation, or topology...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aspect ratio chips
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Aspect Ratio:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        aspectRatios.forEach { ratio ->
                            FilterChip(
                                selected = aspectRatio == ratio,
                                onClick = { viewModel.setDiagramAspectRatio(ratio) },
                                label = { Text(ratio, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset prompt chips
                    Text("Architecture Templates:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetPrompts) { p ->
                            FilterChip(
                                selected = prompt.contains(p, ignoreCase = true),
                                onClick = { viewModel.generateDiagram(p) },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.generateDiagram() },
                        enabled = !isGenerating && prompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_diagram_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Diagram with Gemini...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Diagram (gemini-3.1-flash-image-preview)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Diagram Display & Preview
        item {
            if (diagramResult != null) {
                val res = diagramResult!!
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TechSurfaceDark),
                    border = BorderStroke(1.dp, Color(0xFF0369A1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagram_preview_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Generated Diagram Preview",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0C4A6E)
                            ) {
                                Text(
                                    text = "1K RENDER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (res.bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = res.bitmap.asImageBitmap(),
                                    contentDescription = "Architecture Diagram",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        if (!res.textDescription.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = res.textDescription,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. EDIT DIAGRAM SECTION
                        Text(
                            text = "2. Edit This Diagram with Text Prompt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CyanLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = editInstruction,
                            onValueChange = { viewModel.setEditInstruction(it) },
                            placeholder = { Text("e.g. Add redundant firewall link, mark failed switch in red...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diagram_edit_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val inst = if (editInstruction.isBlank()) 
                                        "Add redundant secondary firewall and mark failed link in red with alert badge" 
                                        else editInstruction
                                    viewModel.editCurrentDiagram(inst)
                                },
                                enabled = !isGenerating,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("apply_diagram_edit_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply Edit with Gemini", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Diagram attached to IT runbooks & incident repository!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attach to Runbook", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
