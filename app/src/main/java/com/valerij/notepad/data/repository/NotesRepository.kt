package com.valerij.notepad.data.repository

import com.valerij.notepad.data.local.NoteDao
import com.valerij.notepad.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val dao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = dao.getAllNotes()

    suspend fun addOrUpdate(note: NoteEntity) {
        dao.insert(note)
    }

    suspend fun getById(id: String): NoteEntity? {
        return dao.getNoteById(id)
    }
}