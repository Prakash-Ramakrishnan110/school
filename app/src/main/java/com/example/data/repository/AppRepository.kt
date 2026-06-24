package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(
    private val userDao: UserDao,
    private val schoolDao: SchoolDao,
    private val toiletDao: ToiletDao,
    private val inspectionDao: InspectionDao,
    private val issueDao: IssueDao
) {
    // User Operations
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun getUserByEmailAndPassword(email: String, psw: String) = userDao.getUserByEmailAndPassword(email, psw)
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)
    suspend fun getUserById(id: Int) = userDao.getUserById(id)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // School Operations
    fun getAllSchools(): Flow<List<School>> = schoolDao.getAllSchools()
    suspend fun getSchoolById(id: Int) = schoolDao.getSchoolById(id)
    suspend fun insertSchool(school: School) = schoolDao.insertSchool(school)
    suspend fun deleteSchool(school: School) = schoolDao.deleteSchool(school)

    // Toilet Operations
    fun getAllToilets(): Flow<List<Toilet>> = toiletDao.getAllToilets()
    fun getToiletsBySchool(schoolId: Int): Flow<List<Toilet>> = toiletDao.getToiletsBySchool(schoolId)
    suspend fun getToiletById(id: Int) = toiletDao.getToiletById(id)
    suspend fun getToiletByQrCode(qrCode: String) = toiletDao.getToiletByQrCode(qrCode)
    suspend fun insertToilet(toilet: Toilet) = toiletDao.insertToilet(toilet)
    suspend fun updateToilet(toilet: Toilet) = toiletDao.updateToilet(toilet)
    suspend fun updateToiletStatus(id: Int, status: String) = toiletDao.updateToiletStatus(id, status)
    suspend fun deleteToilet(toilet: Toilet) = toiletDao.deleteToilet(toilet)

    // Inspection Operations
    fun getAllInspections(): Flow<List<Inspection>> = inspectionDao.getAllInspections()
    fun getInspectionsByToilet(toiletId: Int): Flow<List<Inspection>> = inspectionDao.getInspectionsByToilet(toiletId)
    fun getRecentInspections(limit: Int): Flow<List<Inspection>> = inspectionDao.getRecentInspections(limit)
    suspend fun insertInspection(inspection: Inspection) = inspectionDao.insertInspection(inspection)

    // Issue Operations
    fun getAllIssues(): Flow<List<Issue>> = issueDao.getAllIssues()
    fun getIssuesByToilet(toiletId: Int): Flow<List<Issue>> = issueDao.getIssuesByToilet(toiletId)
    suspend fun getIssueById(id: Int) = issueDao.getIssueById(id)
    suspend fun insertIssue(issue: Issue) = issueDao.insertIssue(issue)
    suspend fun updateIssueStatus(issueId: Int, status: String) = issueDao.updateIssueStatus(issueId, status)
    suspend fun deleteIssue(issue: Issue) = issueDao.deleteIssue(issue)

    // Seed initial data if DB is empty
    suspend fun prepopulateDataIfNecessary() {
        // Check if users table is empty
        val existingUsers = userDao.getAllUsers().firstOrNull() ?: emptyList()
        if (existingUsers.isNotEmpty()) return

        // 1. Insert Default Users
        val adminId = userDao.insertUser(
            User(
                name = "Prakash Kumar (Admin)",
                email = "admin@school.com",
                password = "admin123",
                phone = "9876543210",
                role = "Admin"
            )
        ).toInt()

        val hmId = userDao.insertUser(
            User(
                name = "Dr. Rajendran (Headmaster)",
                email = "headmaster@school.com",
                password = "headmaster123",
                phone = "9876543211",
                role = "Headmaster"
            )
        ).toInt()

        val cleanerId = userDao.insertUser(
            User(
                name = "Selvam (Cleaner)",
                email = "cleaner@school.com",
                password = "cleaner123",
                phone = "9876543212",
                role = "Cleaner"
            )
        ).toInt()

        // 2. Insert Default Schools
        val school1Id = schoolDao.insertSchool(
            School(
                schoolName = "Govt Boys Higher Secondary School",
                district = "Chennai",
                address = "12, Kamarajar Salai, Triplicane, Chennai - 600005",
                headmasterName = "Dr. Rajendran",
                phone = "9876543211"
            )
        ).toInt()

        val school2Id = schoolDao.insertSchool(
            School(
                schoolName = "Kamaraj Model School",
                district = "Madurai",
                address = "45, Bypass Road, Madurai - 625016",
                headmasterName = "Mrs. Meenakshi",
                phone = "9845612300"
            )
        ).toInt()

        // 3. Insert Toilets for School 1
        val t1 = toiletDao.insertToilet(
            Toilet(
                schoolId = school1Id,
                toiletName = "Main Block Boys",
                toiletType = "Boys Toilet",
                floor = 0,
                block = "Block A",
                qrCode = "WM-BOYS-A-01",
                status = "Active"
            )
        ).toInt()

        val t2 = toiletDao.insertToilet(
            Toilet(
                schoolId = school1Id,
                toiletName = "Main Block Girls",
                toiletType = "Girls Toilet",
                floor = 0,
                block = "Block A",
                qrCode = "WM-GIRLS-A-01",
                status = "Active"
            )
        ).toInt()

        val t3 = toiletDao.insertToilet(
            Toilet(
                schoolId = school1Id,
                toiletName = "Staff Lounge Toilet",
                toiletType = "Staff Toilet",
                floor = 1,
                block = "Block B",
                qrCode = "WM-STAFF-01",
                status = "Active"
            )
        ).toInt()

        val t4 = toiletDao.insertToilet(
            Toilet(
                schoolId = school1Id,
                toiletName = "Accessible Ground Floor",
                toiletType = "Accessible Toilet",
                floor = 0,
                block = "Block A",
                qrCode = "WM-ACC-01",
                status = "Under Maintenance"
            )
        ).toInt()

        // Toilets for School 2
        val t5 = toiletDao.insertToilet(
            Toilet(
                schoolId = school2Id,
                toiletName = "Primary Boys Toilet",
                toiletType = "Boys Toilet",
                floor = 0,
                block = "Block C",
                qrCode = "WM-BOYS-C-01",
                status = "Active"
            )
        ).toInt()

        val t6 = toiletDao.insertToilet(
            Toilet(
                schoolId = school2Id,
                toiletName = "Primary Girls Toilet",
                toiletType = "Girls Toilet",
                floor = 0,
                block = "Block C",
                qrCode = "WM-GIRLS-C-01",
                status = "Active"
            )
        ).toInt()

        // 4. Insert Default Inspections
        inspectionDao.insertInspection(
            Inspection(
                toiletId = t1,
                cleanerId = cleanerId,
                cleanerName = "Selvam",
                cleaned = true,
                waterAvailable = true,
                soapAvailable = true,
                flushWorking = true,
                dustbinEmptied = true,
                badSmell = false,
                floorCondition = "Good",
                wallCondition = "Good",
                overallStatus = "Clean",
                beforeImage = "cleaner_task_before_1",
                afterImage = "cleaner_task_after_1",
                inspectionType = "Daily",
                createdAt = System.currentTimeMillis() - 86400000 // 1 day ago
            )
        )

        inspectionDao.insertInspection(
            Inspection(
                toiletId = t2,
                cleanerId = cleanerId,
                cleanerName = "Selvam",
                cleaned = true,
                waterAvailable = true,
                soapAvailable = false, // Soap missing
                flushWorking = true,
                dustbinEmptied = true,
                badSmell = true, // Smelly
                floorCondition = "Average",
                wallCondition = "Good",
                overallStatus = "Need Attention",
                beforeImage = "cleaner_task_before_2",
                afterImage = "cleaner_task_after_2",
                inspectionType = "Daily",
                createdAt = System.currentTimeMillis() - 43200000 // 12 hours ago
            )
        )

        // 5. Insert Sample Issues
        issueDao.insertIssue(
            Issue(
                toiletId = t1,
                category = "Broken Tap",
                priority = "High",
                description = "Tap in second sink is broken and leaking water constantly.",
                photo = "issue_broken_tap",
                status = "In Progress",
                createdAt = System.currentTimeMillis() - 172800000 // 2 days ago
            )
        )

        issueDao.insertIssue(
            Issue(
                toiletId = t4,
                category = "Flush Problem",
                priority = "Critical",
                description = "Flush tank mechanism completely detached. Requires plumber replacement.",
                photo = "issue_flush",
                status = "Reported",
                createdAt = System.currentTimeMillis() - 86400000 // 1 day ago
            )
        )

        issueDao.insertIssue(
            Issue(
                toiletId = t3,
                category = "Light Failure",
                priority = "Medium",
                description = "Ceiling bulb has fused, staff toilet is dark.",
                photo = "issue_bulb",
                status = "Resolved",
                createdAt = System.currentTimeMillis() - 259200000 // 3 days ago
            )
        )
    }
}
