package com.valerij.notepad.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.valerij.notepad.data.local.NotesDatabase
import com.valerij.notepad.data.repository.NotesRepository

class NotesViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val repository =
        NotesRepository(NotesDatabase.getDatabase(context).noteDao())

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotesViewModel(repository) as T
    }
}
