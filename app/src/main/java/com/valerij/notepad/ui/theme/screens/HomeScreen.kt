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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.valerij.notepad.data.local.NoteEntity

import com.valerij.notepad.ui.theme.components.NoteItem
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NotesViewModel
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLeavingScreen by remember { mutableStateOf(false) }
    val sortDesc by viewModel.sortDesc.collectAsState()

    val sortedNotes = notes.sortedWith(
        compareByDescending<NoteEntity> { it.pinned }
            .thenBy {
                if (sortDesc) -it.createdAt else it.createdAt
            }
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
                Row() {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearch,
                        placeholder = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                    )

                    IconButton(onClick = {
                        viewModel.toggleSortByDate()
                    }) {
                        Icon(Icons.Default.Sort, null)
                    }

                    IconButton(onClick = {
                        viewModel.toggleSortByAlfa()
                    }) {
                        Icon(Icons.Default.SortByAlpha, null)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn {
                items(sortedNotes, key = { it.id }) { note ->

                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { totalDistance ->
                            totalDistance * 0.5f
                        }
                    )

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
                                        if (note.id == null){
                                            isLeavingScreen = true

                                            showDeleteDialog = false
                                            navController.popBackStack()
                                        } else {
                                            isLeavingScreen = true

                                            scope.launch {
                                                viewModel.getNote(note.id!!)?.let {
                                                    viewModel.deleteNote(it)
                                                }
                                                showDeleteDialog = false
                                                navController.popBackStack()
                                            }
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


                    LaunchedEffect(dismissState.currentValue) {
                        when (dismissState.currentValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                showDeleteDialog = true
                                launch {
                                    dismissState.reset()
                                }
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.togglePin(note)
                                launch {
                                    dismissState.reset()
                                }
                            }
                            else -> Unit
                        }
                    }

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
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.targetValue){
                                    SwipeToDismissBoxValue.StartToEnd -> Color.Red
                                    SwipeToDismissBoxValue.EndToStart -> Color.Yellow
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
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
