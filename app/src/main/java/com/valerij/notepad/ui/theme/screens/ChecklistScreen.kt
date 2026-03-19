package com.valerij.notepad.ui.theme.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Typography
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val items = remember { mutableStateListOf<ChecklistItem>() }
    var loaded by remember { mutableStateOf(false) }
    var pinnedNote by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentId = remember { noteId ?: UUID.randomUUID().toString() }
    var focusIndex by remember { mutableStateOf<Int?>(null) }

    val progress =
        if (items.isEmpty()) 0f
        else items.count { it.checked }.toFloat() / items.size

    fun buildNote(): NoteEntity {
        val finalTitle =
            if (title.isBlank())
                items.firstOrNull()?.text?.take(30) ?: "No name"
            else title

        val content = items.joinToString("\n") {
            if (it.checked) "[x] ${it.text}"
            else "[ ] ${it.text}"
        }

        return NoteEntity(
            id = currentId,
            title = finalTitle,
            content = content,
            checklist = true,
            pinned = pinnedNote
        )
    }

    fun saveAndExit() {
        if (title.isEmpty() && items.get(0).text.isEmpty()){
            navController.popBackStack()
        } else {
            scope.launch {
                viewModel.saveNote(buildNote())
                navController.popBackStack()
            }
        }
    }

    BackHandler {
        saveAndExit()
    }

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_STOP) {
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
                pinnedNote = note.pinned

                items.clear()
                note.content.split("\n").forEach {
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
                        focusIndex = items.lastIndex
                    }) {
                        Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = null)
                    }
                    IconButton(onClick = {
                        saveAndExit()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                    IconButton(onClick = {
                        showDeleteDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                    IconButton(onClick = {
                        items.sortBy {
                            it.text.toIntOrNull() ?: Int.MAX_VALUE
                            it.text.lowercase()
                        }
                    }) {
                        Icon(Icons.Default.SortByAlpha, contentDescription = null)
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
                                            showDeleteDialog = false
                                            navController.popBackStack()
                                        } else {
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
                .padding(16.dp)
                .fillMaxSize()
        ) {

            items.forEachIndexed { index, item ->
                val focusRequester = remember { FocusRequester() }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .pointerInput(items) {

                                detectDragGesturesAfterLongPress(
                                    onDrag = { change, dragAmount ->

                                        change.consume()

                                        val target =
                                            if (dragAmount.y > 0)
                                                index + 1
                                            else
                                                index - 1

                                        if (target in items.indices) {

                                            items.add(
                                                target,
                                                items.removeAt(index)
                                            )
                                        }
                                    }
                                )
                            }
                    )

                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = {
                            items[index] = item.copy(checked = !item.checked)
                            items.sortBy {
                                it.checked
                            }
                        }
                    )

                    TextField(
                        value = item.text,
                        onValueChange = { newText ->
                            if (newText.isBlank() && items.size > 1) {
                                items.removeAt(index)
                            } else {
                                items[index] = item.copy(text = newText)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned {
                                if (focusIndex == index) {
                                    focusRequester.requestFocus()
                                    focusIndex = null
                                }
                            }
                            .focusRequester(focusRequester),
                        textStyle = LocalTextStyle.current.copy(
                            textDecoration =
                                if (item.checked)
                                    TextDecoration.LineThrough
                                else
                                    TextDecoration.None,

                            color =
                                if (item.checked)
                                    Color.Gray
                                else
                                    LocalContentColor.current
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                items.add(index + 1, ChecklistItem("", false))
                                focusIndex = index + 1
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            items.removeAt(index)

                            if (items.isEmpty()) {
                                items.add(ChecklistItem("", false))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Text(
                text = "${items.count { it.checked }} / ${items.size} completed",
                style = Typography.bodySmall
            )
        }
    }
}