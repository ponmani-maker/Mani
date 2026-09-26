package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TicketEntity
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldStatusOk
import com.example.ui.theme.RoseStatusCritical
import com.example.viewmodel.HelpDeskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    viewModel: HelpDeskViewModel,
    modifier: Modifier = Modifier
) {
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val searchQuery by viewModel.ticketSearchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.ticketStatusFilter.collectAsStateWithLifecycle()
    val selectedTicket by viewModel.selectedTicket.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_ticket_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Ticket")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTicketSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("ticket_search_input"),
                placeholder = { Text("Search by ticket #, title, reporter, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanLight) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setTicketSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanLight,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Status Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Open", "In Progress", "Resolved", "Closed")
                items(filters) { filter ->
                    val isSelected = selectedFilter.equals(filter, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setTicketStatusFilter(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            // Ticket List
            if (tickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No tickets match your filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your search query or tap + to log a new ticket.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tickets, key = { it.id }) { ticket ->
                        TicketItemCard(
                            ticket = ticket,
                            onClick = { viewModel.selectTicket(ticket) },
                            onStarToggle = { viewModel.toggleTicketStar(ticket.id) }
                        )
                    }
                }
            }
        }
    }

    // Create Ticket Modal Dialog
    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, desc, cat, prio, name, email, dept, system ->
                viewModel.createTicket(title, desc, cat, prio, name, email, dept, system)
                showCreateDialog = false
            }
        )
    }

    // Selected Ticket Details Modal
    selectedTicket?.let { ticket ->
        TicketDetailDialog(
            ticket = ticket,
            onDismiss = { viewModel.selectTicket(null) },
            onUpdateStatus = { newStatus, notes ->
                viewModel.updateTicketStatus(ticket.id, newStatus, notes)
            },
            onDelete = {
                viewModel.deleteTicket(ticket)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, cat: String, prio: String, name: String, email: String, dept: String, system: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    var reporterEmail by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Engineering") }
    var affectedSystem by remember { mutableStateOf("") }

    val categories = listOf(
        "Network & Wi-Fi",
        "Operating System",
        "Cloud & Servers",
        "IAM & Access",
        "Hardware & Devices",
        "Cybersecurity & Phishing",
        "Printers & Peripherals"
    )
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val priorities = listOf("P1", "P2", "P3", "P4")
    var selectedPriority by remember { mutableStateOf("P3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Incident / Request Ticket", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Ticket Title *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_ticket_title_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Urgency Priority Level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        priorities.forEach { prio ->
                            FilterChip(
                                selected = selectedPriority == prio,
                                onClick = { selectedPriority = prio },
                                label = { Text(prio) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (prio == "P1") RoseStatusCritical else CyanPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = affectedSystem,
                        onValueChange = { affectedSystem = it },
                        label = { Text("Affected Host / Asset (e.g. Cisco SW-01, Laptop)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = reporterName,
                        onValueChange = { reporterName = it },
                        label = { Text("Reporter Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = reporterEmail,
                        onValueChange = { reporterEmail = it },
                        label = { Text("Reporter Corporate Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val dictated = "Voice Dictated Incident: Technicians report core switch port 0/1 flapping. SFP+ optical transceiver showing intermittent link loss. Dispatching fiber cleaning kit."
                                description = if (description.isBlank()) dictated else "$description\n\n$dictated"
                                if (title.isBlank()) title = "Core Switch Port 0/1 Link Flapping"
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = EmeraldStatusOk, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🎙️ Dictate Incident with Gemini AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldStatusOk,
                                modifier = Modifier.weight(1f)
                            )
                            Text("Insert Voice Note", fontSize = 10.sp, color = CyanLight)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Error Details *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("new_ticket_desc_input"),
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onSubmit(
                            title,
                            description,
                            selectedCategory,
                            selectedPriority,
                            if (reporterName.isBlank()) "Anonymous Colleague" else reporterName,
                            reporterEmail,
                            department,
                            affectedSystem
                        )
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("submit_ticket_button")
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TicketDetailDialog(
    ticket: TicketEntity,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: String, notes: String) -> Unit,
    onDelete: () -> Unit
) {
    var notes by remember { mutableStateOf(ticket.resolutionNotes) }
    var currentStatus by remember { mutableStateOf(ticket.status) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(ticket.updatedAt) { dateFormat.format(Date(ticket.updatedAt)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(ticket.ticketNumber, fontFamily = FontFamily.Monospace, color = CyanLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(ticket.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PriorityBadge(priority = ticket.priority)
                        StatusBadge(status = currentStatus)
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("DETAILS & LOGISTICS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanLight, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Reporter: ${ticket.reporterName} (${ticket.department})", fontSize = 12.sp)
                            if (ticket.reporterEmail.isNotBlank()) {
                                Text("Email: ${ticket.reporterEmail}", fontSize = 12.sp)
                            }
                            if (ticket.affectedSystem.isNotBlank()) {
                                Text("Affected System: ${ticket.affectedSystem}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("Assigned Engineer: ${ticket.assignedEngineer}", fontSize = 12.sp)
                            Text("Last Updated: $formattedDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Text("Issue Description:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(ticket.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                item {
                    Text("Engineering Resolution Notes:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("ticket_resolution_notes_input"),
                        placeholder = { Text("Enter root cause, diagnostic findings or fix applied...") },
                        maxLines = 3
                    )
                }

                item {
                    Text("Update Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("Open", "In Progress", "Resolved")
                        statuses.forEach { st ->
                            FilterChip(
                                selected = currentStatus.equals(st, ignoreCase = true),
                                onClick = { currentStatus = st },
                                label = { Text(st, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (st == "Resolved") EmeraldStatusOk else CyanPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateStatus(currentStatus, notes)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("save_ticket_changes_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            IconButton(
                onClick = {
                    onDelete()
                    onDismiss()
                },
                modifier = Modifier.testTag("delete_ticket_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Ticket", tint = RoseStatusCritical)
            }
        }
    )
}
