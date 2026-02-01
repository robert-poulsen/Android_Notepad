package com.valerij.notepad.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.valerij.notepad.ui.theme.NotesViewModel
import com.valerij.notepad.ui.theme.components.NoteItem

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: NotesViewModel
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("edit") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::updateSearch,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Пошук за назвою") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(viewModel.filteredNotes()) { note ->
                    NoteItem(note) {
                        navController.navigate("edit?noteId=${note.id}")
                    }
                }
            }
        }
    }
}