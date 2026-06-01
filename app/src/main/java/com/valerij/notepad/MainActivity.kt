package com.valerij.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notepad.ThemeViewModel
import com.valerij.notepad.ui.theme.NotepadTheme
import com.valerij.notepad.ui.theme.Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val currentTheme by themeViewModel.theme.collectAsState()

            NotepadTheme(
                darkTheme = when(currentTheme) {
                    Theme.DARK -> true
                    Theme.LIGHT -> false
                    Theme.SYSTEM -> isSystemInDarkTheme()
                },
                dynamicColor = currentTheme == Theme.SYSTEM
            ){
                NotesApp(
                    currentTheme = currentTheme,
                    onThemeChange = { themeViewModel.setTheme(it)}
                )
            }
        }
    }
}