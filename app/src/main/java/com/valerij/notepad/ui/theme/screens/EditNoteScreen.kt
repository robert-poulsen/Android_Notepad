package com.valerij.notepad.ui.theme.screens

import com.valerij.notepad.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var menuExpanded by remember { mutableStateOf(false) }
    var pinned by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val textStyleMedium = MaterialTheme.typography.bodyMedium
    val textStyleLarge = MaterialTheme.typography.bodyLarge
    val keyboardController = LocalSoftwareKeyboardController.current

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
            pinned = pinned,
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
            if (event == Lifecycle.Event.ON_STOP && !isLeavingScreen && (title.isNotBlank() && content.isNotBlank())) {
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

    LaunchedEffect(keyboardVisible) {
        if (!keyboardVisible) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNote(noteId)?.let { note ->
                title = note.title
                content = note.content
                pinned = note.pinned
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(72.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if(keyboardVisible){
                            keyboardController?.hide()
                        } else {
                            isLeavingScreen = true
                            saveAndExit()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null,
                        tint = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(30.dp))
                }

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.note_title),
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            lineHeight = 45.sp
                        )},
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = MaterialTheme.colorScheme.background,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.background
                    ),
                    textStyle = Typography.bodyLarge,
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
                            tint = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(width = 155.dp, height = 183.dp),
                        shape = RoundedCornerShape(22.dp),
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.button_delete),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodySmall)
                            },
                            onClick = {

                                menuExpanded = false

                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
                        Spacer(modifier = Modifier.height(6.dp))
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (pinned)
                                        stringResource(R.string.button_unpin)
                                    else
                                        stringResource(R.string.button_pin),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodySmall
                                )
                            },
                            onClick = {

                                pinned = !pinned

                                scope.launch {
                                    viewModel.togglePin(
                                        buildNote().id
                                    )
                                }

                                menuExpanded = false
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
                        Spacer(modifier = Modifier.height(6.dp))
                        DropdownMenuItem(
                            text = {
                                Text(text = stringResource(R.string.button_save),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodySmall)
                            },
                            onClick = {
                                isLeavingScreen = true
                                saveAndExit()

                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = {
                        showDeleteDialog = false
                    },
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
                        ) { Text(
                            text = stringResource(R.string.delete_submit),
                            color = MaterialTheme.colorScheme.onTertiary,
                            style = Typography.bodySmall)}
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.delete_cancel),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = Typography.bodySmall)
                        }
                    }
                )
            }
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
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text(
                    text = stringResource(R.string.note_text),
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                ) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.background
                ),
                textStyle = Typography.bodyMedium,
            )
        }
    }
}