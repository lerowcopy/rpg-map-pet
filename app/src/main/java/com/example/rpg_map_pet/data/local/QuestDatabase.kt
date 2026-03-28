package com.example.rpg_map_pet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [QuestEntity::class, LandmarkEntity::class, LandmarkPhotoEntity::class],
    version = 6,
    exportSchema = false
)
abstract class QuestDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun landmarkDao(): LandmarkDao
    abstract fun landmarkPhotoDao(): LandmarkPhotoDao

    companion object {
        @Volatile private var INSTANCE: QuestDatabase? = null

        // Миграция с версии 3 на 4: добавление поля completedAt
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE landmarks ADD COLUMN completedAt INTEGER DEFAULT NULL")
            }
        }

        // Миграция с версии 4 на 5: создание таблицы landmark_photos
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE landmark_photos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        landmarkId TEXT NOT NULL,
                        photoPath TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY (landmarkId) REFERENCES landmarks(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX index_landmark_photos_landmarkId ON landmark_photos (landmarkId)")
            }
        }

        // Миграция с версии 5 на 6: не требуется, схема не меняется
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Схема не меняется, миграция пустая
            }
        }

        fun getDatabase(context: Context): QuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuestDatabase::class.java,
                    "quest_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration() // Для версий 1, 2, 3 — уничтожаем и создаём заново
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
