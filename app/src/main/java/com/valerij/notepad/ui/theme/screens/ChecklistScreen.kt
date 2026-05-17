package com.valerij.notepad.ui.theme.screens

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
    val listState = rememberLazyListState()
    var pinned by remember { mutableStateOf(false) }
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
                        isLeavingScreen = true
                        saveAndExit()
                    }
                ) {

                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }

                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Title")
                    },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent,

                        unfocusedContainerColor =
                            Color.Transparent,

                        focusedIndicatorColor =
                            Color.Transparent,

                        unfocusedIndicatorColor =
                            Color.Transparent
                    ),
                    singleLine = true
                )

                Box {

                    IconButton(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {

                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("Add task")
                            },
                            onClick = {

                                addTask()

                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Delete")
                            },
                            onClick = {

                                menuExpanded = false

                                showDeleteDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (pinned)
                                        "Unpin"
                                    else
                                        "Pin"
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

                        DropdownMenuItem(
                            text = {
                                Text("Save")
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
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surface
                            )
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            if (completedItems.isNotEmpty()) {

                item {

                    HorizontalDivider()

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
                            text =
                                "Completed (${completedItems.size})",
                            modifier =
                                Modifier.weight(1f)
                        )

                        Icon(
                            imageVector =
                                if (completedExpanded)
                                    Icons.Default.ExpandLess
                                else
                                    Icons.Default.ExpandMore,
                            contentDescription = null
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
                                    .background(
                                        MaterialTheme
                                            .colorScheme
                                            .surface
                                    )
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
    modifier: Modifier = Modifier
) {

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(focusItemId) {
        if (focusItemId == item.id) {

            val index = items.indexOfFirst { it.id == item.id }
            if (index != -1) {
                listState.animateScrollToItem( index = index + 1)
            }

            focusRequester.requestFocus()
            onFocusConsumed()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DragIndicator,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = dragHandleModifier
                .padding(8.dp)
        )

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
            }
        )

        TextField(
            value = item.text,

            onValueChange = { text ->

                val index = items.indexOfFirst { it.id == item.id }

                if (index == -1) {
                    return@TextField
                }

                items[index] = item.copy(text = text)
            },
            keyboardActions = KeyboardActions(
                onNext = {
                    val insertIndex =
                        items.indexOfLast { !it.checked }.let {
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
                Text("Task")
            },

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),

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
                        LocalContentColor.current),

            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        IconButton(
            onClick = {
                val index = items.indexOfFirst { it.id == item.id }

                if (index != -1) {
//                    focusManager.clearFocus(force = true)
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

            }
        ) {

            Icon(
                Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}