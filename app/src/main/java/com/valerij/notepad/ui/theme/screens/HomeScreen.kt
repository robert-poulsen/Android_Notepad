package com.valerij.notepad.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.valerij.notepad.data.local.NoteEntity

import com.valerij.notepad.ui.theme.components.NoteItem
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NotesViewModel
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }

    val sortedNotes = notes.sortedWith(
        compareByDescending<NoteEntity> { it.pinned }
            .thenByDescending { it.createdAt }
    )

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedNotes.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedNotes.clear()
                        }) {
                            Icon(Icons.Default.Close, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.deleteNotes(selectedNotes)
                            selectedNotes.clear()
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                )
            } else {
                TopAppBar(title = { Text("Notes") })
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (expanded) {
                    FloatingActionButton(
                        onClick = {
                            expanded = false
                            navController.navigate("editNoteScreen")
                        },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) { Text("T") }

                    FloatingActionButton(
                        onClick = {
                            expanded = false
                            navController.navigate("checklistScreen")
                        },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) { Text("C") }
                }
                FloatingActionButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .imePadding()
        ) {

            if (!selectionMode) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn {
                items(sortedNotes, key = { it.id }) { note ->

                    val isSelected = selectedNotes.contains(note.id)

                    val content: @Composable () -> Unit = {
                        NoteItem(
                            note = note,
                            isSelected = isSelected,
                            onLongClick = {
                                selectionMode = true
                                if (!isSelected) selectedNotes.add(note.id)
                            },
                            onClick = {
                                if (selectionMode) {
                                    if (isSelected) {
                                        selectedNotes.remove(note.id)
                                        if (selectedNotes.isEmpty()) selectionMode = false
                                    } else {
                                        selectedNotes.add(note.id)
                                    }
                                } else {
                                    if (note.checklist) {
                                        navController.navigate("checklistScreen?noteId=${note.id}")
                                    } else {
                                        navController.navigate("editNoteScreen?noteId=${note.id}")
                                    }
                                }
                            },
                            onPinClick = { viewModel.togglePin(note) }
                        )
                    }

                    if (!selectionMode) {
                        SwipeToDismissBox(
                            state = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    when (value) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            viewModel.deleteNote(note)
                                            true
                                        }
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            viewModel.togglePin(note)
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            ),
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray)
                                )
                            }
                        ) {
                            content()
                        }
                    } else {
                        content()
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
