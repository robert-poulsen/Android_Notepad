package com.valerij.notepad.ui.theme.components

import android.content.res.Resources
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valerij.notepad.data.local.NoteEntity
import com.valerij.notepad.ui.theme.Typography
import com.valerij.notepad.ui.theme.screens.ChecklistItem
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer
            else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(width = 75.dp, height = 75.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke((0.1).dp ,color = MaterialTheme.colorScheme.onPrimary)
        ) {
            val previeText = buildString { append(note.content) }

            Text(
                text = previeText,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                style = Typography.bodySmall.copy(
                    fontSize = 7.sp,
                    lineHeight = 8.sp
                ),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text =
                        if (note.title.isBlank()) {
                            note.content
                        } else {
                            note.title
                        },

                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (note.pinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .rotate(35f),
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date(note.createdAt)),

                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}