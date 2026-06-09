package com.valerij.notepad.ui.theme.screens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.valerij.notepad.R
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Typography
import com.valerij.notepad.ui.theme.components.NoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBinScreen(
    navController: NavController,
    viewModel: NotesViewModel
) {
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    val deletedNotes by viewModel.deletedNotes.collectAsState()
    var restored by remember { mutableStateOf(false) }
    var emptyTrashBin by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()



    BackHandler(enabled = selectionMode) {
        selectionMode = false
        selectedNotes.clear()
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    title = { Text(
                        text = stringResource(R.string.selected_count, selectedNotes.size),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = Typography.bodyMedium)},
                    navigationIcon = {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedNotes.clear()
                        }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(30.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            selectedNotes.toList().forEach {  noteId ->
                                viewModel.softDelete(noteId)
                            }
                            selectedNotes.clear()
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.RestorePage, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(30.dp))
                        }

                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(30.dp))
                        }
                    }
                )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp))
                    }

                    Text(
                        text = stringResource(R.string.trash),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        style = Typography.bodyLarge
                    )

                    Box {
                        IconButton(
                            onClick = {
                                menuExpanded = true
                            }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(width = 180.dp, height = 63.dp),
                            shape = RoundedCornerShape(22.dp),
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text(
                                    text = stringResource(R.string.empty_all_trash),
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary )},
                                onClick = {
                                    emptyTrashBin = true
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if(emptyTrashBin) {
                AlertDialog(
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = { emptyTrashBin = false },
                    title = { Text(
                        text = stringResource(R.string.trash_bin_delete_title),
                        color = MaterialTheme.colorScheme.primary,
                        style = Typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center)},
                    text = { Text(
                        text = stringResource(R.string.trash_bin_delete_text),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = Typography.bodyMedium)},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deletedNotes.toList().forEach {  note ->
                                    viewModel.deleteNote(note)
                                }
                                emptyTrashBin = false
                            }
                        ) { Text(
                            text = stringResource(R.string.delete_submit),
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = Typography.bodySmall)}
                    },
                    dismissButton = {
                        TextButton(onClick = { emptyTrashBin = false }) {
                            Text(
                                text = stringResource(R.string.delete_cancel),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = Typography.bodySmall)
                        }
                    }
                )
            }

            if (showDeleteDialog && selectedNotes.isNotEmpty()) {
                AlertDialog(
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(
                        text = stringResource(R.string.delete_alert_title),
                        color = MaterialTheme.colorScheme.primary,
                        style = Typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center)},
                    text = { Text(
                        text = stringResource(R.string.delete_alert_text),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = Typography.bodyMedium)},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteNotes(selectedNotes.toList())
                                selectedNotes.clear()
                                selectionMode = false
                                showDeleteDialog = false
                            }
                        ) { Text(
                            text = stringResource(R.string.delete_submit),
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = Typography.bodySmall)}
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(
                                text = stringResource(R.string.delete_cancel),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = Typography.bodySmall)
                        }
                    }
                )
            }

            noteToDelete?.let { note ->
                AlertDialog(
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = { noteToDelete = null },
                    title = { Text(
                        text = stringResource(R.string.delete_alert_title_one),
                        color = MaterialTheme.colorScheme.primary,
                        style = Typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center)},
                    text = { Text(
                        text = stringResource(R.string.delete_alert_text_one),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = Typography.bodyMedium)},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteNote(note)
                                noteToDelete = null
                            }
                        ) { Text(
                            text = stringResource(R.string.delete_submit),
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = Typography.bodySmall )}
                    },
                    dismissButton = {
                        TextButton(onClick = { noteToDelete = null }) {
                            Text(
                                text = stringResource(R.string.delete_cancel),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = Typography.bodySmall)
                        }
                    }
                )
            }

            LazyColumn {
                items(deletedNotes, key = { it.id }) { note ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { it * 0.5f },
                        confirmValueChange = { value ->
                            when (value) {

                                SwipeToDismissBoxValue.StartToEnd -> {
                                    restored = true
                                    viewModel.softDelete(note.id)
                                    false
                                }

                                SwipeToDismissBoxValue.EndToStart -> {
                                    noteToDelete = note
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
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        else -> MaterialTheme.colorScheme.primaryContainer
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