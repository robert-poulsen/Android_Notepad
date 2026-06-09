package com.valerij.notepad.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.data.repository.NotesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    enum class SortType {
        DATE_ASC,
        DATE_DESC,
        TITLE_ASC,
        TITLE_DESC
    }

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType = _sortType.asStateFlow()
    val searchQuery = _searchQuery.asStateFlow()

    val notes = searchQuery.flatMapLatest {
        repository.getAllNotes(it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val deletedNotes = searchQuery.flatMapLatest {
        repository.getDeletedNotes()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun togglePin(noteId: String) {
        viewModelScope.launch {
            val current = repository.getById(noteId) ?: return@launch

            repository.addOrUpdate(
                current.copy(pinned = !current.pinned)
            )
        }
    }

    fun softDelete(noteId: String) {
        viewModelScope.launch {
            val current = repository.getById(noteId) ?: return@launch

            repository.addOrUpdate(
                current.copy(deleted = !current.deleted)
            )
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.addOrUpdate(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteNotes(ids: List<String>) {
        viewModelScope.launch {
            repository.deleteNotes(ids)
        }
    }

    fun sortNotes(selectedSort: SortType) {
        _sortType.value = when (selectedSort) {
            SortType.DATE_ASC -> SortType.DATE_ASC
            SortType.DATE_DESC -> SortType.DATE_DESC
            SortType.TITLE_ASC -> SortType.TITLE_ASC
            SortType.TITLE_DESC -> SortType.TITLE_DESC
        }
    }

    suspend fun getNote(id: String): NoteEntity? {
        return repository.getById(id)
    }
}
