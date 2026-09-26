package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AssetEntity
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
import com.example.ui.theme.TechCardBorderDark
import com.example.ui.theme.TechSurfaceDark
import com.example.ui.theme.TechSurfaceElevatedDark
import com.example.viewmodel.HelpDeskViewModel

@Composable
fun AssetsScreen(
    viewModel: HelpDeskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assets by viewModel.assets.collectAsStateWithLifecycle()
    val searchQuery by viewModel.assetSearchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.assetCategoryFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.assetStatusFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var assetToEdit by remember { mutableStateOf<AssetEntity?>(null) }
    var assetToDelete by remember { mutableStateOf<AssetEntity?>(null) }

    val categories = listOf("All", "Laptop", "Server", "Switch", "Firewall", "Workstation", "Network", "Mobile", "Printer")
    val statuses = listOf("All", "In Use", "Spare Inventory", "Under Maintenance", "Decommissioned")

    // Stats calculation
    val inUseCount = assets.count { it.status == "In Use" }
    val spareCount = assets.count { it.status == "Spare Inventory" }
    val maintenanceCount = assets.count { it.status == "Under Maintenance" }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("assets_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_asset_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add IT Asset")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Asset", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total KPI
                AssetStatCard(
                    title = "Total Assets",
                    count = assets.size.toString(),
                    indicatorColor = CyanLight,
                    modifier = Modifier.weight(1f)
                )
                // In Use KPI
                AssetStatCard(
                    title = "In Use",
                    count = inUseCount.toString(),
                    indicatorColor = EmeraldStatusOk,
                    modifier = Modifier.weight(1f)
                )
                // Spare KPI
                AssetStatCard(
                    title = "Spare Stock",
                    count = spareCount.toString(),
                    indicatorColor = CyanGlow,
                    modifier = Modifier.weight(1f)
                )
                // Maintenance KPI
                AssetStatCard(
                    title = "Repair",
                    count = maintenanceCount.toString(),
                    indicatorColor = AmberStatusWarning,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setAssetSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("asset_search_input"),
                placeholder = { Text("Search hardware model, serial no, user, tag...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanLight) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setAssetSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = categoryFilter.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAssetCategoryFilter(cat) },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimaryContainer,
                            selectedLabelColor = CyanLight,
                            selectedLeadingIconColor = CyanLight
                        )
                    )
                }
            }

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                statuses.forEach { status ->
                    val isSelected = statusFilter.equals(status, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAssetStatusFilter(status) },
                        label = { Text(status, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Asset List
            if (assets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No IT assets found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search query or filter chips",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.setAssetSearchQuery("")
                                viewModel.setAssetCategoryFilter("All")
                                viewModel.setAssetStatusFilter("All")
                            }
                        ) {
                            Text("Reset All Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(assets, key = { it.id }) { asset ->
                        AssetItemCard(
                            asset = asset,
                            onEdit = { assetToEdit = asset },
                            onDelete = { assetToDelete = asset },
                            onQuickStatusChange = { newStatus ->
                                viewModel.updateAssetStatus(asset.id, newStatus)
                            },
                            onCopyText = { label, text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(label, text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Asset Dialog
    if (showAddDialog) {
        AssetFormDialog(
            title = "Inventory New IT Asset",
            confirmButtonText = "Save Asset to Database",
            initialAsset = null,
            onDismiss = { showAddDialog = false },
            onSubmit = { newAsset ->
                viewModel.createAsset(
                    tag = newAsset.assetTag,
                    hardwareModel = newAsset.hardwareModel,
                    category = newAsset.category,
                    serialNumber = newAsset.serialNumber,
                    assignedUser = newAsset.assignedUser,
                    department = newAsset.department,
                    location = newAsset.location,
                    status = newAsset.status,
                    ipAddress = newAsset.ipAddress,
                    macAddress = newAsset.macAddress,
                    purchaseDate = newAsset.purchaseDate,
                    warrantyExpiry = newAsset.warrantyExpiry,
                    specifications = newAsset.specifications,
                    notes = newAsset.notes
                )
                showAddDialog = false
                Toast.makeText(context, "Asset ${newAsset.assetTag} saved to Room database", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Asset Dialog
    assetToEdit?.let { asset ->
        AssetFormDialog(
            title = "Edit IT Asset (${asset.assetTag})",
            confirmButtonText = "Update Asset Record",
            initialAsset = asset,
            onDismiss = { assetToEdit = null },
            onSubmit = { updatedAsset ->
                viewModel.updateAsset(updatedAsset.copy(id = asset.id))
                assetToEdit = null
                Toast.makeText(context, "Asset ${asset.assetTag} updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    assetToDelete?.let { asset ->
        AlertDialog(
            onDismissRequest = { assetToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseStatusCritical) },
            title = { Text("Delete Asset?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete asset ${asset.assetTag} (${asset.hardwareModel}) assigned to ${asset.assignedUser} from the Room database? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAsset(asset)
                        assetToDelete = null
                        Toast.makeText(context, "Asset deleted from database", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseStatusCritical)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { assetToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AssetStatCard(
    title: String,
    count: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AssetItemCard(
    asset: AssetEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickStatusChange: (String) -> Unit,
    onCopyText: (label: String, text: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("asset_card_${asset.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Asset Tag + Category + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(0.5.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = asset.assetTag,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CyanLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = CyanPrimaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(asset.category),
                                contentDescription = null,
                                tint = CyanLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = asset.category,
                                color = CyanLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Status Badge
                val (statusContainer, statusContent) = when (asset.status) {
                    "In Use" -> Pair(EmeraldContainer, EmeraldStatusOk)
                    "Spare Inventory" -> Pair(Color(0xFF0C4A6E), CyanGlow)
                    "Under Maintenance" -> Pair(AmberContainer, AmberStatusWarning)
                    else -> Pair(Color(0xFF334155), Color(0xFFCBD5E1))
                }

                Surface(
                    color = statusContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = asset.status,
                        color = statusContent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hardware Model (Mandatory Field from prompt)
            Text(
                text = asset.hardwareModel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("asset_hardware_model_${asset.id}")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Serial Number (Mandatory Field from prompt)
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyText("Serial Number", asset.serialNumber) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SERIAL NUMBER: ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = asset.serialNumber,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.testTag("asset_serial_number_${asset.id}")
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Serial Number",
                        tint = CyanLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned User (Mandatory Field from prompt)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CyanPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CyanLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Assigned to: ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = asset.assignedUser,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("asset_assigned_user_${asset.id}")
                        )
                    }
                    if (asset.department.isNotBlank()) {
                        Text(
                            text = "Dept: ${asset.department}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location & Network Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = asset.location.ifBlank { "Unassigned Location" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (asset.ipAddress.isNotBlank()) {
                    Text(
                        text = "IP: ${asset.ipAddress}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanLight,
                        modifier = Modifier.clickable { onCopyText("IP Address", asset.ipAddress) }
                    )
                }
            }

            if (asset.specifications.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = asset.specifications,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row (Edit, Quick Status, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick status change buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (asset.status != "In Use") {
                        Surface(
                            color = EmeraldContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("In Use") }
                        ) {
                            Text(
                                text = "Set In Use",
                                color = EmeraldStatusOk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (asset.status != "Spare Inventory") {
                        Surface(
                            color = Color(0xFF0C4A6E),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Spare Inventory") }
                        ) {
                            Text(
                                text = "Mark Spare",
                                color = CyanGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (asset.status != "Under Maintenance") {
                        Surface(
                            color = AmberContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Under Maintenance") }
                        ) {
                            Text(
                                text = "Send Repair",
                                color = AmberStatusWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Edit & Delete Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("asset_edit_button_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Asset",
                            tint = CyanLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("asset_delete_button_${asset.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Asset",
                            tint = RoseStatusCritical,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetFormDialog(
    title: String,
    confirmButtonText: String,
    initialAsset: AssetEntity?,
    onDismiss: () -> Unit,
    onSubmit: (AssetEntity) -> Unit
) {
    var hardwareModel by remember { mutableStateOf(initialAsset?.hardwareModel ?: "") }
    var serialNumber by remember { mutableStateOf(initialAsset?.serialNumber ?: "") }
    var assignedUser by remember { mutableStateOf(initialAsset?.assignedUser ?: "") }
    var assetTag by remember { mutableStateOf(initialAsset?.assetTag ?: "AST-${(100..999).random()}") }

    val categories = listOf("Laptop", "Server", "Switch", "Firewall", "Workstation", "Network", "Mobile", "Printer", "Peripheral")
    var selectedCategory by remember { mutableStateOf(initialAsset?.category ?: categories[0]) }
    var catExpanded by remember { mutableStateOf(false) }

    val statuses = listOf("In Use", "Spare Inventory", "Under Maintenance", "Decommissioned")
    var selectedStatus by remember { mutableStateOf(initialAsset?.status ?: statuses[0]) }
    var statusExpanded by remember { mutableStateOf(false) }

    var department by remember { mutableStateOf(initialAsset?.department ?: "Engineering") }
    var location by remember { mutableStateOf(initialAsset?.location ?: "HQ Server Room Rack A1") }
    var ipAddress by remember { mutableStateOf(initialAsset?.ipAddress ?: "") }
    var macAddress by remember { mutableStateOf(initialAsset?.macAddress ?: "") }
    var specifications by remember { mutableStateOf(initialAsset?.specifications ?: "") }
    var notes by remember { mutableStateOf(initialAsset?.notes ?: "") }
    var warrantyExpiry by remember { mutableStateOf(initialAsset?.warrantyExpiry ?: "2027-12-31") }

    val isFormValid = hardwareModel.isNotBlank() && serialNumber.isNotBlank() && assignedUser.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = CyanLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Enter hardware model, serial number, and assigned user details below to save into the Room database.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Hardware Model Field (Prompt requirement)
                item {
                    OutlinedTextField(
                        value = hardwareModel,
                        onValueChange = { hardwareModel = it },
                        label = { Text("Hardware Model *") },
                        placeholder = { Text("e.g. MacBook Pro 16\" M3, Dell PowerEdge R750") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_hardware_model"),
                        singleLine = true,
                        isError = hardwareModel.isBlank()
                    )
                }

                // Serial Number Field (Prompt requirement)
                item {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number *") },
                        placeholder = { Text("e.g. C02FM3K8MD6R, 7X89B2-S1") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_serial_number"),
                        singleLine = true,
                        isError = serialNumber.isBlank()
                    )
                }

                // Assigned User Field (Prompt requirement)
                item {
                    OutlinedTextField(
                        value = assignedUser,
                        onValueChange = { assignedUser = it },
                        label = { Text("Assigned User *") },
                        placeholder = { Text("e.g. Alex Rivera (Lead Architect), IT Spare Pool") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_assigned_user"),
                        singleLine = true,
                        isError = assignedUser.isBlank()
                    )
                }

                // Asset Tag
                item {
                    OutlinedTextField(
                        value = assetTag,
                        onValueChange = { assetTag = it },
                        label = { Text("Asset Tag / Barcode") },
                        placeholder = { Text("e.g. AST-NB-104") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Category Dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = !catExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Device Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Status Dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Inventory Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        selectedStatus = st
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Department & Location
                item {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        placeholder = { Text("e.g. Engineering, SecOps, Sales") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Office / Rack") },
                        placeholder = { Text("e.g. HQ Server Room Rack A1, Floor 4 Desk 12") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Technical Specs
                item {
                    OutlinedTextField(
                        value = specifications,
                        onValueChange = { specifications = it },
                        label = { Text("Hardware Specifications (Optional)") },
                        placeholder = { Text("e.g. 64GB DDR5, 2TB SSD, M3 Max 16-Core") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Network IP & MAC (Optional)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ipAddress,
                            onValueChange = { ipAddress = it },
                            label = { Text("IP Address") },
                            placeholder = { Text("10.0.10.15") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = macAddress,
                            onValueChange = { macAddress = it },
                            label = { Text("MAC Address") },
                            placeholder = { Text("D8:9D:67...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Warranty & Notes
                item {
                    OutlinedTextField(
                        value = warrantyExpiry,
                        onValueChange = { warrantyExpiry = it },
                        label = { Text("Warranty Expiration (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Asset Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        val asset = AssetEntity(
                            id = initialAsset?.id ?: 0,
                            assetTag = assetTag.trim().uppercase(),
                            hardwareModel = hardwareModel.trim(),
                            category = selectedCategory,
                            serialNumber = serialNumber.trim().uppercase(),
                            assignedUser = assignedUser.trim(),
                            department = department.trim(),
                            location = location.trim(),
                            status = selectedStatus,
                            ipAddress = ipAddress.trim(),
                            macAddress = macAddress.trim().uppercase(),
                            warrantyExpiry = warrantyExpiry.trim(),
                            specifications = specifications.trim(),
                            notes = notes.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSubmit(asset)
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier.testTag("btn_save_asset")
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "laptop" -> Icons.Default.Laptop
        "server" -> Icons.Default.Storage
        "switch", "router" -> Icons.Default.Router
        "firewall" -> Icons.Default.Security
        "workstation" -> Icons.Default.Computer
        "mobile", "tablet" -> Icons.Default.PhoneAndroid
        "printer" -> Icons.Default.Print
        else -> Icons.Default.Inventory2
    }
}
