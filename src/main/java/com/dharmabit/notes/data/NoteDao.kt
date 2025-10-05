package com.dharmabit.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note): Int

    @Query("DELETE FROM notes_table WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int): Int

    @Query("SELECT * FROM notes_table WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    // Regular notes (non-private)
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND (isPrivate = 0 OR isPrivate IS NULL) ORDER BY isPinned DESC, timestamp DESC")
    fun getActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND (isPrivate = 0 OR isPrivate IS NULL) ORDER BY timestamp DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    // Private notes
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isPrivate = 1 ORDER BY isPinned DESC, timestamp DESC")
    fun getActivePrivateNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isPrivate = 1 ORDER BY timestamp DESC")
    fun getArchivedPrivateNotes(): Flow<List<Note>>

    // All notes (for when security is disabled)
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getAllActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 ORDER BY timestamp DESC")
    fun getAllArchivedNotes(): Flow<List<Note>>

    // Get encrypted notes (for batch decryption if needed)
    @Query("SELECT * FROM notes_table WHERE isEncrypted = 1")
    suspend fun getEncryptedNotes(): List<Note>

    // Check if note exists
    @Query("SELECT COUNT(*) FROM notes_table WHERE id = :noteId")
    suspend fun noteExists(noteId: Int): Int
}