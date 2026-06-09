package com.example.notepad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valerij.notepad.ui.theme.Theme
import com.valerij.notepad.ui.theme.components.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = ThemePreferences(application)

    private val _theme = MutableStateFlow(Theme.SYSTEM)
    val theme: StateFlow<Theme> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.themeFlow.collect { value ->
                _theme.value = Theme.valueOf(value)
            }
        }
    }

    fun setTheme(theme: Theme) {
        _theme.value = theme
        viewModelScope.launch {
            preferences.saveTheme(theme.name)
        }
    }
}