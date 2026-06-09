package com.valerij.notepad

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.NotesViewModelFactory
import com.valerij.notepad.ui.theme.Theme
import com.valerij.notepad.ui.theme.screens.ChecklistScreen
import com.valerij.notepad.ui.theme.screens.EditNoteScreen
import com.valerij.notepad.ui.theme.screens.HomeScreen
import com.valerij.notepad.ui.theme.screens.TrashBinScreen

@Composable
fun NotesApp(
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: NotesViewModel = viewModel(
        factory = NotesViewModelFactory(LocalContext.current)
    )

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController, viewModel, currentTheme, onThemeChange)
        }

        composable("trashBin") {
            TrashBinScreen(navController, viewModel)
        }

        composable("editNoteScreen?noteId={noteId}") { backStack ->
            val noteId = backStack.arguments?.getString("noteId")
            EditNoteScreen(navController, viewModel, noteId)
        }

        composable("checklistScreen?noteId={noteId}") { backStack ->
            val noteId = backStack.arguments?.getString("noteId")
            ChecklistScreen(navController, viewModel, noteId)
        }
    }
}
