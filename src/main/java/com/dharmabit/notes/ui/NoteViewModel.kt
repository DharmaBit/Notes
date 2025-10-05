package com.dharmabit.notes.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dharmabit.notes.data.*
import com.dharmabit.notes.security.SecurityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : ViewModel() {
    private val repository: NoteRepository
    private val securityManager = SecurityManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isPrivateMode = MutableStateFlow(false)
    val isPrivateMode = _isPrivateMode.asStateFlow()

    private val _isUnlocked = MutableStateFlow(!securityManager.isSecurityEnabled())
    val isUnlocked = _isUnlocked.asStateFlow()

    val activeNotes: StateFlow<List<Note>>
    val archivedNotes: StateFlow<List<Note>>

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)

        activeNotes = combine(
            _isPrivateMode,
            _searchQuery,
            repository.activeNotes,
            repository.activePrivateNotes,
            repository.allActiveNotes
        ) { isPrivate, query, regularNotes, privateNotes, allNotes ->
            val notesToShow = when {
                !securityManager.isSecurityEnabled() -> allNotes
                isPrivate -> privateNotes
                else -> regularNotes
            }

            val decryptedNotes = notesToShow.map { note ->
                if (note.isEncrypted && securityManager.isSecurityEnabled()) {
                    note.copy(
                        title = note.getDecryptedTitle(securityManager),
                        content = note.getDecryptedContent(securityManager)
                    )
                } else note
            }

            if (query.isBlank()) decryptedNotes else decryptedNotes.filter {
                it.title.contains(query, true) || it.content.contains(query, true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        archivedNotes = combine(
            _isPrivateMode,
            _searchQuery,
            repository.archivedNotes,
            repository.archivedPrivateNotes,
            repository.allArchivedNotes
        ) { isPrivate, query, regularNotes, privateNotes, allNotes ->
            val notesToShow = when {
                !securityManager.isSecurityEnabled() -> allNotes
                isPrivate -> privateNotes
                else -> regularNotes
            }

            val decryptedNotes = notesToShow.map { note ->
                if (note.isEncrypted && securityManager.isSecurityEnabled()) {
                    note.copy(
                        title = note.getDecryptedTitle(securityManager),
                        content = note.getDecryptedContent(securityManager)
                    )
                } else note
            }

            if (query.isBlank()) decryptedNotes else decryptedNotes.filter {
                it.title.contains(query, true) || it.content.contains(query, true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Security methods
    fun getSecurityManager() = securityManager

    fun unlock() {
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun togglePrivateMode() {
        if (securityManager.isPrivateNotesEnabled()) {
            _isPrivateMode.value = !_isPrivateMode.value
        }
    }

    fun disableSecurity() {
        viewModelScope.launch {
            try {
                // Decrypt all encrypted notes before disabling security
                val encryptedNotes = repository.getEncryptedNotes()
                encryptedNotes.forEach { note ->
                    val decryptedNote = note.decrypt(securityManager)
                    repository.insertOrUpdate(decryptedNote)
                }

                securityManager.disableSecurity()
                _isUnlocked.value = true
                _isPrivateMode.value = false
            } catch (e: Exception) {
                android.util.Log.e("NoteViewModel", "Error disabling security", e)
            }
        }
    }

    // Note operations
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun insertOrUpdate(note: Note) = viewModelScope.launch {
        try {
            val noteToSave = if (securityManager.isSecurityEnabled() && _isPrivateMode.value) {
                val privateNote = note.copy(isPrivate = true)
                // Encrypt private notes
                privateNote.encrypt(securityManager)
            } else {
                note.copy(isPrivate = false)
            }

            repository.insertOrUpdate(noteToSave)
        } catch (e: Exception) {
            android.util.Log.e("NoteViewModel", "Error saving note", e)
        }
    }

    fun deleteById(noteId: Int) = viewModelScope.launch {
        repository.deleteById(noteId)
    }

    suspend fun getNoteById(id: Int): Note? {
        return try {
            val note = repository.getNoteById(id)
            if (note?.isEncrypted == true && securityManager.isSecurityEnabled()) {
                note.copy(
                    title = note.getDecryptedTitle(securityManager),
                    content = note.getDecryptedContent(securityManager)
                )
            } else note
        } catch (e: Exception) {
            android.util.Log.e("NoteViewModel", "Error getting note by ID", e)
            null
        }
    }

    fun archiveNote(note: Note) = insertOrUpdate(note.copy(isArchived = true, isPinned = false))
    fun unarchiveNote(note: Note) = insertOrUpdate(note.copy(isArchived = false))
}

class NoteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}