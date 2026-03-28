package com.example.rpg_map_pet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [QuestEntity::class, LandmarkEntity::class],
    version = 4,
    exportSchema = false
)
abstract class QuestDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun landmarkDao(): LandmarkDao

    companion object {
        @Volatile private var INSTANCE: QuestDatabase? = null

        // Миграция с версии 3 на 4: добавление поля completedAt
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE landmarks ADD COLUMN completedAt INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): QuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuestDatabase::class.java,
                    "quest_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // Для версий 1, 2 — уничтожаем и создаём заново
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
