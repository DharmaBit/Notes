package com.dharmabit.notes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    // Regular notes flows
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()

    // Private notes flows
    val activePrivateNotes: Flow<List<Note>> = noteDao.getActivePrivateNotes()
    val archivedPrivateNotes: Flow<List<Note>> = noteDao.getArchivedPrivateNotes()

    // All notes flows (for when security is disabled)
    val allActiveNotes: Flow<List<Note>> = noteDao.getAllActiveNotes()
    val allArchivedNotes: Flow<List<Note>> = noteDao.getAllArchivedNotes()

    suspend fun insertOrUpdate(note: Note) {
        try {
            noteDao.insertOrUpdateNote(note)
            android.util.Log.d("NoteRepository", "Note saved: ${note.id} - ${note.title}")
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error saving note", e)
        }
    }

    suspend fun deleteById(noteId: Int) {
        try {
            val deletedRows = noteDao.deleteNoteById(noteId)
            android.util.Log.d("NoteRepository", "Deleted rows: $deletedRows for ID: $noteId")
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error deleting note by ID", e)
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)
    }

    suspend fun getEncryptedNotes(): List<Note> {
        return noteDao.getEncryptedNotes()
    }
}