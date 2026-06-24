package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Inspection
import com.example.data.entity.Toilet
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanerDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scannedToilet by viewModel.scannedToilet.collectAsStateWithLifecycle()
    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val toilets by viewModel.toilets.collectAsStateWithLifecycle()
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showIssueReporter by remember { mutableStateOf(false) }
    var inputQrCode by remember { mutableStateOf("") }

    // Cleaner summary
    val myName = currentUser?.name ?: "Selvam"
    val myCleanedToday = inspections.count {
        it.cleanerName.contains(myName.split(" ")[0]) &&
        it.createdAt > getTodayStartTimestamp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Welcome, ${myName.split(" ")[0]}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("School Cleaner Duty Terminal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats & shift banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("ACTIVE SHIFT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Main Block & Block A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("COMPLETED TODAY", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$myCleanedToday Toilets",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (scannedToilet == null) {
                    // QR Scanner View Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SimulatedQrScanner(
                            onScanSuccess = { qr ->
                                val found = viewModel.scanQrCode(qr)
                                if (found) {
                                    Toast.makeText(context, "QR code scanned successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No toilet mapped to QR code '$qr'", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Manual Entry Block
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Or Enter QR Code Manually:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = inputQrCode,
                                        onValueChange = { inputQrCode = it },
                                        placeholder = { Text("e.g. WM-BOYS-A-01") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("manual_qr_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (inputQrCode.isNotEmpty()) {
                                                val found = viewModel.scanQrCode(inputQrCode.trim())
                                                if (found) {
                                                    Toast.makeText(context, "Checking in...", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Invalid QR Code", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(54.dp)
                                            .testTag("manual_qr_submit")
                                    ) {
                                        Text("Submit")
                                    }
                                }
                            }
                        }

                        // Recent Logs
                        Text(
                            text = "My Shift Completion History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        val myLogs = inspections.filter { it.cleanerId == currentUser?.id }
                        if (myLogs.isEmpty()) {
                            Text(
                                "No cleaning records logged for your account yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            myLogs.take(5).forEach { log ->
                                val logToiletName = toilets.find { it.id == log.toiletId }?.toiletName ?: "Toilet #${log.toiletId}"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(logToiletName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(log.createdAt)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        StatusChip(status = log.overallStatus)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Check-in Forms Expanded View
                    val currentToilet = scannedToilet!!
                    val toiletSchool = schools.find { it.id == currentToilet.schoolId }?.schoolName ?: "Unknown School"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header active checkout toilet details card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("CHECKED IN TOILET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(currentToilet.toiletName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Text(toiletSchool, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    }
                                    IconButton(
                                        onClick = { viewModel.setScannedToilet(null) },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close checkin", tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SuggestionChip(onClick = {}, label = { Text(currentToilet.qrCode) })
                                    SuggestionChip(onClick = {}, label = { Text("${currentToilet.block} • Floor ${currentToilet.floor}") })
                                }
                            }
                        }

                        // Options Switch: Checklist vs Issue Reporter
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showIssueReporter = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!showIssueReporter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!showIssueReporter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checklist_tab")
                            ) {
                                Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checklist", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showIssueReporter = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (showIssueReporter) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (showIssueReporter) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("issue_tab")
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Report Issue", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!showIssueReporter) {
                            // checklist form
                            ChecklistForm(
                                toiletId = currentToilet.id,
                                onSubmit = { cleaned, water, soap, flush, bin, smell, floor, wall, overall, before, after, type ->
                                    viewModel.submitInspection(
                                        toiletId = currentToilet.id,
                                        cleaned = cleaned,
                                        waterAvailable = water,
                                        soapAvailable = soap,
                                        flushWorking = flush,
                                        dustbinEmptied = bin,
                                        badSmell = smell,
                                        floorCondition = floor,
                                        wallCondition = wall,
                                        overallStatus = overall,
                                        beforePhoto = before,
                                        afterPhoto = after,
                                        type = type
                                    ) {
                                        Toast.makeText(context, "$type Cleaning Record Logged!", Toast.LENGTH_SHORT).show()
                                        viewModel.setScannedToilet(null)
                                    }
                                }
                            )
                        } else {
                            // issue reporter
                            IssueReporterForm(
                                toiletId = currentToilet.id,
                                onSubmit = { cat, pri, desc, pic ->
                                    viewModel.submitIssue(
                                        toiletId = currentToilet.id,
                                        category = cat,
                                        priority = pri,
                                        description = desc,
                                        photo = pic
                                    ) {
                                        Toast.makeText(context, "Infrastructure Issue reported to headmaster!", Toast.LENGTH_LONG).show()
                                        viewModel.setScannedToilet(null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistForm(
    toiletId: Int,
    onSubmit: (Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, String, String, String, String?, String?, String) -> Unit
) {
    var inspectionType by remember { mutableStateOf("Daily") } // "Daily", "Weekly", "Monthly", "Yearly"

    // --- DAILY STATE ---
    var toiletCleaned by remember { mutableStateOf(true) }
    var waterAvailable by remember { mutableStateOf(true) }
    var soapAvailable by remember { mutableStateOf(true) }
    var flushWorking by remember { mutableStateOf(true) }
    var dustbinEmptied by remember { mutableStateOf(true) }
    var badSmellPresent by remember { mutableStateOf(false) }

    var floorCondition by remember { mutableStateOf("Good") } // "Good", "Average", "Poor"
    var wallCondition by remember { mutableStateOf("Good") }
    var mirrorCondition by remember { mutableStateOf("Good") }
    var doorLockCondition by remember { mutableStateOf("Good") }
    var lightingCondition by remember { mutableStateOf("Good") }

    // --- WEEKLY STATE ---
    var weeklyWaterSupply by remember { mutableStateOf(true) }
    var weeklyPipeLeakage by remember { mutableStateOf(false) }
    var weeklyFlushCondition by remember { mutableStateOf("Good") }
    var weeklyDoorsOk by remember { mutableStateOf(true) }
    var weeklyLightsOk by remember { mutableStateOf(true) }
    var weeklyVentilationOk by remember { mutableStateOf(true) }
    var weeklyDrainageOk by remember { mutableStateOf(true) }

    // --- MONTHLY STATE ---
    var monthlyPlumbing by remember { mutableStateOf("Good") }
    var monthlyTile by remember { mutableStateOf("Good") }
    var monthlyPaint by remember { mutableStateOf("Good") }
    var monthlyStructural by remember { mutableStateOf("Good") }
    var monthlyRemarks by remember { mutableStateOf("") }

    // --- YEARLY STATE ---
    var yearlyRenovationNeeded by remember { mutableStateOf(false) }
    var yearlyMajorRepairs by remember { mutableStateOf("") }
    var yearlyReplacementSuggestions by remember { mutableStateOf("") }

    // --- IMAGE STATE ---
    var beforePhotoUploaded by remember { mutableStateOf(false) }
    var afterPhotoUploaded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("checklist_form")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Frequency Selector Row (Daily, Weekly, Monthly, Yearly)
            Text(
                text = "Select Inspection Interval:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ScrollableTabRow(
                selectedTabIndex = when (inspectionType) {
                    "Daily" -> 0
                    "Weekly" -> 1
                    "Monthly" -> 2
                    else -> 3
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(selected = inspectionType == "Daily", onClick = { inspectionType = "Daily" }, text = { Text("Daily Cleaning", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) })
                Tab(selected = inspectionType == "Weekly", onClick = { inspectionType = "Weekly" }, text = { Text("Weekly Inspection", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) })
                Tab(selected = inspectionType == "Monthly", onClick = { inspectionType = "Monthly" }, text = { Text("Monthly Audit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) })
                Tab(selected = inspectionType == "Yearly", onClick = { inspectionType = "Yearly" }, text = { Text("Yearly Audit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) })
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            when (inspectionType) {
                "Daily" -> {
                    Text("Daily Cleaning Check-off Sheet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    ChecklistSwitchRow("Toilet bowls washed and cleaned?", toiletCleaned) { toiletCleaned = it }
                    ChecklistSwitchRow("Running water available in taps?", waterAvailable) { waterAvailable = it }
                    ChecklistSwitchRow("Soap/Handwash container stocked?", soapAvailable) { soapAvailable = it }
                    ChecklistSwitchRow("Flush tanks working normally?", flushWorking) { flushWorking = it }
                    ChecklistSwitchRow("Dustbin/Sanitary bins emptied?", dustbinEmptied) { dustbinEmptied = it }
                    ChecklistSwitchRow("Bad smell/Odour present?", badSmellPresent) { badSmellPresent = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Floor Condition Rating:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(floorCondition) { floorCondition = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Wall / Tile Cleanliness Rating:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(wallCondition) { wallCondition = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Mirror Cleanliness Rating:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(mirrorCondition) { mirrorCondition = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Door & Lock Condition Rating:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(doorLockCondition) { doorLockCondition = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lighting Condition Rating:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(lightingCondition) { lightingCondition = it }
                }

                "Weekly" -> {
                    Text("Weekly Infrastructure Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    ChecklistSwitchRow("Is Water Supply source stable?", weeklyWaterSupply) { weeklyWaterSupply = it }
                    ChecklistSwitchRow("Any active pipe/faucet leakage?", weeklyPipeLeakage) { weeklyPipeLeakage = it }
                    ChecklistSwitchRow("Doors and frames in working order?", weeklyDoorsOk) { weeklyDoorsOk = it }
                    ChecklistSwitchRow("All lighting points functional?", weeklyLightsOk) { weeklyLightsOk = it }
                    ChecklistSwitchRow("Exhaust fan / ventilation active?", weeklyVentilationOk) { weeklyVentilationOk = it }
                    ChecklistSwitchRow("Are the drain inlets free of blockage?", weeklyDrainageOk) { weeklyDrainageOk = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Flush Assembly Condition:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(weeklyFlushCondition) { weeklyFlushCondition = it }
                }

                "Monthly" -> {
                    Text("Monthly System Deep-Dive Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Text("Plumbing Assembly State:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(monthlyPlumbing) { monthlyPlumbing = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tile and Grouting Quality:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(monthlyTile) { monthlyTile = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Interior/Exterior Paint Condition:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(monthlyPaint) { monthlyPaint = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Overall Structural Condition:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    ConditionSegmentedChoice(monthlyStructural) { monthlyStructural = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = monthlyRemarks,
                        onValueChange = { monthlyRemarks = it },
                        label = { Text("Enter Monthly Audit Remarks / Action Items") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                "Yearly" -> {
                    Text("Yearly Strategic Capital Audit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    ChecklistSwitchRow("Complete Block Renovation required?", yearlyRenovationNeeded) { yearlyRenovationNeeded = it }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = yearlyMajorRepairs,
                        onValueChange = { yearlyMajorRepairs = it },
                        label = { Text("Log Major Repairs Needed") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = yearlyReplacementSuggestions,
                        onValueChange = { yearlyReplacementSuggestions = it },
                        label = { Text("Suggested Asset Replacements") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Photo Captures (Simulated)
            Text("Verification Photo Uploads (MANDATORY):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        beforePhotoUploaded = true
                        Toast.makeText(context, "Before photo captured successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (beforePhotoUploaded) Color(0xFFE0F2F1) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (beforePhotoUploaded) Color(0xFF00796B) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).testTag("before_photo_btn")
                ) {
                    Icon(imageVector = if (beforePhotoUploaded) Icons.Default.CheckCircle else Icons.Default.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (beforePhotoUploaded) "Before Photo" else "Take Before")
                }

                Button(
                    onClick = {
                        afterPhotoUploaded = true
                        Toast.makeText(context, "After photo captured successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (afterPhotoUploaded) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (afterPhotoUploaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).testTag("after_photo_btn")
                ) {
                    Icon(imageVector = if (afterPhotoUploaded) Icons.Default.CheckCircle else Icons.Default.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (afterPhotoUploaded) "After Photo" else "Take After")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Submit Button
            Button(
                onClick = {
                    if (!beforePhotoUploaded || !afterPhotoUploaded) {
                        Toast.makeText(context, "Please capture verification photos first", Toast.LENGTH_LONG).show()
                    } else {
                        // Compute overall state depending on selected inspection frequency
                        var finalCleaned = true
                        var finalWater = true
                        var finalSoap = true
                        var finalFlush = true
                        var finalBin = true
                        var finalSmell = false
                        var finalFloor = "Good"
                        var finalWall = "Good"
                        var finalOverall = "Clean"

                        when (inspectionType) {
                            "Daily" -> {
                                finalCleaned = toiletCleaned
                                finalWater = waterAvailable
                                finalSoap = soapAvailable
                                finalFlush = flushWorking
                                finalBin = dustbinEmptied
                                finalSmell = badSmellPresent
                                finalFloor = floorCondition
                                finalWall = wallCondition

                                finalOverall = if (!toiletCleaned || badSmellPresent || floorCondition == "Poor" || wallCondition == "Poor" || mirrorCondition == "Poor") {
                                    "Critical"
                                } else if (floorCondition == "Average" || wallCondition == "Average" || !soapAvailable) {
                                    "Need Attention"
                                } else {
                                    "Clean"
                                }
                            }

                            "Weekly" -> {
                                finalCleaned = true
                                finalWater = weeklyWaterSupply
                                finalPipeLeakageCheck(weeklyPipeLeakage)
                                finalSoap = true
                                finalFlush = weeklyFlushCondition == "Good"
                                finalBin = true
                                finalSmell = weeklyPipeLeakage
                                finalFloor = weeklyFlushCondition
                                finalWall = "Good"

                                finalOverall = if (weeklyPipeLeakage || !weeklyWaterSupply || weeklyFlushCondition == "Poor" || !weeklyDrainageOk) {
                                    "Critical"
                                } else if (weeklyFlushCondition == "Average" || !weeklyLightsOk || !weeklyVentilationOk) {
                                    "Need Attention"
                                } else {
                                    "Clean"
                                }
                            }

                            "Monthly" -> {
                                finalCleaned = true
                                finalWater = true
                                finalSoap = true
                                finalFlush = true
                                finalBin = true
                                finalSmell = false
                                finalFloor = monthlyPlumbing
                                finalWall = monthlyTile

                                finalOverall = if (monthlyPlumbing == "Poor" || monthlyStructural == "Poor") {
                                    "Critical"
                                } else if (monthlyTile == "Average" || monthlyPaint == "Average") {
                                    "Need Attention"
                                } else {
                                    "Clean"
                                }
                            }

                            "Yearly" -> {
                                finalCleaned = true
                                finalWater = true
                                finalSoap = true
                                finalFlush = true
                                finalBin = true
                                finalSmell = false
                                finalFloor = "Good"
                                finalWall = "Good"

                                finalOverall = if (yearlyRenovationNeeded) "Critical" else "Clean"
                            }
                        }

                        onSubmit(
                            finalCleaned,
                            finalWater,
                            finalSoap,
                            finalFlush,
                            finalBin,
                            finalSmell,
                            finalFloor,
                            finalWall,
                            finalOverall,
                            "simulated_before_photo_${inspectionType.lowercase()}",
                            "simulated_after_photo_${inspectionType.lowercase()}",
                            inspectionType
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_checklist_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit $inspectionType Verification Card", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun finalPipeLeakageCheck(leakage: Boolean) {}

@Composable
fun ChecklistSwitchRow(
    question: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun ConditionSegmentedChoice(
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Good", "Average", "Poor").forEach { choice ->
            val isSelected = selected == choice
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            when (choice) {
                                "Good" -> Color(0xFFE8F5E9)
                                "Average" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            }
                        } else Color.Transparent
                    )
                    .clickable { onSelectedChange(choice) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = choice,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        when (choice) {
                            "Good" -> Color(0xFF2E7D32)
                            "Average" -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        }
                    } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun IssueReporterForm(
    toiletId: Int,
    onSubmit: (String, String, String, String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("No Water") }
    var selectedPriority by remember { mutableStateOf("High") }
    var photoUploaded by remember { mutableStateOf(false) }

    val categories = listOf("No Water", "Broken Tap", "Flush Problem", "Drain Blockage", "Light Failure", "Door Lock Damage", "Bad Smell", "Broken Tiles", "Water Leakage")
    val priorities = listOf("Low", "Medium", "High", "Critical")

    var categoryExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("issue_form")
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Report Sanitary/Plumbing Issue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            // Category Selection Dropdown
            Text("Issue Category:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Box {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true },
                    shape = RoundedCornerShape(10.dp)
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = {
                            selectedCategory = cat
                            categoryExpanded = false
                        })
                    }
                }
            }

            // Priority Segment Selection
            Text("Repair Urgency Priority:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                priorities.forEach { pri ->
                    val isSel = selectedPriority == pri
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) {
                                    when (pri) {
                                        "Low" -> Color(0xFFE3F2FD)
                                        "Medium" -> Color(0xFFFFF3E0)
                                        "High" -> Color(0xFFFFE0B2)
                                        else -> Color(0xFFFFEBEE)
                                    }
                                } else Color.Transparent
                            )
                            .clickable { selectedPriority = pri }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pri,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) {
                                when (pri) {
                                    "Low" -> Color(0xFF1565C0)
                                    "Medium" -> Color(0xFFE65100)
                                    "High" -> Color(0xFFD84315)
                                    else -> Color(0xFFC62828)
                                }
                            } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Issue description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe the defect/issue in detail...") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("issue_desc_input"),
                shape = RoundedCornerShape(10.dp)
            )

            // Issue Photo Upload (Simulated)
            Button(
                onClick = {
                    photoUploaded = true
                    Toast.makeText(context, "Defect photo attached successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (photoUploaded) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (photoUploaded) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().testTag("issue_photo_btn")
            ) {
                Icon(imageVector = if (photoUploaded) Icons.Default.CheckCircle else Icons.Default.AttachFile, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (photoUploaded) "Defect Photo Attached" else "Attach Photo of Defect")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Issue
            Button(
                onClick = {
                    if (description.isEmpty()) {
                        Toast.makeText(context, "Please write a brief description of the issue.", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmit(
                            selectedCategory,
                            selectedPriority,
                            description,
                            "cleaner_captured_defect_photo"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_issue_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Report Defect to Headmaster", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Utility timestamp helper
fun getTodayStartTimestamp(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
