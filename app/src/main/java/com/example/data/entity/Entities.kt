package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: String, // "Cleaner", "Headmaster", "Admin"
    val profilePhoto: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schools")
data class School(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolName: String,
    val district: String,
    val address: String,
    val headmasterName: String,
    val phone: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "toilets")
data class Toilet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolId: Int,
    val toiletName: String,
    val toiletType: String, // "Boys Toilet", "Girls Toilet", "Staff Toilet", "Accessible Toilet"
    val floor: Int,
    val block: String,
    val qrCode: String,
    val status: String = "Active", // "Active", "Under Maintenance", "Closed"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inspections")
data class Inspection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toiletId: Int,
    val cleanerId: Int,
    val cleanerName: String,
    val cleaned: Boolean,
    val waterAvailable: Boolean,
    val soapAvailable: Boolean,
    val flushWorking: Boolean,
    val dustbinEmptied: Boolean,
    val badSmell: Boolean,
    val floorCondition: String, // "Good", "Average", "Poor"
    val wallCondition: String,  // "Good", "Average", "Poor"
    val overallStatus: String,   // "Clean", "Need Attention", "Critical"
    val beforeImage: String? = null, // Store Base64 or local description/uri
    val afterImage: String? = null,
    val inspectionType: String = "Daily", // "Daily", "Weekly", "Monthly", "Yearly"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "issues")
data class Issue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toiletId: Int,
    val category: String, // "No Water", "Broken Tap", "Flush Problem", "Drain Blockage", "Light Failure", "Door Lock Damage", "Bad Smell", "Broken Tiles", "Water Leakage"
    val priority: String, // "Low", "Medium", "High", "Critical"
    val description: String,
    val photo: String? = null,
    val status: String = "Reported", // "Reported", "Assigned", "In Progress", "Resolved"
    val createdAt: Long = System.currentTimeMillis()
)
