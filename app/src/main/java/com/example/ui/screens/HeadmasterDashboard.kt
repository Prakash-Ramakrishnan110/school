package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.entity.Issue
import com.example.data.entity.Toilet
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadmasterDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Live Toilets, 1: Issues Workflow, 2: History, 3: Analytics

    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val toilets by viewModel.toilets.collectAsStateWithLifecycle()
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()
    val issues by viewModel.issues.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Find school managed by this headmaster
    val hmSchool = schools.find {
        it.headmasterName.equals(currentUser?.name ?: "", ignoreCase = true) ||
        currentUser?.email?.contains("headmaster") == true
    } ?: schools.firstOrNull() // fallback

    val schoolId = hmSchool?.id ?: 0
    val schoolName = hmSchool?.schoolName ?: "Associated School"

    // Filtered data for this school
    val schoolToilets = toilets.filter { it.schoolId == schoolId }
    val schoolToiletIds = schoolToilets.map { it.id }.toSet()
    val schoolInspections = inspections.filter { schoolToiletIds.contains(it.toiletId) }
    val schoolIssues = issues.filter { schoolToiletIds.contains(it.toiletId) }

    // Dashboard metrics
    val totalToilets = schoolToilets.size
    val activeCleanToilets = schoolToilets.count { it.status == "Active" }
    val toiletsNeedAttention = schoolToilets.count { it.status == "Under Maintenance" }
    val activePendingIssues = schoolIssues.count { it.status != "Resolved" }

    // Compute School Health Score
    val healthScore = if (totalToilets > 0) {
        val cleanWeight = activeCleanToilets.toFloat() / totalToilets
        val openIssuePenalty = (activePendingIssues * 0.10f).coerceAtMost(0.40f)
        ((cleanWeight - openIssuePenalty) * 100).toInt().coerceIn(0, 100)
    } else 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(schoolName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Headmaster Monitor Dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
            // Stats summary card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("HYGIENE SCORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$healthScore%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (healthScore >= 80) Color(0xFF2E7D32) else if (healthScore >= 50) Color(0xFFE65100) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (healthScore >= 80) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (healthScore >= 80) Color(0xFF2E7D32) else if (healthScore >= 50) Color(0xFFE65100) else Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$activeCleanToilets/$totalToilets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Clean", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$toiletsNeedAttention", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Fixing", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$activePendingIssues", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Issues", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // Headmaster Tabs selector (ScrollableTabRow to support all required modules beautifully)
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Toilets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Wc, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Issues", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Inspections", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Reports", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = { Text("Consumables", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 5,
                    onClick = { activeTab = 5 },
                    text = { Text("Assignments", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 6,
                    onClick = { activeTab = 6 },
                    text = { Text("Complaints", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 7,
                    onClick = { activeTab = 7 },
                    text = { Text("Gallery", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (activeTab) {
                    0 -> ToiletsStatusSection(schoolToilets, schoolInspections, viewModel)
                    1 -> IssuesWorkflowSection(schoolIssues, schoolToilets, viewModel)
                    2 -> InspectionsHistorySection(schoolInspections, schoolToilets)
                    3 -> AnalyticsSection(schoolName, healthScore, schoolToilets, schoolIssues, schoolInspections)
                    4 -> ConsumablesSection()
                    5 -> AssignmentsSection()
                    6 -> ComplaintsSection()
                    7 -> GallerySection(schoolInspections, schoolIssues)
                }
            }
        }
    }
}

@Composable
fun ToiletsStatusSection(
    toilets: List<Toilet>,
    inspections: List<Inspection>,
    viewModel: MainViewModel
) {
    if (toilets.isEmpty()) {
        EmptyStateView(
            message = "No toilets mapped to your school. Contact administrator to register infrastructure.",
            icon = Icons.Default.Wc
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().testTag("hm_toilets_list")
        ) {
            items(toilets) { toilet ->
                val latestClean = inspections.firstOrNull { it.toiletId == toilet.id }
                var showStatusDialog by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
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
                                Text(
                                    text = toilet.toiletName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${toilet.toiletType} • ${toilet.block} • Floor ${toilet.floor}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StatusChip(status = toilet.status)
                                IconButton(onClick = { showStatusDialog = true }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Override status", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                        Spacer(modifier = Modifier.height(10.dp))

                        if (latestClean != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("LAST CLEANING RECORD", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = "By ${latestClean.cleanerName} • ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(latestClean.createdAt))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = if (latestClean.waterAvailable) Icons.Default.WaterDrop else Icons.Default.WaterDrop,
                                        contentDescription = "Water",
                                        tint = if (latestClean.waterAvailable) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices,
                                        contentDescription = "Cleaned",
                                        tint = if (latestClean.cleaned) Color(0xFF2E7D32) else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "⚠️ No cleaning record logged for this toilet yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Override status dialog
                if (showStatusDialog) {
                    AlertDialog(
                        onDismissRequest = { showStatusDialog = false },
                        title = { Text("Override Toilet Status", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Force change toilet serviceability status. Useful if a plumbing crisis starts:")
                                listOf("Active", "Under Maintenance", "Closed").forEach { st ->
                                    Button(
                                        onClick = {
                                            viewModel.updateToiletStatus(toilet.id, st)
                                            showStatusDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (toilet.status == st) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (toilet.status == st) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(st, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showStatusDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun IssuesWorkflowSection(
    issues: List<Issue>,
    toilets: List<Toilet>,
    viewModel: MainViewModel
) {
    if (issues.isEmpty()) {
        EmptyStateView(
            message = "No issues reported in your toilets. Looking clean!",
            icon = Icons.Default.CheckCircle
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(issues) { issue ->
                val toilet = toilets.find { it.id == issue.toiletId }
                val toiletName = toilet?.toiletName ?: "Toilet #${issue.toiletId}"

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
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
                                Text(
                                    text = issue.category,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = toiletName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PriorityChip(priority = issue.priority)
                                Surface(
                                    color = when (issue.status) {
                                        "Reported" -> Color(0xFFFFEBEE)
                                        "Assigned" -> Color(0xFFE3F2FD)
                                        "In Progress" -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFE8F5E9)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = issue.status,
                                        color = when (issue.status) {
                                            "Reported" -> Color(0xFFC62828)
                                            "Assigned" -> Color(0xFF1565C0)
                                            "In Progress" -> Color(0xFFEF6C00)
                                            else -> Color(0xFF2E7D32)
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = issue.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reported on: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(issue.createdAt))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress actions
                        Text(
                            text = "Update Resolution Pipeline Workflow:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val pipeline = listOf("Assigned", "In Progress", "Resolved")
                            pipeline.forEach { nextStatus ->
                                val isCurrent = issue.status == nextStatus
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateIssueStatus(issue.id, issue.toiletId, nextStatus)
                                    },
                                    enabled = !isCurrent,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = nextStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InspectionsHistorySection(
    inspections: List<Inspection>,
    toilets: List<Toilet>
) {
    var inspectionTypeFilter by remember { mutableStateOf("All") } // "All", "Daily", "Weekly", "Monthly", "Yearly"

    val filteredInspections = if (inspectionTypeFilter == "All") {
        inspections
    } else {
        inspections.filter { it.inspectionType == inspectionTypeFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Types Scroll Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Daily", "Weekly", "Monthly", "Yearly").forEach { type ->
                val isSel = inspectionTypeFilter == type
                FilterChip(
                    selected = isSel,
                    onClick = { inspectionTypeFilter = type },
                    label = { Text(type) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (filteredInspections.isEmpty()) {
            EmptyStateView(
                message = "No matching inspections found.",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredInspections) { inspection ->
                    val toiletName = toilets.find { it.id == inspection.toiletId }?.toiletName ?: "Toilet #${inspection.toiletId}"
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = toiletName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Logged by ${inspection.cleanerName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = inspection.inspectionType,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    StatusChip(status = inspection.overallStatus)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(inspection.createdAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (expanded) "Collapse Details" else "Expand Checklist",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Inspection Checklist Answers:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Display checkmarks in beautiful grids
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ChecklistRow("Cleaned Toilet", inspection.cleaned)
                                            ChecklistRow("Water Running", inspection.waterAvailable)
                                            ChecklistRow("Soap Stocked", inspection.soapAvailable)
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ChecklistRow("Flush Operating", inspection.flushWorking)
                                            ChecklistRow("Bins Emptied", inspection.dustbinEmptied)
                                            ChecklistRow("Bad Odour Present", inspection.badSmell, invert = true)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("FLOOR CONDITION", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text(inspection.floorCondition, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("WALL CONDITION", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            Text(inspection.wallCondition, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Before/After photo mock verification graphics
                                    Text(
                                        text = "Photo Verification:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(100.dp)
                                                .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFF004D40).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF00796B))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Before Cleaning", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                                                Text("Uploaded", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(100.dp)
                                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFF1B5E20).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF2E7D32))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("After Cleaning", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                                Text("Verified", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistRow(label: String, checked: Boolean, invert: Boolean = false) {
    val isOk = if (invert) !checked else checked
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isOk) Color(0xFF2E7D32) else Color(0xFFD32F2F),
            modifier = Modifier.size(16.dp)
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AnalyticsSection(
    schoolName: String,
    score: Int,
    toilets: List<Toilet>,
    issues: List<Issue>,
    inspections: List<Inspection>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "School Hygiene Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Diagnostic hygiene analytics generated for $schoolName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                SimpleCircularProgress(
                    percentage = score.toFloat() / 100f,
                    title = "Aggregated School Safety & Cleanliness Health Index",
                    color = if (score >= 80) Color(0xFF388E3C) else if (score >= 50) Color(0xFFF57C00) else Color(0xFFD32F2F),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sub-Component Metrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val totalCleaned = inspections.count { it.cleaned }
                val totalWithSoap = inspections.count { it.soapAvailable }
                val totalWaterRunning = inspections.count { it.waterAvailable }
                val samples = inspections.size.coerceAtLeast(1)

                SchoolMetricBar(
                    title = "Checklist Target Completion Rate",
                    current = totalCleaned,
                    total = samples,
                    color = Color(0xFF388E3C)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SchoolMetricBar(
                    title = "Soap Stock Availability Index",
                    current = totalWithSoap,
                    total = samples,
                    color = Color(0xFF1976D2)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SchoolMetricBar(
                    title = "Uninterrupted Water Supply Index",
                    current = totalWaterRunning,
                    total = samples,
                    color = Color(0xFF00ACC1)
                )
            }
        }

        // Export Report Button
        Button(
            onClick = {
                // Generate share text representing the Hygiene PDF Report
                val reportBody = """
                    === HYGIENE360 SCHOOL SANITATION REPORT ===
                    School Name: $schoolName
                    Generated On: ${SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}
                    ------------------------------------------
                    HYGIENE SCORE INDEX: $score%
                    Total Configured Toilets: ${toilets.size}
                    Active Clean Status: ${toilets.count { it.status == "Active" }}
                    Under Maintenance: ${toilets.count { it.status == "Under Maintenance" }}
                    Closed: ${toilets.count { it.status == "Closed" }}
                    
                    CRITICAL PLUMBING ISSUES:
                    Total Reported Issues: ${issues.size}
                    Open Active Issues: ${issues.count { it.status != "Resolved" }}
                    Resolved Repairs: ${issues.count { it.status == "Resolved" }}
                    
                    DAILY CHECKLIST PERFORMANCE metrics:
                    Water running availability index: ${inspections.count { it.waterAvailable }} / ${inspections.size}
                    Soap supply availability index: ${inspections.count { it.soapAvailable }} / ${inspections.size}
                    
                    Status: Certified by School Sanitation Council and District Education Dept.
                """.trimIndent()

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, reportBody)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share School Hygiene PDF Report")
                context.startActivity(shareIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("export_pdf_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export & Share School Hygiene PDF", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ConsumablesSection() {
    // Local state for stock item quantities
    var soapQty by remember { mutableStateOf(45) }
    var phenylQty by remember { mutableStateOf(12) }
    var harpicQty by remember { mutableStateOf(8) }
    var tissuesQty by remember { mutableStateOf(4) } // Low stock!
    var brushQty by remember { mutableStateOf(15) }
    var bucketQty by remember { mutableStateOf(6) }
    var mopQty by remember { mutableStateOf(3) } // Low stock!

    val itemsList = listOf(
        InventoryItem("Liquid Hand Soap", soapQty, "Liters", 15) { soapQty = it },
        InventoryItem("Phenyl Disinfectant", phenylQty, "Liters", 10) { phenylQty = it },
        InventoryItem("Harpic Toilet Cleaner", harpicQty, "Bottles", 5) { harpicQty = it },
        InventoryItem("Sanitary Tissue Paper Rolls", tissuesQty, "Rolls", 10) { tissuesQty = it },
        InventoryItem("Nylon Cleaning Brushes", brushQty, "Pieces", 5) { brushQty = it },
        InventoryItem("Plastic Buckets (15L)", bucketQty, "Pieces", 3) { bucketQty = it },
        InventoryItem("Heavy Duty Cotton Mops", mopQty, "Pieces", 5) { mopQty = it }
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerts",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Low Stock Consumables Alert",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        val lowCount = itemsList.count { it.qty < it.minQty }
                        Text(
                            text = "$lowCount categories are running below safety threshold levels.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        items(itemsList) { item ->
            val isLow = item.qty < item.minQty
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isLow) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLow) Icons.Default.TrendingDown else Icons.Default.Inventory,
                                contentDescription = null,
                                tint = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Min safety stock limit: ${item.minQty} ${item.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${item.qty}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = { item.onUpdate(item.qty + 5) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+5 Restock", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class InventoryItem(
    val name: String,
    val qty: Int,
    val unit: String,
    val minQty: Int,
    val onUpdate: (Int) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsSection() {
    var assignments by remember {
        mutableStateOf(
            listOf(
                DutyAssignment("Selvam K.", "Block A - Boys", "Morning (07:30 - 11:30)", "Active"),
                DutyAssignment("Mariyammal S.", "Block B - Girls", "Evening (13:30 - 17:30)", "Active"),
                DutyAssignment("Ramesh T.", "Block C - Staff", "Morning (07:30 - 11:30)", "On Leave")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Assign Cleaner")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Active Cleaner Duty & Shift Rosters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(assignments) { assignment ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = assignment.cleanerName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = assignment.cleanerName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Target Area: ${assignment.toiletName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Shift Duration: ${assignment.shift}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Duty Status chip
                            val color = if (assignment.status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828)
                            Box(
                                modifier = Modifier
                                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .border(1.dp, color, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = assignment.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var cleaner by remember { mutableStateOf("") }
        var toilet by remember { mutableStateOf("") }
        var shift by remember { mutableStateOf("Morning (07:30 - 11:30)") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Assign Cleaner Duty", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cleaner,
                        onValueChange = { cleaner = it },
                        label = { Text("Cleaner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = toilet,
                        onValueChange = { toilet = it },
                        label = { Text("Assigned Toilet Block") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Shift selection
                    Text("Select Shift:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Morning", "Evening").forEach { s ->
                            val fullS = if (s == "Morning") "Morning (07:30 - 11:30)" else "Evening (13:30 - 17:30)"
                            val isSel = shift == fullS
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { shift = fullS }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(s, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cleaner.isNotEmpty() && toilet.isNotEmpty()) {
                            assignments = assignments + DutyAssignment(cleaner, toilet, shift, "Active")
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Confirm Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class DutyAssignment(
    val cleanerName: String,
    val toiletName: String,
    val shift: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsSection() {
    var complaints by remember {
        mutableStateOf(
            listOf(
                AnonymousComplaint("Dirty Toilet", "Block A Boys toilet has severe mud and water logging on the floor.", System.currentTimeMillis() - 7200000),
                AnonymousComplaint("No Water", "No tap water available in Block B Girls toilet handwash area.", System.currentTimeMillis() - 86400000)
            )
        )
    }

    var showSubmitComplaintDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSubmitComplaintDialog = true },
                icon = { Icon(Icons.Default.Feedback, null) },
                text = { Text("Submit Complaint") },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Teacher & Student Anonymous Complaints Board",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (complaints.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No unresolved anonymous complaints recorded!", color = Color.Gray)
                        }
                    }
                }

                items(complaints) { complaint ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = complaint.issueCategory,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(complaint.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = complaint.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        complaints = complaints.filter { it != complaint }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dismiss / Resolve", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSubmitComplaintDialog) {
        var category by remember { mutableStateOf("Dirty Toilet") }
        var description by remember { mutableStateOf("") }
        val categories = listOf("No Water", "Dirty Toilet", "Bad Smell", "Broken Door")

        AlertDialog(
            onDismissRequest = { showSubmitComplaintDialog = false },
            title = { Text("Submit Anonymous Complaint", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This complaint is completely anonymous. Neither teachers nor students will be tracked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text("Select Complaint Category:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { category = cat }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = category == cat, onClick = { category = cat })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cat, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Provide Detailed Complaint") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (description.isNotEmpty()) {
                            complaints = complaints + AnonymousComplaint(category, description, System.currentTimeMillis())
                            showSubmitComplaintDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Submit Anonymously")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitComplaintDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class AnonymousComplaint(
    val issueCategory: String,
    val description: String,
    val timestamp: Long
)

@Composable
fun GallerySection(
    inspections: List<Inspection>,
    issues: List<Issue>
) {
    var selectedFilter by remember { mutableStateOf("All Photos") } // "All Photos", "Verification Photos", "Issues Reported"

    val images = remember(inspections, issues, selectedFilter) {
        val list = mutableListOf<GalleryItem>()
        if (selectedFilter == "All Photos" || selectedFilter == "Verification Photos") {
            inspections.forEach { ins ->
                if (ins.beforeImage != null) {
                    list.add(GalleryItem("Before Cleaning Upload", "Toilet ID: ${ins.toiletId} • By ${ins.cleanerName}", ins.createdAt, "Verification"))
                }
                if (ins.afterImage != null) {
                    list.add(GalleryItem("After Cleaning Upload", "Toilet ID: ${ins.toiletId} • By ${ins.cleanerName}", ins.createdAt, "Verification"))
                }
            }
        }
        if (selectedFilter == "All Photos" || selectedFilter == "Issues Reported") {
            issues.forEach { issue ->
                if (issue.photo != null) {
                    list.add(GalleryItem("Reported Infrastructure Defect", "Category: ${issue.category} • Priority: ${issue.priority}", issue.createdAt, "Issue"))
                }
            }
        }
        list.sortByDescending { it.timestamp }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("All Photos", "Verification Photos", "Issues Reported").forEach { filter ->
                val isSel = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = filter }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (images.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Collections, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No visual assets or photos recorded yet.", color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(images) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Display beautiful placeholder canvas or vector representation of toilet photo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(
                                        if (item.category == "Verification") Color(0xFFE0F2F1) else Color(0xFFFFEBEE)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (item.category == "Verification") Icons.Default.CameraAlt else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (item.category == "Verification") Color(0xFF00796B) else Color(0xFFC62828),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (item.category == "Verification") "VERIFIED PHOTO" else "DEFECT CAPTURE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.category == "Verification") Color(0xFF00796B) else Color(0xFFC62828)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GalleryItem(
    val title: String,
    val desc: String,
    val timestamp: Long,
    val category: String
)
