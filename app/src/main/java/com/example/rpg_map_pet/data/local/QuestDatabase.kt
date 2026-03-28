package com.example.rpg_map_pet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QuestEntity::class, LandmarkEntity::class],
    version = 3,
    exportSchema = false
)
abstract class QuestDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun landmarkDao(): LandmarkDao

    companion object {
        @Volatile private var INSTANCE: QuestDatabase? = null

        fun getDatabase(context: Context): QuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuestDatabase::class.java,
                    "quest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
