package com.valerij.notepad.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    var isLeavingScreen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current

    fun buildNote(): NoteEntity {
        val finalTitle =
            if (title.isBlank()) {
                content
                    .lineSequence()
                    .firstOrNull()
                    ?.take(30)
                    ?: "No name"
            } else title

        return NoteEntity(
            id = noteId ?: UUID.randomUUID().toString(),
            title = finalTitle,
            content = content,
            checklist = false,
            pinned = pinnedNote,
        )
    }

    fun saveAndExit() {
        if (title.isBlank() && content.isBlank()){
            navController.popBackStack()
        } else {
            scope.launch {
                viewModel.saveNote(buildNote())
                navController.popBackStack()
            }
        }
    }

    BackHandler {
        isLeavingScreen = true
        saveAndExit()
    }

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && !isLeavingScreen) {
                scope.launch {
                    viewModel.saveNote(buildNote())
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                    IconButton(onClick = {
                        isLeavingScreen = true
                        saveAndExit()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isLeavingScreen = true
                        saveAndExit()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
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
                                        if (noteId == null){
                                            isLeavingScreen = true

                                            showDeleteDialog = false
                                            navController.popBackStack()
                                        } else {
                                            isLeavingScreen = true

                                            scope.launch {
                                                viewModel.getNote(noteId!!)?.let {
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
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 10.dp)
                .fillMaxSize()
                .imePadding()
        ) {
            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit){
                        detectTapGestures {
                            focusManager.clearFocus()
                        }
                    },
                placeholder = { Text("Text here") },
                textStyle = Typography.bodySmall,
            )
        }
    }
}