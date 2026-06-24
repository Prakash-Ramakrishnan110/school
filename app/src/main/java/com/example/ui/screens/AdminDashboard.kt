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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.School
import com.example.data.entity.Toilet
import com.example.data.entity.User
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Schools, 1: Toilets, 2: Users

    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val toilets by viewModel.toilets.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    var showAddSchoolDialog by remember { mutableStateOf(false) }
    var showAddToiletDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hygiene360 Admin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("District & Infrastructure Control", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeTab) {
                        0 -> showAddSchoolDialog = true
                        1 -> showAddToiletDialog = true
                        2 -> showAddUserDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("admin_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row Navigation
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Schools (${schools.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Toilets (${toilets.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Wc, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Users (${users.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
            }

            // Quick Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Schools",
                    value = schools.size.toString(),
                    icon = Icons.Default.School,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Toilets",
                    value = toilets.size.toString(),
                    icon = Icons.Default.Wc,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Beautiful Search Bar (Search Module requirement!)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search schools, toilets, or staff users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("admin_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Tabs Content
            val filteredSchools = schools.filter {
                it.schoolName.contains(searchQuery, ignoreCase = true) ||
                it.district.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
            }
            val filteredToilets = toilets.filter {
                it.toiletName.contains(searchQuery, ignoreCase = true) ||
                it.block.contains(searchQuery, ignoreCase = true) ||
                it.toiletType.contains(searchQuery, ignoreCase = true)
            }
            val filteredUsers = users.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (activeTab) {
                    0 -> SchoolsTab(schools = filteredSchools, onDeleteSchool = { viewModel.deleteSchool(it) })
                    1 -> ToiletsTab(toilets = filteredToilets, schools = schools, onDeleteToilet = { viewModel.deleteToilet(it) })
                    2 -> UsersTab(users = filteredUsers)
                }
            }
        }
    }

    // dialogs
    if (showAddSchoolDialog) {
        AddSchoolDialog(
            onDismiss = { showAddSchoolDialog = false },
            onConfirm = { name, dist, addr, hm, ph ->
                viewModel.addSchool(name, dist, addr, hm, ph)
                showAddSchoolDialog = false
            }
        )
    }

    if (showAddToiletDialog) {
        AddToiletDialog(
            schools = schools,
            onDismiss = { showAddToiletDialog = false },
            onConfirm = { schoolId, name, type, floor, block, qrCode ->
                viewModel.addToilet(schoolId, name, type, floor, block, qrCode)
                showAddToiletDialog = false
            }
        )
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, email, phone, pass, role ->
                viewModel.registerUser(name, email, pass, phone, role) { _, _ -> }
                showAddUserDialog = false
            }
        )
    }
}

@Composable
fun SchoolsTab(
    schools: List<School>,
    onDeleteSchool: (School) -> Unit
) {
    if (schools.isEmpty()) {
        EmptyStateView(
            message = "No registered schools. Tap '+' to register your first school district.",
            icon = Icons.Default.School
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().testTag("schools_list")
        ) {
            items(schools) { school ->
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
                            Text(
                                text = school.schoolName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { onDeleteSchool(school) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete School", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, size16Modifier, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${school.address}, ${school.district}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("HEADMASTER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(school.headmasterName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PHONE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(school.phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToiletsTab(
    toilets: List<Toilet>,
    schools: List<School>,
    onDeleteToilet: (Toilet) -> Unit
) {
    var selectedSchoolId by remember { mutableStateOf<Int?>(null) }
    var showSchoolDropdown by remember { mutableStateOf(false) }

    val filteredToilets = if (selectedSchoolId == null) {
        toilets
    } else {
        toilets.filter { it.schoolId == selectedSchoolId }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // School Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .clickable { showSchoolDropdown = true }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val selectedSchoolName = schools.find { it.id == selectedSchoolId }?.schoolName ?: "All Schools"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(selectedSchoolName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)

            DropdownMenu(
                expanded = showSchoolDropdown,
                onDismissRequest = { showSchoolDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Schools") },
                    onClick = {
                        selectedSchoolId = null
                        showSchoolDropdown = false
                    }
                )
                schools.forEach { school ->
                    DropdownMenuItem(
                        text = { Text(school.schoolName) },
                        onClick = {
                            selectedSchoolId = school.id
                            showSchoolDropdown = false
                        }
                    )
                }
            }
        }

        if (filteredToilets.isEmpty()) {
            EmptyStateView(
                message = "No toilets registered in this selection. Tap '+' to configure.",
                icon = Icons.Default.Wc
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().testTag("toilets_list")
            ) {
                items(filteredToilets) { toilet ->
                    val schoolName = schools.find { it.id == toilet.schoolId }?.schoolName ?: "Unknown School"
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
                                        text = schoolName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusChip(status = toilet.status)
                                    IconButton(onClick = { onDeleteToilet(toilet) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Toilet", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(toilet.toiletType) },
                                        icon = { Icon(Icons.Default.Wc, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text("${toilet.block} • Floor ${toilet.floor}") }
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = toilet.qrCode,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
}

@Composable
fun UsersTab(
    users: List<User>
) {
    if (users.isEmpty()) {
        EmptyStateView(
            message = "No other users registered. Users added via auth will show here.",
            icon = Icons.Default.People
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(users) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp),
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
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (user.role == "Admin") Icons.Default.AdminPanelSettings else if (user.role == "Headmaster") Icons.Default.SupervisorAccount else Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = user.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(text = "${user.email} • ${user.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        Surface(
                            color = when (user.role) {
                                "Admin" -> Color(0xFFE8EAF6)
                                "Headmaster" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE0F2F1)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = user.role,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (user.role) {
                                    "Admin" -> Color(0xFF3F51B5)
                                    "Headmaster" -> Color(0xFFE65100)
                                    else -> Color(0xFF00796B)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog views
@Composable
fun AddSchoolDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var schoolName by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var hmName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New School", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = schoolName, onValueChange = { schoolName = it }, label = { Text("School Name") }, modifier = Modifier.testTag("school_name_field"))
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") })
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Address") })
                OutlinedTextField(value = hmName, onValueChange = { hmName = it }, label = { Text("Headmaster Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (schoolName.isEmpty() || district.isEmpty() || address.isEmpty() || hmName.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(schoolName, district, address, hmName, phone)
                    }
                },
                modifier = Modifier.testTag("school_confirm")
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddToiletDialog(
    schools: List<School>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String, Int, String, String) -> Unit
) {
    var selectedSchoolIndex by remember { mutableStateOf(0) }
    var toiletName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Girls Toilet") }
    var selectedFloor by remember { mutableStateOf(0) }
    var blockName by remember { mutableStateOf("Block A") }
    var customQr by remember { mutableStateOf("") }

    var expandedSchools by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    val toiletTypes = listOf("Boys Toilet", "Girls Toilet", "Staff Toilet", "Accessible Toilet")
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Toilet Infrastructure", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (schools.isEmpty()) {
                    Text("Register a school first before adding toilets.", color = MaterialTheme.colorScheme.error)
                } else {
                    // School dropdown
                    Text("Select School:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedTextField(
                            value = schools[selectedSchoolIndex].schoolName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedSchools = true }
                        )
                        DropdownMenu(expanded = expandedSchools, onDismissRequest = { expandedSchools = false }) {
                            schools.forEachIndexed { idx, school ->
                                DropdownMenuItem(text = { Text(school.schoolName) }, onClick = {
                                    selectedSchoolIndex = idx
                                    expandedSchools = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = toiletName, onValueChange = { toiletName = it }, label = { Text("Toilet Name (e.g. Block A Floor 1)") }, modifier = Modifier.testTag("toilet_name_field"))

                    // Type dropdown
                    Text("Toilet Category Type:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedType = true }
                        )
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            toiletTypes.forEach { type ->
                                DropdownMenuItem(text = { Text(type) }, onClick = {
                                    selectedType = type
                                    expandedType = false

                                    // Auto generate QR code prefix suggestion
                                    val prefix = when (type) {
                                        "Boys Toilet" -> "WM-BOYS"
                                        "Girls Toilet" -> "WM-GIRLS"
                                        "Staff Toilet" -> "WM-STAFF"
                                        else -> "WM-ACC"
                                    }
                                    customQr = "$prefix-${blockName.lastOrNull() ?: 'A'}-0${(1..9).random()}"
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = blockName, onValueChange = { blockName = it }, label = { Text("Block Name (e.g. Block B)") })

                    // Floor Selector Row
                    Text("Floor Number:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 1, 2, 3).forEach { floorNum ->
                            val isSel = selectedFloor == floorNum
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { selectedFloor = floorNum },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (floorNum == 0) "GF" else "F$floorNum",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customQr,
                        onValueChange = { customQr = it },
                        label = { Text("Assigned QR Code (e.g. WM-BOYS-A-01)") },
                        modifier = Modifier.testTag("qr_field")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (schools.isNotEmpty() && toiletName.isNotEmpty() && customQr.isNotEmpty()) {
                        onConfirm(schools[selectedSchoolIndex].id, toiletName, selectedType, selectedFloor, blockName, customQr.trim())
                    } else {
                        Toast.makeText(context, "Please fill in all toilet infrastructure fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.testTag("toilet_confirm")
            ) {
                Text("Map Asset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Cleaner") }

    var expandedRole by remember { mutableStateOf(false) }
    val roles = listOf("Cleaner", "Headmaster", "Admin")

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register District User Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Temporary Password") })

                Text("System Access Role Role:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedRole = true }
                    )
                    DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                role = r
                                expandedRole = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && pass.isNotEmpty()) {
                        onConfirm(name, email, phone, pass, role)
                    } else {
                        Toast.makeText(context, "Fill in all credentials fields", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Register Staff")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyStateView(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

val size16Modifier = Modifier.size(16.dp)
