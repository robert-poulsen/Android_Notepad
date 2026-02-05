package com.valerij.notepad.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.valerij.notepad.data.local.NoteEntity

@Composable
fun NoteItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
) {
    Row(){
        IconButton(
            modifier = Modifier
                .padding(top = 30.dp),
            onClick = {onPinClick()}
        ){
            Icon(
                Icons.Default.PushPin,
                contentDescription = null,
                tint = if (note.pinned)
                    MaterialTheme.colorScheme.inversePrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if(note.title.isBlank()){
                    Text(
                        note.content,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,

                        )
                } else {
                    Text(note.title, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    note.content,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}