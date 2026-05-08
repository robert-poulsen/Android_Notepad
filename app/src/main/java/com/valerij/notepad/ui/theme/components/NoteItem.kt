package com.valerij.notepad.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valerij.notepad.data.local.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    isSelected: Boolean,
    onLongClick: () -> Unit,
) {
    Row(Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
        .padding(12.dp)
        .background(if (isSelected)
            Color.LightGray
        else Color.Transparent)){
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
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (note.title.isBlank()) {
                            note.content
                        } else {
                            note.title
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                            .format(Date(note.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    note.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )

               // Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}