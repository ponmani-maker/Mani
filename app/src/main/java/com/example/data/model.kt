package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TicketPriority(val label: String, val levelCode: String) {
    P1("P1 - Critical Outage", "P1"),
    P2("P2 - High Urgency", "P2"),
    P3("P3 - Medium Standard", "P3"),
    P4("P4 - Low / Request", "P4")
}

enum class TicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    PENDING_USER("Pending User"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class TicketCategory(val displayName: String, val iconName: String) {
    NETWORK("Network & Wi-Fi", "wifi"),
    SYSTEM_OS("Operating System", "computer"),
    CLOUD_SERVERS("Cloud & Servers", "cloud"),
    IAM_ACCOUNTS("IAM & Access", "security"),
    HARDWARE("Hardware & Devices", "hardware"),
    SECURITY("Cybersecurity & Phishing", "shield"),
    PRINTERS("Printers & Peripherals", "print")
}

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketNumber: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String, // P1, P2, P3, P4
    val status: String,   // Open, In Progress, etc.
    val reporterName: String,
    val reporterEmail: String,
    val department: String,
    val affectedSystem: String = "",
    val assignedEngineer: String = "Unassigned",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resolutionNotes: String = "",
    val isStarred: Boolean = false
)

enum class AssetStatus(val label: String) {
    IN_USE("In Use"),
    SPARE_INVENTORY("Spare Inventory"),
    UNDER_MAINTENANCE("Under Maintenance"),
    DECOMMISSIONED("Decommissioned")
}

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetTag: String,             // e.g. "AST-SRV-01"
    val hardwareModel: String,        // e.g. "Dell PowerEdge R750"
    val category: String,             // Server, Laptop, Switch, Firewall, Workstation, Tablet, Printer
    val serialNumber: String,         // e.g. "7X89B2-S1"
    val assignedUser: String,         // e.g. "Alex Rivera (Lead Architect)"
    val department: String = "IT Infrastructure",
    val location: String = "HQ Server Room Rack A1",
    val status: String = "In Use",    // In Use, Spare Inventory, Under Maintenance, Decommissioned
    val ipAddress: String = "",
    val macAddress: String = "",
    val purchaseDate: String = "2024-01-15",
    val warrantyExpiry: String = "2027-12-31",
    val specifications: String = "",  // e.g. "64GB RAM, 2TB SSD"
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Backward compatibility accessor for components expecting .name
    val name: String
        get() = hardwareModel
}

data class ServiceStatusItem(
    val id: String,
    val name: String,
    val category: String,
    val isOperational: Boolean,
    val statusText: String,
    val uptimePercent: Double,
    val responseTimeMs: Int,
    val lastIncident: String = "None in 30 days"
)

data class Runbook(
    val id: String,
    val title: String,
    val category: String,
    val symptom: String,
    val rootCause: String,
    val steps: List<String>,
    val terminalCommands: List<String> = emptyList(),
    val severityNotice: String = ""
)

data class PingResult(
    val host: String,
    val ip: String = "",
    val isReachable: Boolean,
    val latencyMs: Long = 0,
    val details: String = ""
)

data class PortCheckResult(
    val port: Int,
    val serviceName: String,
    val isOpen: Boolean,
    val latencyMs: Long = 0,
    val message: String = ""
)

data class SubnetCalculation(
    val networkAddress: String,
    val subnetMask: String,
    val wildcardMask: String,
    val broadcastAddress: String,
    val firstUsableHost: String,
    val lastUsableHost: String,
    val totalHosts: Long,
    val usableHosts: Long,
    val cidrPrefix: Int
)

data class HelpDeskRating(
    val stars: Int,
    val reviewerName: String,
    val feedback: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MemoryHealthStatus(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedRamMb: Long,
    val usedRamPercent: Int,
    val isLowMemory: Boolean,
    val lowMemThresholdMb: Long,
    val jvmMaxHeapMb: Long,
    val jvmTotalHeapMb: Long,
    val jvmFreeHeapMb: Long,
    val jvmUsedHeapMb: Long,
    val totalStorageGb: Double,
    val availableStorageGb: Double,
    val usedStorageGb: Double,
    val usedStoragePercent: Int
)

data class NetworkHealthStatus(
    val isConnected: Boolean,
    val hasInternetCapability: Boolean,
    val isMetered: Boolean,
    val transportType: String, // Wi-Fi, Cellular, Ethernet, Offline
    val localIp: String,
    val gatewayDns: String,
    val dnsResolvingOk: Boolean,
    val dnsLatencyMs: Long,
    val linkDownstreamBandwidthKbps: Int,
    val linkUpstreamBandwidthKbps: Int,
    val statusSummary: String
)

enum class HealthCheckGrade(val label: String) {
    OPTIMAL("Optimal"),
    WARNING("Warning"),
    CRITICAL("Critical")
}

data class HealthCheckItem(
    val title: String,
    val metricValue: String,
    val grade: HealthCheckGrade,
    val details: String
)

data class SystemHealthReport(
    val memory: MemoryHealthStatus,
    val network: NetworkHealthStatus,
    val overallScorePercent: Int,
    val checks: List<HealthCheckItem>,
    val scannedAtTimestamp: Long = System.currentTimeMillis()
)
