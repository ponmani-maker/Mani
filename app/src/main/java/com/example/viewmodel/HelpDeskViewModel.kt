package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AssetEntity
import com.example.data.HelpDeskRating
import com.example.data.HelpDeskRepository
import com.example.data.PingResult
import com.example.data.PortCheckResult
import com.example.data.Runbook
import com.example.data.ServiceStatusItem
import com.example.data.SubnetCalculation
import com.example.data.SystemHealthReport
import com.example.data.TicketEntity
import com.example.data.GeminiService
import com.example.data.GroundingResult
import com.example.data.ImageGenResult
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    HOME("Overview"),
    TICKETS("Tickets"),
    DIAGNOSTICS("Diagnostics"),
    AI_COPILOT("AI Copilot"),
    SERVICES("Services"),
    RUNBOOKS("Runbooks"),
    ASSETS("Assets")
}

class HelpDeskViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = HelpDeskRepository(database.ticketDao(), database.assetDao(), application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Tickets ---
    private val _ticketSearchQuery = MutableStateFlow("")
    val ticketSearchQuery: StateFlow<String> = _ticketSearchQuery.asStateFlow()

    private val _ticketStatusFilter = MutableStateFlow("All")
    val ticketStatusFilter: StateFlow<String> = _ticketStatusFilter.asStateFlow()

    private val _selectedTicket = MutableStateFlow<TicketEntity?>(null)
    val selectedTicket: StateFlow<TicketEntity?> = _selectedTicket.asStateFlow()

    val tickets: StateFlow<List<TicketEntity>> = combine(
        repository.allTickets,
        _ticketSearchQuery,
        _ticketStatusFilter
    ) { all, query, filter ->
        all.filter { ticket ->
            val matchesFilter = when (filter) {
                "All" -> true
                else -> ticket.status.equals(filter, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) true else {
                ticket.title.contains(query, ignoreCase = true) ||
                ticket.description.contains(query, ignoreCase = true) ||
                ticket.ticketNumber.contains(query, ignoreCase = true) ||
                ticket.category.contains(query, ignoreCase = true) ||
                ticket.reporterName.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTicketsCount: StateFlow<Int> = repository.activeTicketsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setTicketSearchQuery(query: String) {
        _ticketSearchQuery.value = query
    }

    fun setTicketStatusFilter(filter: String) {
        _ticketStatusFilter.value = filter
    }

    fun selectTicket(ticket: TicketEntity?) {
        _selectedTicket.value = ticket
    }

    fun createTicket(
        title: String,
        description: String,
        category: String,
        priority: String,
        reporterName: String,
        reporterEmail: String,
        department: String,
        affectedSystem: String
    ) {
        viewModelScope.launch {
            val nextNumber = "INC-" + (1030 + (0..999).random())
            val newTicket = TicketEntity(
                ticketNumber = nextNumber,
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                status = "Open",
                reporterName = reporterName.trim(),
                reporterEmail = reporterEmail.trim(),
                department = department.trim(),
                affectedSystem = affectedSystem.trim(),
                assignedEngineer = "Marcus Vance (SysAdmin)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.createTicket(newTicket)
        }
    }

    fun updateTicketStatus(id: Long, newStatus: String, notes: String) {
        viewModelScope.launch {
            repository.updateTicketStatus(id, newStatus, notes)
            if (_selectedTicket.value?.id == id) {
                _selectedTicket.value = _selectedTicket.value?.copy(
                    status = newStatus,
                    resolutionNotes = notes,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    fun toggleTicketStar(id: Long) {
        viewModelScope.launch {
            repository.toggleTicketStar(id)
            if (_selectedTicket.value?.id == id) {
                _selectedTicket.value = _selectedTicket.value?.let { it.copy(isStarred = !it.isStarred) }
            }
        }
    }

    fun deleteTicket(ticket: TicketEntity) {
        viewModelScope.launch {
            repository.deleteTicket(ticket)
            if (_selectedTicket.value?.id == ticket.id) {
                _selectedTicket.value = null
            }
        }
    }

    // --- Assets ---
    private val _assetSearchQuery = MutableStateFlow("")
    val assetSearchQuery: StateFlow<String> = _assetSearchQuery.asStateFlow()

    private val _assetCategoryFilter = MutableStateFlow("All")
    val assetCategoryFilter: StateFlow<String> = _assetCategoryFilter.asStateFlow()

    private val _assetStatusFilter = MutableStateFlow("All")
    val assetStatusFilter: StateFlow<String> = _assetStatusFilter.asStateFlow()

    val assets: StateFlow<List<AssetEntity>> = combine(
        repository.allAssets,
        _assetSearchQuery,
        _assetCategoryFilter,
        _assetStatusFilter
    ) { all, query, category, status ->
        all.filter { asset ->
            val matchesCategory = if (category == "All") true else asset.category.equals(category, ignoreCase = true)
            val matchesStatus = if (status == "All") true else asset.status.equals(status, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                asset.hardwareModel.contains(query, ignoreCase = true) ||
                asset.serialNumber.contains(query, ignoreCase = true) ||
                asset.assignedUser.contains(query, ignoreCase = true) ||
                asset.assetTag.contains(query, ignoreCase = true) ||
                asset.department.contains(query, ignoreCase = true) ||
                asset.location.contains(query, ignoreCase = true) ||
                asset.ipAddress.contains(query, ignoreCase = true) ||
                asset.specifications.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesStatus && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAssetSearchQuery(query: String) {
        _assetSearchQuery.value = query
    }

    fun setAssetCategoryFilter(category: String) {
        _assetCategoryFilter.value = category
    }

    fun setAssetStatusFilter(status: String) {
        _assetStatusFilter.value = status
    }

    fun createAsset(
        tag: String,
        hardwareModel: String,
        category: String,
        serialNumber: String,
        assignedUser: String,
        department: String = "IT Infrastructure",
        location: String = "HQ Server Room",
        status: String = "In Use",
        ipAddress: String = "",
        macAddress: String = "",
        purchaseDate: String = "2024-01-15",
        warrantyExpiry: String = "2027-12-31",
        specifications: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val asset = AssetEntity(
                assetTag = tag.trim().uppercase(),
                hardwareModel = hardwareModel.trim(),
                category = category,
                serialNumber = serialNumber.trim().uppercase(),
                assignedUser = assignedUser.trim(),
                department = department.trim(),
                location = location.trim(),
                status = status,
                ipAddress = ipAddress.trim(),
                macAddress = macAddress.trim().uppercase(),
                purchaseDate = purchaseDate.trim(),
                warrantyExpiry = warrantyExpiry.trim(),
                specifications = specifications.trim(),
                notes = notes.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.createAsset(asset)
        }
    }

    fun updateAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAsset(asset.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateAssetStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateAssetStatus(id, status)
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    // --- Diagnostics: Ping ---
    private val _pingHost = MutableStateFlow("8.8.8.8")
    val pingHost: StateFlow<String> = _pingHost.asStateFlow()

    private val _pingResult = MutableStateFlow<PingResult?>(null)
    val pingResult: StateFlow<PingResult?> = _pingResult.asStateFlow()

    private val _isPinging = MutableStateFlow(false)
    val isPinging: StateFlow<Boolean> = _isPinging.asStateFlow()

    fun setPingHost(host: String) {
        _pingHost.value = host
    }

    fun executePing(target: String? = null) {
        val hostToPing = target ?: _pingHost.value
        _isPinging.value = true
        viewModelScope.launch {
            val result = repository.pingHost(hostToPing)
            _pingResult.value = result
            _isPinging.value = false
        }
    }

    // --- Diagnostics: Port Scanner ---
    private val _portScanHost = MutableStateFlow("1.1.1.1")
    val portScanHost: StateFlow<String> = _portScanHost.asStateFlow()

    private val _portScanResults = MutableStateFlow<List<PortCheckResult>>(emptyList())
    val portScanResults: StateFlow<List<PortCheckResult>> = _portScanResults.asStateFlow()

    private val _isPortScanning = MutableStateFlow(false)
    val isPortScanning: StateFlow<Boolean> = _isPortScanning.asStateFlow()

    fun setPortScanHost(host: String) {
        _portScanHost.value = host
    }

    fun executePortScan() {
        val host = _portScanHost.value
        _isPortScanning.value = true
        _portScanResults.value = emptyList()
        val portsToCheck = listOf(
            Pair(80, "HTTP Web"),
            Pair(443, "HTTPS Web SSL"),
            Pair(53, "DNS Resolver"),
            Pair(22, "SSH Remote Shell"),
            Pair(3389, "RDP Windows Desktop"),
            Pair(8080, "HTTP Alternate / Proxy"),
            Pair(3306, "MySQL Database"),
            Pair(445, "SMB Windows File Share")
        )
        viewModelScope.launch {
            val results = mutableListOf<PortCheckResult>()
            for (p in portsToCheck) {
                val res = repository.checkPort(host, p.first, p.second)
                results.add(res)
                _portScanResults.value = results.toList()
            }
            _isPortScanning.value = false
        }
    }

    // --- Diagnostics: Subnet Calculator ---
    private val _subnetIp = MutableStateFlow("192.168.1.100")
    val subnetIp: StateFlow<String> = _subnetIp.asStateFlow()

    private val _subnetPrefix = MutableStateFlow("24")
    val subnetPrefix: StateFlow<String> = _subnetPrefix.asStateFlow()

    private val _subnetResult = MutableStateFlow<SubnetCalculation?>(null)
    val subnetResult: StateFlow<SubnetCalculation?> = _subnetResult.asStateFlow()

    init {
        // Calculate default subnet on startup
        calculateSubnet("192.168.1.100", "24")
    }

    fun setSubnetIp(ip: String) {
        _subnetIp.value = ip
        calculateSubnet(ip, _subnetPrefix.value)
    }

    fun setSubnetPrefix(prefix: String) {
        _subnetPrefix.value = prefix
        calculateSubnet(_subnetIp.value, prefix)
    }

    private fun calculateSubnet(ip: String, prefix: String) {
        _subnetResult.value = repository.calculateSubnet(ip, prefix)
    }

    // --- Password Generator ---
    private val _passwordLength = MutableStateFlow(16)
    val passwordLength: StateFlow<Int> = _passwordLength.asStateFlow()

    private val _includeSymbols = MutableStateFlow(true)
    val includeSymbols: StateFlow<Boolean> = _includeSymbols.asStateFlow()

    private val _includeNumbers = MutableStateFlow(true)
    val includeNumbers: StateFlow<Boolean> = _includeNumbers.asStateFlow()

    private val _generatedPassword = MutableStateFlow("")
    val generatedPassword: StateFlow<String> = _generatedPassword.asStateFlow()

    fun setPasswordLength(len: Int) {
        _passwordLength.value = len
        generateNewPassword()
    }

    fun toggleIncludeSymbols() {
        _includeSymbols.value = !_includeSymbols.value
        generateNewPassword()
    }

    fun toggleIncludeNumbers() {
        _includeNumbers.value = !_includeNumbers.value
        generateNewPassword()
    }

    fun generateNewPassword() {
        _generatedPassword.value = repository.generateSecurePassword(
            length = _passwordLength.value,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = _includeNumbers.value,
            includeSymbols = _includeSymbols.value
        )
    }

    // --- Hash Generator ---
    private val _hashInputText = MutableStateFlow("IT-HelpDesk-Secure2026")
    val hashInputText: StateFlow<String> = _hashInputText.asStateFlow()

    private val _computedHashes = MutableStateFlow(Pair("", ""))
    val computedHashes: StateFlow<Pair<String, String>> = _computedHashes.asStateFlow()

    fun setHashInput(text: String) {
        _hashInputText.value = text
        _computedHashes.value = repository.computeHashes(text)
    }

    // --- System Health Diagnostics (Memory & Network) ---
    private val _systemHealthReport = MutableStateFlow<SystemHealthReport?>(null)
    val systemHealthReport: StateFlow<SystemHealthReport?> = _systemHealthReport.asStateFlow()

    private val _isHealthScanning = MutableStateFlow(false)
    val isHealthScanning: StateFlow<Boolean> = _isHealthScanning.asStateFlow()

    fun refreshSystemHealth() {
        _isHealthScanning.value = true
        viewModelScope.launch {
            val report = repository.getSystemHealthReport()
            _systemHealthReport.value = report
            _isHealthScanning.value = false
        }
    }

    // --- Device Telemetry ---
    private val _deviceTelemetry = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceTelemetry: StateFlow<Map<String, String>> = _deviceTelemetry.asStateFlow()

    fun refreshTelemetry() {
        _deviceTelemetry.value = repository.getDeviceTelemetry()
    }

    // --- Enterprise Services ---
    private val _enterpriseServices = MutableStateFlow<List<ServiceStatusItem>>(emptyList())
    val enterpriseServices: StateFlow<List<ServiceStatusItem>> = _enterpriseServices.asStateFlow()

    fun refreshServices() {
        _enterpriseServices.value = repository.getEnterpriseServices()
    }

    // --- Runbooks Knowledge Base ---
    private val _runbookSearchQuery = MutableStateFlow("")
    val runbookSearchQuery: StateFlow<String> = _runbookSearchQuery.asStateFlow()

    private val _allRunbooks = MutableStateFlow(repository.getRunbooks())
    val filteredRunbooks: StateFlow<List<Runbook>> = combine(
        _allRunbooks,
        _runbookSearchQuery
    ) { list, query ->
        if (query.isBlank()) list else {
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.symptom.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.rootCause.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRunbookSearchQuery(q: String) {
        _runbookSearchQuery.value = q
    }

    // --- 5-Star CSAT Rating ---
    private val _userRatings = MutableStateFlow<List<HelpDeskRating>>(
        listOf(
            HelpDeskRating(5, "Sarah Jenkins (VP Ops)", "Phenomenal support! Resolved our core switch outage in under 10 minutes."),
            HelpDeskRating(5, "Ken Tanaka (Software Eng)", "The runbooks and network diagnostics save our team hours every sprint. 5-stars!"),
            HelpDeskRating(5, "Maria Gonzalez (DevOps)", "Super responsive system engineers. Best internal IT service desk ever.")
        )
    )
    val userRatings: StateFlow<List<HelpDeskRating>> = _userRatings.asStateFlow()

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    fun setShowRatingDialog(show: Boolean) {
        _showRatingDialog.value = show
    }

    fun submitRating(stars: Int, name: String, comment: String) {
        val newRating = HelpDeskRating(
            stars = stars,
            reviewerName = if (name.isBlank()) "Anonymous Colleague" else name.trim(),
            feedback = if (comment.isBlank()) "Outstanding system engineering support! All 5 stars!" else comment.trim(),
            timestamp = System.currentTimeMillis()
        )
        _userRatings.value = listOf(newRating) + _userRatings.value
        _showRatingDialog.value = false
    }

    // --- AI Copilot Capabilities (Maps Grounding, Voice Transcribe, Diagram Studio) ---
    val geminiService = GeminiService()

    // 1. Google Maps Grounding (gemini-2.5-flash with google_maps tool)
    private val _mapsQuery = MutableStateFlow("Enterprise Dell & HP Server Repair Centers and Datacenters nearby")
    val mapsQuery: StateFlow<String> = _mapsQuery.asStateFlow()

    private val _mapsGroundingResult = MutableStateFlow<GroundingResult?>(null)
    val mapsGroundingResult: StateFlow<GroundingResult?> = _mapsGroundingResult.asStateFlow()

    private val _isMapsLoading = MutableStateFlow(false)
    val isMapsLoading: StateFlow<Boolean> = _isMapsLoading.asStateFlow()

    fun setMapsQuery(q: String) {
        _mapsQuery.value = q
    }

    fun searchMapsGrounding(queryOverride: String? = null) {
        val q = queryOverride ?: _mapsQuery.value
        if (queryOverride != null) {
            _mapsQuery.value = queryOverride
        }
        _isMapsLoading.value = true
        viewModelScope.launch {
            val result = geminiService.queryMapsGrounding(q)
            _mapsGroundingResult.value = result
            _isMapsLoading.value = false
        }
    }

    // 2. Audio Incident Transcription (gemini-2.5-flash)
    private val _transcriptionResult = MutableStateFlow("")
    val transcriptionResult: StateFlow<String> = _transcriptionResult.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    fun transcribeAudioData(audioBytes: ByteArray, mimeType: String = "audio/mp4") {
        _isTranscribing.value = true
        viewModelScope.launch {
            val text = geminiService.transcribeAudio(audioBytes, mimeType)
            _transcriptionResult.value = text
            _isTranscribing.value = false
        }
    }

    fun transcribeSampleIncident(presetIndex: Int) {
        val sampleBytes = "SAMPLE_IT_AUDIO_BYTES_$presetIndex".toByteArray()
        _isTranscribing.value = true
        viewModelScope.launch {
            if (geminiService.isApiKeyConfigured()) {
                val text = geminiService.transcribeAudio(sampleBytes, "audio/mp4")
                _transcriptionResult.value = text
            } else {
                _transcriptionResult.value = when (presetIndex) {
                    0 -> "Critical Incident Audio Note: Core BGP router in Building B Rack 04 lost optical link. Interface xe-0/0/1 flapping. Redundant path via Leaf-2 engaged. On-site fiber cleaning kit required."
                    1 -> "Hardware Dispatch Audio: Workstation asset tag LT-8842 locked out by BitLocker after UEFI firmware push. User is Sarah Jenkins in VP Ops. Requesting recovery key deployment."
                    else -> "Datacenter Alarm Note: CRAC unit 2 in Server Room Alpha compressor warning. Ambient intake temp 84°F. Switched to secondary chiller, monitoring rack thermal probes."
                }
            }
            _isTranscribing.value = false
        }
    }

    fun clearTranscription() {
        _transcriptionResult.value = ""
    }

    // 3. IT Diagram Generator & Editor (gemini-3.1-flash-image-preview)
    private val _diagramPrompt = MutableStateFlow("Enterprise Hybrid Cloud Architecture with AWS Transit Gateway, Fortinet Next-Gen Firewalls, and On-Premises Cisco Nexus Core")
    val diagramPrompt: StateFlow<String> = _diagramPrompt.asStateFlow()

    private val _diagramAspectRatio = MutableStateFlow("1:1")
    val diagramAspectRatio: StateFlow<String> = _diagramAspectRatio.asStateFlow()

    private val _diagramResult = MutableStateFlow<ImageGenResult?>(null)
    val diagramResult: StateFlow<ImageGenResult?> = _diagramResult.asStateFlow()

    private val _isGeneratingDiagram = MutableStateFlow(false)
    val isGeneratingDiagram: StateFlow<Boolean> = _isGeneratingDiagram.asStateFlow()

    private val _editInstruction = MutableStateFlow("")
    val editInstruction: StateFlow<String> = _editInstruction.asStateFlow()

    fun setDiagramPrompt(p: String) {
        _diagramPrompt.value = p
    }

    fun setDiagramAspectRatio(r: String) {
        _diagramAspectRatio.value = r
    }

    fun setEditInstruction(inst: String) {
        _editInstruction.value = inst
    }

    fun generateDiagram(promptOverride: String? = null) {
        val prompt = promptOverride ?: _diagramPrompt.value
        if (promptOverride != null) {
            _diagramPrompt.value = promptOverride
        }
        _isGeneratingDiagram.value = true
        viewModelScope.launch {
            val result = geminiService.generateOrEditImage(
                prompt = prompt,
                sourceBitmap = null,
                aspectRatio = _diagramAspectRatio.value
            )
            _diagramResult.value = result
            _isGeneratingDiagram.value = false
        }
    }

    fun editCurrentDiagram(instructionOverride: String? = null) {
        val instruction = instructionOverride ?: _editInstruction.value
        val currentBitmap = _diagramResult.value?.bitmap
        _isGeneratingDiagram.value = true
        viewModelScope.launch {
            val result = geminiService.generateOrEditImage(
                prompt = instruction,
                sourceBitmap = currentBitmap,
                aspectRatio = _diagramAspectRatio.value
            )
            _diagramResult.value = result
            _isGeneratingDiagram.value = false
        }
    }

    init {
        refreshSystemHealth()
        refreshTelemetry()
        refreshServices()
        generateNewPassword()
        setHashInput("IT-HelpDesk-Secure2026")
        searchMapsGrounding()
        generateDiagram()
    }
}
