package com.valerij.notepad.ui.theme.screens

import com.valerij.notepad.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Typography
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var checked: Boolean
)

@OptIn(
    ExperimentalMaterial3Api::class
)
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
    var completedExpanded by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }
    var focusItemId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLeavingScreen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    var pinned by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val reorderableLazyListState =
        rememberReorderableLazyListState(
            lazyListState = listState,
            onMove = { from, to ->
                val fromItemId = from.key as String
                val toItemId = to.key as String
                val fromItem = items.first { it.id == fromItemId }
                val toItem = items.first { it.id == toItemId }

                if (fromItem.checked != toItem.checked) {
                    return@rememberReorderableLazyListState
                }

                val fromIndex = items.indexOfFirst { it.id == fromItem.id }
                val toIndex = items.indexOfFirst { it.id == toItem.id }

                if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyListState

                items.add(
                    toIndex,
                    items.removeAt(fromIndex)
                )
            }
        )

    val activeItems by remember { derivedStateOf { items.filter { !it.checked } } }
    val completedItems by remember { derivedStateOf { items.filter { it.checked } } }
    val progress =
        if (items.isEmpty()) 0f
        else {
            items.count { it.checked }
                .toFloat() / items.size
        }

    fun addTask() {

        val insertIndex =
            items.indexOfLast {
                !it.checked
            }.let {

                if (it == -1)
                    0
                else
                    it + 1
            }

        val newItem =
            ChecklistItem(
                text = "",
                checked = false
            )

        items.add(
            insertIndex,
            newItem
        )

        focusItemId = newItem.id
    }

    fun buildNote(): NoteEntity {

        val orderedItems = activeItems + completedItems

        val content =
            orderedItems.joinToString("\n") {

                if (it.checked)
                    "☑ ${it.text}"
                else
                    "☐ ${it.text}"
            }

        return NoteEntity(
            id = noteId
                ?: UUID.randomUUID().toString(),
            title = title.ifBlank {
                orderedItems.firstOrNull()?.text
                    ?: "No title"
            },
            content = content,
            deleted = deleted,
            pinned = pinned,
            checklist = true
        )
    }

    fun saveAndExit() {
        if (title.isBlank() && items.all {it.text.isBlank()}){
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
            if (event == Lifecycle.Event.ON_STOP && !isLeavingScreen && (title.isNotBlank() && items.all {it.text.isNotBlank()})) {
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

            viewModel.getNote(noteId)
                ?.let { note ->

                    title = note.title

                    pinned = note.pinned

                    deleted = note.deleted

                    items.clear()

                    note.content
                        .split("\n")
                        .forEach {

                            when {

                                it.startsWith("☑ ") -> {

                                    items.add(
                                        ChecklistItem(
                                            text = it.removePrefix("☑ "),
                                            checked = true
                                        )
                                    )
                                }

                                it.startsWith("☐ ") -> {

                                    items.add(
                                        ChecklistItem(
                                            text = it.removePrefix("☐ "),
                                            checked = false
                                        )
                                    )
                                }
                            }
                        }
                }
        }

        if (items.isEmpty()) {

            items.add(
                ChecklistItem(
                    text = "",
                    checked = false
                )
            )
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(bottom = 450.dp)
                        .size(width = 300.dp, height = 80.dp)
                        .statusBarsPadding(),
                    shape = RoundedCornerShape(80.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = stringResource(R.string.note_restored),
                        style = Typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.note_title),
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            lineHeight = 45.sp
                        )},
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    enabled = !deleted
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
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    if(!deleted) {
                        DropdownMenu(
                            expanded = menuExpanded,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(width = 155.dp, height = 246.dp),
                            shape = RoundedCornerShape(22.dp),
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.button_new_task),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = Typography.bodySmall
                                    )
                                },
                                onClick = {
                                    addTask()
                                    menuExpanded = false
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
                            Spacer(modifier = Modifier.height(6.dp))
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.button_delete),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = Typography.bodySmall
                                    )
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
                                    Text(
                                        text = stringResource(R.string.button_save),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = Typography.bodySmall
                                    )
                                },
                                onClick = {
                                    isLeavingScreen = true
                                    saveAndExit()

                                    menuExpanded = false
                                }
                            )
                        }
                    }
                    if(deleted){
                        DropdownMenu(
                            expanded = menuExpanded,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(width = 155.dp, height = 126.dp),
                            shape = RoundedCornerShape(22.dp),
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.delete_forever_note),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = Typography.bodySmall
                                    )
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
                                        text = stringResource(R.string.restore_note),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = Typography.bodySmall
                                    )
                                },
                                onClick = {
                                    deleted = false
                                    menuExpanded = false

                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
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
                                } else if(deleted){
                                    isLeavingScreen = true

                                    scope.launch {
                                        viewModel.getNote(noteId!!)?.let {
                                            viewModel.deleteNote(it)
                                        }
                                        showDeleteDialog = false
                                        navController.popBackStack()
                                    }
                                } else {
                                    isLeavingScreen = true
                                    deleted = true
                                    showDeleteDialog = false
                                    saveAndExit()
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.delete_submit),
                                color = MaterialTheme.colorScheme.onTertiary,
                                style = Typography.bodySmall)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.delete_cancel),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = Typography.bodySmall)
                        }
                    }
                )
            }
        }

    ) { padding ->

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            reverseLayout = false
        ) {

            item {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }

            items(
                items = activeItems,
                key = { it.id }
            ) { item ->

                ReorderableItem(
                    state = reorderableLazyListState,
                    key = item.id
                ) { isDragging ->

                    ChecklistRow(
                        item = item,
                        items = items,
                        focusItemId = focusItemId,
                        focusManager = focusManager,
                        dragHandleModifier = Modifier.draggableHandle(),
                        onFocusItemChange = {
                            focusItemId = it
                        },
                        onFocusConsumed = {
                            focusItemId = null
                        },
                        listState = listState,
                        deleted = deleted,
                        modifier = Modifier
                            .animateItem()
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(
                                elevation =
                                    if (isDragging)
                                        8.dp
                                    else
                                        0.dp
                            )
                            .background(Color.Transparent)
                    )
                }
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            if (completedItems.isNotEmpty()) {

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                completedExpanded =
                                    !completedExpanded
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = "${stringResource(R.string.completed_count)} (${completedItems.size})",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = Typography.bodyLarge
                        )
                        Icon(
                            imageVector =
                                if (completedExpanded)
                                    Icons.Default.ExpandLess
                                else
                                    Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                if (completedExpanded) {
                    items(
                        items = completedItems,
                        key = { it.id }
                    ) { item ->

                        ReorderableItem(
                            state = reorderableLazyListState,
                            key = item.id
                        ) { isDragging ->

                            ChecklistRow(
                                item = item,
                                items = items,
                                focusItemId = focusItemId,
                                focusManager = focusManager,
                                dragHandleModifier = Modifier.draggableHandle(),
                                onFocusItemChange = {
                                    focusItemId = it
                                },
                                onFocusConsumed = {
                                    focusItemId = null
                                },
                                listState = listState,
                                deleted = deleted,
                                modifier = Modifier
                                    .animateItem()
                                    .clip(RoundedCornerShape(16.dp))
                                    .shadow(
                                        elevation =
                                            if (isDragging)
                                                8.dp
                                            else
                                                0.dp
                                    )
                                    .background(Color.Transparent)
                            )
                        }
                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistRow(
    item: ChecklistItem,
    items: SnapshotStateList<ChecklistItem>,
    listState: LazyListState,
    focusItemId: String?,
    onFocusItemChange: (String?) -> Unit,
    onFocusConsumed: () -> Unit,
    focusManager: FocusManager,
    dragHandleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    deleted: Boolean
) {

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var textFieldValue by remember(item.id) {
        mutableStateOf(
            TextFieldValue(
                text = item.text,
                selection = TextRange(item.text.length)
            )
        )
    }

    LaunchedEffect(focusItemId) {
        if (focusItemId == item.id) {

            val index = items.indexOfFirst { it.id == item.id }
            if (index != -1) {
                listState.scrollToItem(index + 1)
            }

            focusRequester.requestFocus()
            onFocusConsumed()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(!deleted) {
            Icon(
                Icons.Default.DragIndicator,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = dragHandleModifier
                    .padding(8.dp)
            )
        }

        Checkbox(
            checked = item.checked,
            onCheckedChange = {
                val index = items.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    items[index] =
                        item.copy(
                            checked = !item.checked
                        )
                }
            },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.onTertiary,
                uncheckedColor = MaterialTheme.colorScheme.onTertiary,
                checkmarkColor = MaterialTheme.colorScheme.tertiary),
            enabled = !deleted
        )

        TextField(
            value = textFieldValue,

            onValueChange = { value ->
                textFieldValue = value
                val index = items.indexOfFirst { it.id == item.id }
                if (index == -1) return@TextField
                items[index] = item.copy(
                    text = value.text
                )
            },
            keyboardActions = KeyboardActions(
                onNext = {

                    val currentIndex =
                        items.indexOfFirst {
                            it.id == item.id
                        }

                    if (currentIndex == -1) return@KeyboardActions

                    val newItem =
                        ChecklistItem(
                            text = "",
                            checked = false
                        )

                    items.add(
                        currentIndex + 1,
                        newItem
                    )

                    onFocusItemChange(newItem.id)
                }
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->

                    if (
                        event.type == KeyEventType.KeyDown &&
                        event.key == Key.Backspace &&
                        item.text.isEmpty()
                    ) {

                        val index =
                            items.indexOfFirst {
                                it.id == item.id
                            }

                        if (
                            index != -1 &&
                            items.size > 1
                        ) {

                            val previousIndex =
                                (index - 1)
                                    .coerceAtLeast(0)

                            val previousItemId =
                                items[previousIndex].id

                            onFocusItemChange(previousItemId)

                            scope.launch {

                                delay(50)

                                items.removeAt(index)
                            }
                        }

                        true
                    } else {
                        false
                    }
                },

            placeholder = {
                Text(
                    text = stringResource(R.string.note_task),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = Typography.bodyMedium)
            },

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),

            textStyle = LocalTextStyle.current.copy(
                textDecoration =
                    if (item.checked)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None,

                color =
                    if (item.checked)
                        MaterialTheme.colorScheme.primaryContainer
                    else if(deleted)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.primary),

            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            enabled = !deleted
        )

        IconButton(
            onClick = {
                val index = items.indexOfFirst { it.id == item.id }

                if (index != -1) {
                    items.removeAt(index)

                    if (items.isEmpty()) {
                        items.add(
                            ChecklistItem(
                                text = "",
                                checked = false
                            )
                        )
                    }
                }

            },
            enabled = !deleted
        ) {

            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}