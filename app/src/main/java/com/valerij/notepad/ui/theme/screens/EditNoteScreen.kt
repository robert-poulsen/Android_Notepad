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
import com.valerij.notepad.ui.theme.Typography
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
    var pinnedNote by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNote(noteId)?.let { note ->
                title = note.title
                content = note.content
                pinnedNote = note.pinned
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
                title = { TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Title") },
                    singleLine = true,
                    textStyle = Typography.bodyLarge,
                )},
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
                                    content = content,
                                    pinned = pinnedNote
                                )
                            )
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }

                    if (noteId != null) {
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDeleteDialog = false
                            },
                            title = { Text(
                                text = "Delete note?",
                                style = Typography.bodyLarge
                            )},
                            text = { Text(
                                text = "Are you sure you want to delete this note?",
                                style = Typography.bodyMedium
                            )},
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.getNote(noteId!!)?.let {
                                                viewModel.deleteNote(it)
                                            }
                                            showDeleteDialog = false
                                            navController.popBackStack()
                                        }
                                    }
                                ) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog = false
                                    }
                                ) {
                                    Text("No")
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 10.dp)
                .fillMaxSize()
        ) {
            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text("Text here") },
                textStyle = Typography.bodySmall,
            )
        }
    }
}