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

    val searchQuery = MutableStateFlow("")

    val notes: StateFlow<List<NoteEntity>> =
        searchQuery.flatMapLatest { query ->
            repository.getAllNotes().map { list ->
                if (query.isBlank()) list
                else list.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )


    fun updateSearch(query: String) {
        searchQuery.value = query
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
