package com.valerij.notepad.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Typography
import kotlinx.coroutines.launch
import java.util.UUID

data class ChecklistItem(
    var text: String,
    var checked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    noteId: String?
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(mutableStateListOf<ChecklistItem>()) }
    var loaded by remember { mutableStateOf(false) }
    var pinnedNote by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun saveAndExit() {

        if (title.isEmpty() && items.get(0).text.isEmpty()){
            navController.popBackStack()
        } else {
            scope.launch {

                val finalTitle =
                    if (title.isBlank()) {
                        items.firstOrNull()?.text?.take(30) ?: "No name"
                    } else title

                val content = items.joinToString("\n") {
                    if (it.checked) "[x] ${it.text}"
                    else "[ ] ${it.text}"
                }

                viewModel.saveNote(
                    NoteEntity(
                        id = noteId ?: UUID.randomUUID().toString(),
                        title = finalTitle,
                        content = content,
                        checklist = true,
                        pinned = pinnedNote
                    )
                )

                navController.popBackStack()
            }
        }
    }

    BackHandler {
        saveAndExit()
    }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNote(noteId)?.let { note ->
                title = note.title
                pinnedNote = note.pinned

                val lines = note.content.split("\n")

                items.clear()
                lines.forEach {
                    when {
                        it.startsWith("[x] ") ->
                            items.add(ChecklistItem(it.removePrefix("[x] "), true))

                        it.startsWith("[ ] ") ->
                            items.add(ChecklistItem(it.removePrefix("[ ] "), false))
                    }
                }
            }
        }

        if (items.isEmpty()) {
            items.add(ChecklistItem("", false))
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = { Text("Title") },
                    singleLine = true,
                    textStyle = Typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )},
                navigationIcon = {
                    IconButton(onClick = { saveAndExit() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        items.add(ChecklistItem("", false))
                    }) {
                        Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = null)
                    }
                    IconButton(onClick = {
                        saveAndExit()
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
                .padding(16.dp)
                .fillMaxSize()
        ) {

            items.forEachIndexed { index, item ->

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = {

                            items[index] =
                                item.copy(checked = !item.checked)
                        }
                    )

                    TextField(
                        value = item.text,
                        onValueChange = { newText ->
                            items[index] =
                                item.copy(text = newText)
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            textDecoration =
                                if (item.checked)
                                    TextDecoration.LineThrough
                                else
                                    TextDecoration.None
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(

                            onNext = {
                                items.add(index + 1,
                                    ChecklistItem("", false))
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}