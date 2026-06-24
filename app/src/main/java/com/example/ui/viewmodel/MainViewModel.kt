package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AppRepository

    // Current Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Scanned toilet or checked-in toilet
    private val _scannedToilet = MutableStateFlow<Toilet?>(null)
    val scannedToilet: StateFlow<Toilet?> = _scannedToilet.asStateFlow()

    // Search & Filter state
    val searchQuery = MutableStateFlow("")
    val activeSchoolFilterId = MutableStateFlow<Int?>(null)
    val activeStatusFilter = MutableStateFlow<String?>(null) // "All", "Active", "Under Maintenance", "Closed"
    val activePriorityFilter = MutableStateFlow<String?>(null) // "All", "Low", "Medium", "High", "Critical"

    // Theme state
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Notification State
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(
            database.userDao(),
            database.schoolDao(),
            database.toiletDao(),
            database.inspectionDao(),
            database.issueDao()
        )

        // Seed sample data in the background and trigger initial notifications
        viewModelScope.launch {
            repository.prepopulateDataIfNecessary()
            setupDefaultNotifications()
        }
    }

    // Exposed Flows from DB
    val schools: StateFlow<List<School>> = repository.getAllSchools()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toilets: StateFlow<List<Toilet>> = repository.getAllToilets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inspections: StateFlow<List<Inspection>> = repository.getAllInspections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val issues: StateFlow<List<Issue>> = repository.getAllIssues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists for the UI
    val filteredToilets: Flow<List<Toilet>> = combine(toilets, activeSchoolFilterId, activeStatusFilter, searchQuery) { list, schoolId, status, query ->
        list.filter { toilet ->
            (schoolId == null || toilet.schoolId == schoolId) &&
            (status == null || status == "All" || toilet.status.equals(status, ignoreCase = true)) &&
            (query.isEmpty() || toilet.toiletName.contains(query, ignoreCase = true) || toilet.qrCode.contains(query, ignoreCase = true) || toilet.block.contains(query, ignoreCase = true))
        }
    }

    val filteredIssues: Flow<List<Issue>> = combine(issues, activePriorityFilter, searchQuery) { list, priority, query ->
        list.filter { issue ->
            (priority == null || priority == "All" || issue.priority.equals(priority, ignoreCase = true)) &&
            (query.isEmpty() || issue.category.contains(query, ignoreCase = true) || issue.description.contains(query, ignoreCase = true))
        }
    }

    // Authentication Actions
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmailAndPassword(email, password)
            if (user != null) {
                _currentUser.value = user
                onResult(true, "Welcome back, ${user.name}!")
            } else {
                onResult(false, "Invalid email or password.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _scannedToilet.value = null
    }

    fun registerUser(name: String, email: String, password: String, phone: String, role: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onResult(false, "Email is already registered.")
                return@launch
            }
            val newUser = User(name = name, email = email, password = password, phone = phone, role = role)
            repository.insertUser(newUser)
            onResult(true, "Account created successfully for $name.")
        }
    }

    fun updateProfile(name: String, phone: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val updated = current.copy(name = name, phone = phone, email = email)
            repository.updateUser(updated)
            _currentUser.value = updated
            onResult(true, "Profile updated successfully!")
        }
    }

    // QR Code Scanning Flow (Simulated QR lookup)
    fun scanQrCode(qrCode: String): Boolean {
        var found = false
        viewModelScope.launch {
            val toilet = repository.getToiletByQrCode(qrCode)
            if (toilet != null) {
                _scannedToilet.value = toilet
                found = true
            }
        }
        return found
    }

    fun setScannedToilet(toilet: Toilet?) {
        _scannedToilet.value = toilet
    }

    // Admin Actions: School Management
    fun addSchool(name: String, district: String, address: String, hmName: String, phone: String) {
        viewModelScope.launch {
            repository.insertSchool(
                School(
                    schoolName = name,
                    district = district,
                    address = address,
                    headmasterName = hmName,
                    phone = phone
                )
            )
            addNotification("New School Added", "School '$name' registered successfully in district $district.", "Info")
        }
    }

    fun deleteSchool(school: School) {
        viewModelScope.launch {
            repository.deleteSchool(school)
            addNotification("School Deleted", "School '${school.schoolName}' removed from system.", "Warning")
        }
    }

    // Admin Actions: Toilet Management
    fun addToilet(schoolId: Int, name: String, type: String, floor: Int, block: String, qrCode: String) {
        viewModelScope.launch {
            repository.insertToilet(
                Toilet(
                    schoolId = schoolId,
                    toiletName = name,
                    toiletType = type,
                    floor = floor,
                    block = block,
                    qrCode = qrCode,
                    status = "Active"
                )
            )
            addNotification("New Toilet Added", "Toilet '$name' added for verification.", "Info")
        }
    }

    fun deleteToilet(toilet: Toilet) {
        viewModelScope.launch {
            repository.deleteToilet(toilet)
            addNotification("Toilet Deleted", "Toilet '${toilet.toiletName}' removed.", "Warning")
        }
    }

    fun updateToiletStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateToiletStatus(id, status)
        }
    }

    // Cleaner Actions: Daily Cleaning Checklist Form
    fun submitInspection(
        toiletId: Int,
        cleaned: Boolean,
        waterAvailable: Boolean,
        soapAvailable: Boolean,
        flushWorking: Boolean,
        dustbinEmptied: Boolean,
        badSmell: Boolean,
        floorCondition: String,
        wallCondition: String,
        overallStatus: String,
        beforePhoto: String? = null,
        afterPhoto: String? = null,
        type: String = "Daily",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val cleaner = _currentUser.value
            val cleanerName = cleaner?.name ?: "Unknown Cleaner"
            val cleanerId = cleaner?.id ?: 0

            repository.insertInspection(
                Inspection(
                    toiletId = toiletId,
                    cleanerId = cleanerId,
                    cleanerName = cleanerName,
                    cleaned = cleaned,
                    waterAvailable = waterAvailable,
                    soapAvailable = soapAvailable,
                    flushWorking = flushWorking,
                    dustbinEmptied = dustbinEmptied,
                    badSmell = badSmell,
                    floorCondition = floorCondition,
                    wallCondition = wallCondition,
                    overallStatus = overallStatus,
                    beforeImage = beforePhoto ?: "sample_before",
                    afterImage = afterPhoto ?: "sample_after",
                    inspectionType = type
                )
            )

            // Update corresponding toilet status
            val resolvedStatus = if (overallStatus == "Clean") "Active" else "Under Maintenance"
            repository.updateToiletStatus(toiletId, resolvedStatus)

            addNotification(
                title = "Inspection Logged",
                message = "$type Inspection for toilet ID #$toiletId marked as $overallStatus by $cleanerName.",
                type = if (overallStatus == "Critical") "Error" else "Success"
            )
            onComplete()
        }
    }

    // Cleaner/Headmaster Actions: Issue Reporting
    fun submitIssue(
        toiletId: Int,
        category: String,
        priority: String,
        description: String,
        photo: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertIssue(
                Issue(
                    toiletId = toiletId,
                    category = category,
                    priority = priority,
                    description = description,
                    photo = photo ?: "sample_issue",
                    status = "Reported"
                )
            )

            // Auto-update toilet status to Under Maintenance if High or Critical issue is reported
            if (priority == "High" || priority == "Critical") {
                repository.updateToiletStatus(toiletId, "Under Maintenance")
            }

            addNotification(
                title = "Critical Issue Reported",
                message = "New issue [$category] with $priority priority reported on toilet ID #$toiletId.",
                type = if (priority == "Critical" || priority == "High") "Error" else "Warning"
            )
            onComplete()
        }
    }

    fun updateIssueStatus(issueId: Int, toiletId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateIssueStatus(issueId, newStatus)

            // If an issue is resolved, check if there are other open issue. If not, restore toilet status to Active
            if (newStatus == "Resolved") {
                repository.updateToiletStatus(toiletId, "Active")
                addNotification("Issue Resolved", "Issue ID #$issueId is marked as Resolved.", "Success")
            } else {
                addNotification("Issue Updated", "Issue ID #$issueId updated to '$newStatus'.", "Info")
            }
        }
    }

    // Toggle Dark Mode
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Notifications Management
    private fun setupDefaultNotifications() {
        _notifications.value = listOf(
            NotificationItem(1, "Toilet Cleaning Inspection Pending", "Daily inspection for Block A Boys toilet is due.", "Warning", System.currentTimeMillis() - 3600000),
            NotificationItem(2, "Weekly Maintenance Inspection Due", "Check water pipelines, fittings and ventilation exhaust systems today.", "Info", System.currentTimeMillis() - 10800000),
            NotificationItem(3, "Critical Alert: Girls Block A Needs Attention", "Reported: Water leak in the primary girls block floor. Priority: High.", "Error", System.currentTimeMillis() - 43200000)
        )
    }

    fun addNotification(title: String, message: String, type: String) {
        val newNotification = NotificationItem(
            id = (System.currentTimeMillis() % 100000).toInt(),
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun clearNotification(id: Int) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val type: String, // "Info", "Success", "Warning", "Error"
    val timestamp: Long
)
