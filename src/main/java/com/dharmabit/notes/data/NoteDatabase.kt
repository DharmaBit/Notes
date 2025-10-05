package com.dharmabit.notes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Note::class],
    version = 3, // Updated version for new security fields
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE notes_table_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isPinned INTEGER NOT NULL,
                        noteType TEXT NOT NULL,
                        checklistItems TEXT NOT NULL,
                        imageUri TEXT,
                        isArchived INTEGER NOT NULL,
                        audioPath TEXT
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO notes_table_new (id, title, content, timestamp, isPinned, noteType, checklistItems, imageUri, isArchived, audioPath)
                    SELECT id, title, content, timestamp, isPinned, noteType, checklistItems, imageUri, isArchived, audioPath
                    FROM notes_table
                """.trimIndent())

                database.execSQL("DROP TABLE notes_table")
                database.execSQL("ALTER TABLE notes_table_new RENAME TO notes_table")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new security columns with proper defaults
                database.execSQL("ALTER TABLE notes_table ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes_table ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes_table ADD COLUMN encryptedContent TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE notes_table ADD COLUMN encryptedTitle TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE notes_table ADD COLUMN hasCustomPassword INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE notes_table ADD COLUMN customPasswordHash TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}