package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entity.*

@Database(
    entities = [
        User::class,
        School::class,
        Toilet::class,
        Inspection::class,
        Issue::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun schoolDao(): SchoolDao
    abstract fun toiletDao(): ToiletDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun issueDao(): IssueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hygiene360_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
