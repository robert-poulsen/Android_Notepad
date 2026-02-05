package com.valerij.notepad.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.data.repository.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val notes = searchQuery.flatMapLatest {
        repository.getAllNotes(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.addOrUpdate(
                note.copy(pinned = !note.pinned)
            )
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.addOrUpdate(note)
        }
    }

    suspend fun deleteNote(note: NoteEntity){
        repository.deleteNote(note)
    }

    suspend fun getNote(id: String): NoteEntity? {
        return repository.getById(id)
    }
}
