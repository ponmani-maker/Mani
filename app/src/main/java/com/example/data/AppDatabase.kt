package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TicketEntity::class, AssetEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "it_help_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.ticketDao(), database.assetDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(ticketDao: TicketDao, assetDao: AssetDao) {
            val sampleTickets = listOf(
                TicketEntity(
                    ticketNumber = "INC-1025",
                    title = "Core Switch VLAN 20 Packet Drop on Floor 3",
                    description = "Engineering team reporting recurring ping timeouts and 35% packet drops reaching internal GitLab server. Trunk port 24 experiencing CRC errors.",
                    category = "Network & Wi-Fi",
                    priority = "P1",
                    status = "In Progress",
                    reporterName = "Alex Rivera",
                    reporterEmail = "alex.r@enterprise.com",
                    department = "DevOps / Engineering",
                    affectedSystem = "Cisco Catalyst 9300 (SW-FL3)",
                    assignedEngineer = "Marcus Vance (Senior NetEng)",
                    resolutionNotes = "SFP module swapped on GigabitEthernet1/0/24. Monitoring link error counters."
                ),
                TicketEntity(
                    ticketNumber = "INC-1029",
                    title = "Urgent: Phishing email campaign impersonating Payroll",
                    description = "Multiple employees received an email requesting emergency direct deposit verification via an external spoofed portal (hxxp://pay-login-portal.co).",
                    category = "Cybersecurity & Phishing",
                    priority = "P1",
                    status = "In Progress",
                    reporterName = "Samantha Miller",
                    reporterEmail = "smiller@enterprise.com",
                    department = "Human Resources",
                    affectedSystem = "Office 365 Exchange Defender",
                    assignedEngineer = "Elena Chen (SecOps)",
                    resolutionNotes = "Sender domain quarantined globally in Defender. URL blocked at Palo Alto firewall perimeter."
                ),
                TicketEntity(
                    ticketNumber = "INC-1024",
                    title = "Active Directory Account Lockout - Finance Controller",
                    description = "Finance manager locked out of workstation and ERP SAP application after password rotation last night. Cached credentials likely misfiring on mobile device.",
                    category = "IAM & Access",
                    priority = "P2",
                    status = "Open",
                    reporterName = "David Goldberg",
                    reporterEmail = "d.goldberg@enterprise.com",
                    department = "Finance & Accounting",
                    affectedSystem = "Azure AD / On-Prem AD Domain Controller",
                    assignedEngineer = "IT Help Desk L2",
                    resolutionNotes = ""
                ),
                TicketEntity(
                    ticketNumber = "INC-1028",
                    title = "BitLocker Recovery Pin Prompt After BIOS Update",
                    description = "Workstation prompted for 48-digit BitLocker key upon cold boot following Windows Update firmware patch. User unable to access local files.",
                    category = "Operating System",
                    priority = "P2",
                    status = "In Progress",
                    reporterName = "Chloe Bennett",
                    reporterEmail = "c.bennett@enterprise.com",
                    department = "Legal & Compliance",
                    affectedSystem = "Dell Latitude 7440 (Asset #AST-NB-104)",
                    assignedEngineer = "Marcus Vance",
                    resolutionNotes = "Retrieved recovery key ID 8493-2104 from Microsoft Intune portal. Escorting user through unlock."
                ),
                TicketEntity(
                    ticketNumber = "INC-1026",
                    title = "Exchange Online Mailbox Sync Error 0x8004010F",
                    description = "Outlook desktop client unable to download Offline Address Book (OAB). Webmail works fine.",
                    category = "Cloud & Servers",
                    priority = "P3",
                    status = "Open",
                    reporterName = "Robert Taylor",
                    reporterEmail = "r.taylor@enterprise.com",
                    department = "Sales Operations",
                    affectedSystem = "Microsoft 365 Exchange",
                    assignedEngineer = "IT Help Desk L1",
                    resolutionNotes = ""
                ),
                TicketEntity(
                    ticketNumber = "INC-1027",
                    title = "Floor 2 Marketing HP LaserJet Spooler Jammed",
                    description = "Documents queued for print are stuck in 'Sent to printer' status with print spooler service hanging on local print server.",
                    category = "Printers & Peripherals",
                    priority = "P3",
                    status = "Resolved",
                    reporterName = "Jessica Wu",
                    reporterEmail = "j.wu@enterprise.com",
                    department = "Marketing",
                    affectedSystem = "HP LaserJet Enterprise MFP M528",
                    assignedEngineer = "Marcus Vance",
                    resolutionNotes = "Purged corrupted .SPL/.SHD files in C:\\Windows\\System32\\spool\\PRINTERS and restarted Print Spooler service."
                )
            )
            ticketDao.insertAll(sampleTickets)

            val sampleAssets = listOf(
                AssetEntity(
                    assetTag = "AST-SRV-01",
                    hardwareModel = "Dell PowerEdge R750 (Cluster Host 1)",
                    category = "Server",
                    serialNumber = "7X89B2-S1",
                    assignedUser = "Data Center Infra Team",
                    department = "IT Infrastructure",
                    location = "HQ Server Room Rack A1",
                    status = "In Use",
                    ipAddress = "10.0.10.15",
                    macAddress = "D8:9D:67:12:4A:91",
                    specifications = "2x Intel Xeon Gold 6330, 256GB RAM, 8x 3.84TB SAS SSD",
                    warrantyExpiry = "2027-11-30"
                ),
                AssetEntity(
                    assetTag = "AST-SRV-02",
                    hardwareModel = "HPE ProLiant DL380 Gen10 (Backup SAN)",
                    category = "Server",
                    serialNumber = "HP99341-B",
                    assignedUser = "Backup & Recovery Ops",
                    department = "IT Infrastructure",
                    location = "HQ Server Room Rack A2",
                    status = "In Use",
                    ipAddress = "10.0.10.18",
                    macAddress = "9C:B6:54:33:11:80",
                    specifications = "128GB RAM, 12x 8TB SAS HDD RAID-6",
                    warrantyExpiry = "2026-08-15"
                ),
                AssetEntity(
                    assetTag = "AST-SW-01",
                    hardwareModel = "Cisco Catalyst 9300 48-Port PoE+",
                    category = "Switch",
                    serialNumber = "FOC22340A9",
                    assignedUser = "Core Network Engineering",
                    department = "Network Ops",
                    location = "Floor 2 IDF Cabinet",
                    status = "In Use",
                    ipAddress = "10.0.1.2",
                    macAddress = "00:1E:E5:A2:3F:01",
                    specifications = "48x 1GbE PoE+ (740W), 4x 10GbE SFP+ uplinks",
                    warrantyExpiry = "2028-05-20"
                ),
                AssetEntity(
                    assetTag = "AST-FW-01",
                    hardwareModel = "Palo Alto Networks PA-3220 NGFW",
                    category = "Firewall",
                    serialNumber = "01340029881",
                    assignedUser = "SecOps Perimeter Guard",
                    department = "Cybersecurity",
                    location = "HQ Gateway Rack B1",
                    status = "In Use",
                    ipAddress = "10.0.0.1",
                    macAddress = "08:66:98:50:88:FF",
                    specifications = "5.3 Gbps Threat Prevention throughput",
                    warrantyExpiry = "2028-01-10"
                ),
                AssetEntity(
                    assetTag = "AST-NB-104",
                    hardwareModel = "MacBook Pro 16\" Apple M3 Max",
                    category = "Laptop",
                    serialNumber = "C02FM3K8MD6R",
                    assignedUser = "Alex Rivera (Lead Architect)",
                    department = "DevOps / Engineering",
                    location = "Floor 4 - Tech Wing",
                    status = "In Use",
                    ipAddress = "10.0.20.114",
                    macAddress = "F4:D4:88:99:AA:02",
                    specifications = "Apple M3 Max 16-core, 64GB Unified RAM, 1TB SSD",
                    warrantyExpiry = "2027-02-14"
                ),
                AssetEntity(
                    assetTag = "AST-NB-205",
                    hardwareModel = "Lenovo ThinkPad P16 Gen 2",
                    category = "Laptop",
                    serialNumber = "PF49Z11A",
                    assignedUser = "IT Emergency Loaner Pool",
                    department = "IT Support",
                    location = "IT Help Desk Locker 3",
                    status = "Spare Inventory",
                    ipAddress = "10.0.20.142",
                    macAddress = "3C:E1:A1:7B:80:23",
                    specifications = "Intel Core i7-13850HX, 32GB RAM, RTX A2000, 1TB SSD",
                    warrantyExpiry = "2026-10-31"
                ),
                AssetEntity(
                    assetTag = "AST-WS-301",
                    hardwareModel = "Dell Precision 5820 Workstation",
                    category = "Workstation",
                    serialNumber = "8M29PQ3",
                    assignedUser = "Elena Chen (SecOps Analyst)",
                    department = "Security Operations",
                    location = "Floor 3 - SOC Room",
                    status = "In Use",
                    ipAddress = "10.0.30.55",
                    macAddress = "E4:54:E8:11:9C:24",
                    specifications = "Intel Xeon W-2245, 64GB ECC RAM, NVIDIA RTX 4000",
                    warrantyExpiry = "2027-06-30"
                ),
                AssetEntity(
                    assetTag = "AST-AP-102",
                    hardwareModel = "Cisco Catalyst 9120AX Wi-Fi 6 AP",
                    category = "Network",
                    serialNumber = "FOC24198BC",
                    assignedUser = "Corporate Wi-Fi Infrastructure",
                    department = "Network Ops",
                    location = "Floor 3 Ceiling Grid C",
                    status = "Under Maintenance",
                    ipAddress = "10.0.1.45",
                    macAddress = "70:81:05:43:D1:60",
                    specifications = "Wi-Fi 6 802.11ax 4x4 MU-MIMO, PoE+ powered",
                    warrantyExpiry = "2028-09-15"
                )
            )
            assetDao.insertAll(sampleAssets)
        }
    }
}
