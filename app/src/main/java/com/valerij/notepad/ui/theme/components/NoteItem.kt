package com.valerij.notepad.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valerij.notepad.data.Note

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if(note.title == ""){
                Text(
                    note.content,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                note.content,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}