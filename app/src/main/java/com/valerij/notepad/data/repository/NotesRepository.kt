package com.valerij.notepad.data.repository

import androidx.room.util.query
import com.valerij.notepad.data.local.NoteDao
import com.valerij.notepad.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val dao: NoteDao
) {
    fun getAllNotes(query: String) = dao.getAllNotes(query)

    suspend fun addOrUpdate(note: NoteEntity) {
        dao.insert(note)
    }

    suspend fun deleteNote(note: NoteEntity){
        dao.delete(note)
    }

    suspend fun getById(id: String): NoteEntity? {
        return dao.getNoteById(id)
    }
}