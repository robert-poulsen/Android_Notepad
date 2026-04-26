package com.valerij.notepad.ui.theme.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
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
import kotlinx.coroutines.delay

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
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLeavingScreen by remember { mutableStateOf(false) }
    val sortDesc by viewModel.sortDesc.collectAsState()
    val sortByAlphabet by viewModel.sortByAlphabet.collectAsState()
    var pendingPinId by remember { mutableStateOf<String?>(null) }
    val sortType by viewModel.sortType.collectAsState()

    val sortedNotes = remember(notes, sortType) {

        val pinned = notes.filter { it.pinned }
        val others = notes.filter { !it.pinned }

        fun sortList(list: List<NoteEntity>): List<NoteEntity> {
            return when (sortType) {

                NotesViewModel.SortType.TITLE_ASC -> list.sortedWith(compareBy {
                    val title = it.title.trim().lowercase()
                    val isDigit = title.firstOrNull()?.isDigit() == true
                    if (isDigit) "0$title" else "1$title"
                })

                NotesViewModel.SortType.TITLE_DESC -> list.sortedWith(compareByDescending {
                    val title = it.title.trim().lowercase()
                    val isDigit = title.firstOrNull()?.isDigit() == true
                    if (isDigit) "0$title" else "1$title"
                })

                NotesViewModel.SortType.DATE_ASC -> list.sortedBy { it.createdAt }

                NotesViewModel.SortType.DATE_DESC -> list.sortedByDescending { it.createdAt }
            }
        }

        sortList(pinned) + sortList(others)
    }

    BackHandler(enabled = selectionMode) {
        selectionMode = false
        selectedNotes.clear()
    }

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
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ){
                    if (expanded) {
                        FloatingActionButton(
                            onClick = {
                                expanded = false
                                navController.navigate("editNoteScreen")
                            },
                            modifier = Modifier.offset(x = (-70).dp, y = (-70).dp)
                        ) { Icon(Icons.Default.Notes, null) }

                        FloatingActionButton(
                            onClick = {
                                expanded = false
                                navController.navigate("checklistScreen")
                            },
                            modifier = Modifier.offset(x = 0.dp, y = (-100).dp)
                        ) { Icon(Icons.Default.Checklist, null) }
                    }
                    FloatingActionButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.Add, null)
                    }
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

                    IconButton(onClick = { viewModel.toggleSortByDate() }) {
                        Icon(Icons.Default.Sort, null)
                    }

                    IconButton(onClick = { viewModel.toggleSortByAlphabet() }) {
                        Icon(Icons.Default.SortByAlpha, null)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showDeleteDialog && selectedNotes.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete notes?") },
                    text = { Text("Are you sure you want to delete selected notes?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteNotes(selectedNotes.toList())
                                selectedNotes.clear()
                                selectionMode = false
                                showDeleteDialog = false
                            }
                        ) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

            noteToDelete?.let { note ->
                AlertDialog(
                    onDismissRequest = { noteToDelete = null },
                    title = { Text("Delete note?") },
                    text = { Text("Are you sure you want to delete this note?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteNote(note)
                                noteToDelete = null
                            }
                        ) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { noteToDelete = null }) {
                            Text("No")
                        }
                    }
                )
            }

            LaunchedEffect(pendingPinId) {
                pendingPinId?.let { id ->
                    delay(120)
                    viewModel.togglePin(id)
                    pendingPinId = null
                }
            }

            LazyColumn {
                items(sortedNotes, key = { it.id }) { note ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { it * 0.5f },
                        confirmValueChange = { value ->
                            when (value) {

                                SwipeToDismissBoxValue.StartToEnd -> {
                                    noteToDelete = note
                                    false
                                }

                                SwipeToDismissBoxValue.EndToStart -> {
                                    pendingPinId = note.id
                                    false
                                }

                                else -> false
                            }
                        }


                    )


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
                            onPinClick = { viewModel.togglePin(note.id) }
                        )
                    }

                    if (!selectionMode) {
                        key(note.id) {
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {

                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Color.Red
                                        SwipeToDismissBoxValue.EndToStart -> Color.Yellow
                                        else -> Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {

                                    }
                                }
                            ) {
                                content()
                            }
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
