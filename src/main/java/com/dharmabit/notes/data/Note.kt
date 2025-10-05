package com.dharmabit.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.dharmabit.notes.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class NoteType {
    TEXT, CHECKLIST
}

data class ChecklistItem(
    val id: Long = System.currentTimeMillis(),
    var text: String,
    var isChecked: Boolean
)

@Entity(tableName = "notes_table")
@TypeConverters(ChecklistConverter::class)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val noteType: NoteType = NoteType.TEXT,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val imageUri: String? = null,
    val isArchived: Boolean = false,
    val audioPath: String? = null,
    // New security fields
    val isPrivate: Boolean = false,
    val isEncrypted: Boolean = false,
    val encryptedContent: String? = null,
    val encryptedTitle: String? = null,
    val hasCustomPassword: Boolean = false,
    val customPasswordHash: String? = null
)

class ChecklistConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromChecklist(checklist: List<ChecklistItem>): String = gson.toJson(checklist)
    @TypeConverter fun toChecklist(json: String): List<ChecklistItem> {
        val type = object : TypeToken<List<ChecklistItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}

// Extension functions for encryption/decryption
fun Note.getDecryptedTitle(securityManager: SecurityManager): String {
    return if (isEncrypted && encryptedTitle != null) {
        securityManager.decryptText(encryptedTitle) ?: title
    } else title
}

fun Note.getDecryptedContent(securityManager: SecurityManager): String {
    return if (isEncrypted && encryptedContent != null) {
        securityManager.decryptText(encryptedContent) ?: content
    } else content
}

fun Note.encrypt(securityManager: SecurityManager): Note {
    val encTitle = securityManager.encryptText(title)
    val encContent = securityManager.encryptText(content)

    return copy(
        isEncrypted = true,
        encryptedTitle = encTitle,
        encryptedContent = encContent,
        title = if (encTitle != null) "" else title, // Clear plaintext only if encryption succeeded
        content = if (encContent != null) "" else content // Clear plaintext only if encryption succeeded
    )
}

fun Note.decrypt(securityManager: SecurityManager): Note {
    return copy(
        title = getDecryptedTitle(securityManager),
        content = getDecryptedContent(securityManager),
        isEncrypted = false,
        encryptedTitle = null,
        encryptedContent = null
    )
}