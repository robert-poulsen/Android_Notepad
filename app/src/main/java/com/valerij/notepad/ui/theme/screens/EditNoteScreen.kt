package com.valerij.notepad.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.NotesViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    noteId: String?
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    // 🔹 Завантаження нотатки
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNote(noteId)?.let { note ->
                title = note.title
                content = note.content
            }
        }
        loaded = true
    }

    if (!loaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Нотатка") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val finalTitle =
                                if (title.isBlank()) {
                                    content
                                        .lineSequence()
                                        .firstOrNull()
                                        ?.take(30)
                                        ?: "Без назви"
                                } else title

                            viewModel.saveNote(
                                NoteEntity(
                                    id = noteId ?: UUID.randomUUID().toString(),
                                    title = finalTitle,
                                    content = content
                                )
                            )
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }

                    if (noteId != null) {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.getNote(noteId)?.let {
                                    viewModel.deleteNote(it)
                                }
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
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
                placeholder = { Text("Заголовок") },
                singleLine = true
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