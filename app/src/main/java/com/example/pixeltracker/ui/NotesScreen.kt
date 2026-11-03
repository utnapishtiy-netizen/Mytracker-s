package com.example.pixeltracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixeltracker.TrackerViewModel

@Composable
fun NotesScreen(
    viewModel: TrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val notesList by viewModel.notes.collectAsState(initial = emptyList())
    var noteInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ЗАМЕТКИ",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Новая заметка...", color = Color.DarkGray) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White)
                    .clickable {
                        if (noteInput.isNotBlank()) {
                            viewModel.addNote(noteInput)
                            noteInput = ""
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(text = "ДОБАВИТЬ", color = Color.White, fontSize = 12.sp)
            }
        }

        if (notesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Нет заметок", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notesList) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.3f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = note.text, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(
                            text = "[Удалить]",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { viewModel.deleteNote(note.id) }
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
