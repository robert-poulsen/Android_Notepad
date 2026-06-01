package com.valerij.notepad.ui.theme.screens

import com.valerij.notepad.R
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.NotepadTheme

import com.valerij.notepad.ui.theme.components.NoteItem
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.Theme
import com.valerij.notepad.ui.theme.Typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NotesViewModel,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var isLeavingScreen by remember { mutableStateOf(false) }
    var tempTheme by remember { mutableStateOf(currentTheme) }
    var selectedSort by remember { mutableStateOf(NotesViewModel.SortType.DATE_DESC) }
    var tempSort by remember { mutableStateOf(selectedSort) }
    var pendingPinId by remember { mutableStateOf<String?>(null) }
    val sortType by viewModel.sortType.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val rotation by animateFloatAsState(targetValue = if (expanded) -45f else 0f, label = "")
    val noteOffsetX by animateDpAsState(targetValue = if (expanded) (-10).dp else 0.dp, label = "")
    val noteOffsetY by animateDpAsState(targetValue = if (expanded) (-80).dp else 0.dp, label = "")
    val checklistOffsetX by animateDpAsState(targetValue = if (expanded) (-80).dp else 0.dp, label = "")
    val checklistOffsetY by animateDpAsState(targetValue = if (expanded) (-10).dp else 0.dp, label = "")
    var menuExpanded by remember { mutableStateOf(false) }

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

    LaunchedEffect(keyboardVisible) {
        if (!keyboardVisible) {
            focusManager.clearFocus()
        }
    }

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
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(30.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            selectedNotes.toList().forEach {  noteId ->
                                viewModel.togglePin(noteId)
                            }
                            selectedNotes.clear()
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(30.dp))
                        }

                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(30.dp))
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
                    androidx.compose.animation.AnimatedVisibility (
                        visible = expanded,

                        enter = fadeIn() + scaleIn(
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = 500f
                            )
                        ),

                        exit = fadeOut() + scaleOut()
                    ) {

                        FloatingActionButton(
                            onClick = {
                                expanded = false
                                navController.navigate("editNoteScreen")
                            },

                            modifier = Modifier.offset(
                                x = noteOffsetX,
                                y = noteOffsetY)
                                .size(65.dp),

                            shape = CircleShape,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary,
                        ) {
                            Icon(Icons.Default.Notes, null)
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn() + scaleIn(
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = 500f
                            )
                        ),
                        exit = fadeOut() + scaleOut()
                    ) {

                        FloatingActionButton(
                            onClick = {
                                expanded = false
                                navController.navigate("checklistScreen")
                            },

                            modifier = Modifier.offset(
                                x = checklistOffsetX,
                                y = checklistOffsetY)
                                .size(65.dp),

                            shape = CircleShape,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary,
                        ) {
                            Icon(Icons.Default.Checklist, null)
                        }
                    }
                    FloatingActionButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(65.dp),
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp).rotate(rotation)
                        )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearch,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = Typography.bodyLarge.copy(
                            lineHeight = 40.sp
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search),
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )},
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                null,
                                Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )},
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedIndicatorColor = MaterialTheme.colorScheme.background,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.background
                        ),
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(width = 155.dp, height = 178.dp),
                            shape = RoundedCornerShape(22.dp),
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text(
                                    text = stringResource(R.string.sort_by),
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary )},
                                onClick = {
                                    showSortDialog = true
                                    menuExpanded = false
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            DropdownMenuItem(
                                text = { Text(
                                    text = stringResource(R.string.trash),
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary )},
                                onClick = {
                                    menuExpanded = false
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            DropdownMenuItem(
                                text = { Text(
                                    text = stringResource(R.string.appearance),
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary )},
                                onClick = {
                                    showAppearanceDialog = true
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showSortDialog) {
                AlertDialog(
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = { showSortDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.sort_by),
                            color = MaterialTheme.colorScheme.primary,
                            style = Typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center)
                    },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSort = NotesViewModel.SortType.DATE_ASC
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempSort == NotesViewModel.SortType.DATE_ASC,
                                    onClick = {
                                        tempSort = NotesViewModel.SortType.DATE_ASC
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.sort_date_old),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSort = NotesViewModel.SortType.DATE_DESC
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempSort == NotesViewModel.SortType.DATE_DESC,
                                    onClick = {
                                        tempSort = NotesViewModel.SortType.DATE_DESC
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.sort_date_new),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSort = NotesViewModel.SortType.TITLE_ASC
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempSort == NotesViewModel.SortType.TITLE_ASC,
                                    onClick = {
                                        tempSort = NotesViewModel.SortType.TITLE_ASC
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.sort_A_Z),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSort = NotesViewModel.SortType.TITLE_DESC
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempSort == NotesViewModel.SortType.TITLE_DESC,
                                    onClick = {
                                        tempSort = NotesViewModel.SortType.TITLE_DESC
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.sort_Z_A),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium)
                            }
                        }
                    },

                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedSort = tempSort
                                viewModel.sortNotes(selectedSort)
                                showSortDialog = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.submit),
                                color = MaterialTheme.colorScheme.onTertiary,
                                style = Typography.bodySmall)
                        }
                    },

                    dismissButton = {
                        TextButton(
                            onClick = {
                                tempSort = selectedSort
                                showSortDialog = false
                            }
                        ) {
                            Text(text = stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = Typography.bodySmall)
                        }
                    }
                )
            }

            if (showAppearanceDialog) {
                AlertDialog(
                    shape = RoundedCornerShape(30.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onDismissRequest = { showAppearanceDialog = false },
                    title = { Text(
                        text = stringResource(R.string.select_theme_title),
                        color = MaterialTheme.colorScheme.primary,
                        style = Typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center)},
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempTheme = Theme.DARK
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempTheme == Theme.DARK,
                                    onClick = {
                                        tempTheme = Theme.DARK
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.select_theme_dark),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempTheme = Theme.LIGHT
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempTheme == Theme.LIGHT,
                                    onClick = {
                                        tempTheme = Theme.LIGHT
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.select_theme_light),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempTheme = Theme.SYSTEM
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onTertiary,
                                        unselectedColor = MaterialTheme.colorScheme.tertiary
                                    ),
                                    selected = tempTheme == Theme.SYSTEM,
                                    onClick = {
                                        tempTheme = Theme.SYSTEM
                                    }
                                )
                                Text(
                                    text = stringResource(R.string.select_theme_system),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = Typography.bodyMedium
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onThemeChange(tempTheme)
                                showAppearanceDialog = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.submit),
                                color = MaterialTheme.colorScheme.onTertiary,
                                style = Typography.bodySmall)
                        }
                    },

                    dismissButton = {
                        TextButton(
                            onClick = {
                                tempTheme = currentTheme
                                showAppearanceDialog = false
                            }
                        ) {
                            Text(text = stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.onPrimary,
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
                                color = MaterialTheme.colorScheme.onPrimary,
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
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = Typography.bodySmall)
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
                                    pendingPinId = note.id
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
