package com.valerij.notepad.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valerij.notepad.data.Note
import com.valerij.notepad.ui.theme.NotesViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    noteId: String?
) {
    val existingNote = noteId?.let { viewModel.getNoteById(it) }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Назва нотатки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (existingNote == null) {
                            viewModel.addNote(Note(title = title, content = content))
                        } else {
                            viewModel.updateNote(
                                existingNote.copy(title = title, content = content)
                            )
                        }
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Save, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Заголовок") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text("Текст нотатки") }
            )
        }
    }
}