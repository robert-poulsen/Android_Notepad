package com.valerij.notepad.ui.theme

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.valerij.notepad.data.Note

class NotesViewModel : ViewModel() {

    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> = _notes

    var searchQuery by mutableStateOf("")
        private set

    fun updateSearch(query: String) {
        searchQuery = query
    }

    fun filteredNotes(): List<Note> {
        if (searchQuery.isBlank()) return notes
        return notes.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    fun addNote(note: Note) {
        _notes.add(note)
    }

    fun updateNote(updated: Note) {
        val index = _notes.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            _notes[index] = updated
        }
    }

    fun getNoteById(id: String): Note? {
        return _notes.find { it.id == id }
    }
}
