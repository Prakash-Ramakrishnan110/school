package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>
}

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools ORDER BY schoolName ASC")
    fun getAllSchools(): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE id = :id LIMIT 1")
    suspend fun getSchoolById(id: Int): School?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: School): Long

    @Delete
    suspend fun deleteSchool(school: School)
}

@Dao
interface ToiletDao {
    @Query("SELECT * FROM toilets ORDER BY toiletName ASC")
    fun getAllToilets(): Flow<List<Toilet>>

    @Query("SELECT * FROM toilets WHERE schoolId = :schoolId ORDER BY toiletName ASC")
    fun getToiletsBySchool(schoolId: Int): Flow<List<Toilet>>

    @Query("SELECT * FROM toilets WHERE id = :id LIMIT 1")
    suspend fun getToiletById(id: Int): Toilet?

    @Query("SELECT * FROM toilets WHERE qrCode = :qrCode LIMIT 1")
    suspend fun getToiletByQrCode(qrCode: String): Toilet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToilet(toilet: Toilet): Long

    @Update
    suspend fun updateToilet(toilet: Toilet)

    @Query("UPDATE toilets SET status = :status WHERE id = :id")
    suspend fun updateToiletStatus(id: Int, status: String)

    @Delete
    suspend fun deleteToilet(toilet: Toilet)
}

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY createdAt DESC")
    fun getAllInspections(): Flow<List<Inspection>>

    @Query("SELECT * FROM inspections WHERE toiletId = :toiletId ORDER BY createdAt DESC")
    fun getInspectionsByToilet(toiletId: Int): Flow<List<Inspection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: Inspection): Long

    @Query("SELECT * FROM inspections ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentInspections(limit: Int): Flow<List<Inspection>>
}

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    fun getAllIssues(): Flow<List<Issue>>

    @Query("SELECT * FROM issues WHERE toiletId = :toiletId ORDER BY createdAt DESC")
    fun getIssuesByToilet(toiletId: Int): Flow<List<Issue>>

    @Query("SELECT * FROM issues WHERE id = :id LIMIT 1")
    suspend fun getIssueById(id: Int): Issue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: Issue): Long

    @Query("UPDATE issues SET status = :status WHERE id = :id")
    suspend fun updateIssueStatus(id: Int, status: String)

    @Delete
    suspend fun deleteIssue(issue: Issue)
}
