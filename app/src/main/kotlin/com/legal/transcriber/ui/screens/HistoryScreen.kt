package com.legal.transcriber.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.shared.models.TranscriptHistory
import com.legal.transcriber.shared.viewmodel.HistoryState
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Separator
import com.legal.transcriber.ui.theme.White
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: TranscriptionViewModel,
    onOpenTranscript: (TranscriptHistory) -> Unit,
) {
    val context = LocalContext.current
    val historyState by viewModel.historyState.collectAsState()
    var deleteTarget by remember { mutableStateOf<TranscriptHistory?>(null) }

    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState(0, 0)
    }

    LaunchedEffect(Unit) {
        if (historyState is HistoryState.Loading) {
            viewModel.loadHistory()
        }
    }

    LaunchedEffect(historyState) {
        if (historyState is HistoryState.Error) {
            Toast.makeText(
                context,
                (historyState as HistoryState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "History",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Navy,
                ),
                windowInsets = TopAppBarDefaults.windowInsets,
            )
        },
        containerColor = Cream,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = historyState) {
                is HistoryState.Loading -> {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HistoryState.Success -> {
                    if (state.items.isEmpty()) {
                        EmptyHistory()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.items) { item ->
                                HistoryCard(
                                    item = item,
                                    formattedDate = viewModel.formattedDate(item.createdAt),
                                    onOpen = { onOpenTranscript(item) },
                                    onOpenPdf = {
                                        item.pdfStoragePath?.let { path ->
                                            viewModel.downloadPdf(path) { bytes ->
                                                bytes?.let {
                                                    val exportName = viewModel.exportFileName(item.title, "pdf")
                                                    saveFileAndOpen(context, it, exportName)
                                                }
                                            }
                                        }
                                    },
                                    onDelete = { deleteTarget = item },
                                )
                            }
                        }
                    }
                }
                is HistoryState.Error -> {
                    Text(
                        state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Transcript", fontFamily = FontFamily.Default) },
            text = { Text("Are you sure you want to delete this transcript?", fontFamily = FontFamily.Default) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHistory(target)
                    deleteTarget = null
                }) {
                    Text("Delete", color = Color.Red, fontFamily = FontFamily.Default)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", fontFamily = FontFamily.Default)
                }
            }
        )
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.History,
            contentDescription = null,
            tint = Separator,
            modifier = Modifier.size(56.dp).padding(bottom = 16.dp)
        )
        Text(
            "No history yet",
            fontFamily = FontFamily.Default,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MutedInk,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Transcripts you save will appear here",
            fontFamily = FontFamily.Default,
            fontSize = 14.sp,
            color = MutedInk,
        )
    }
}

@Composable
private fun HistoryCard(
    item: TranscriptHistory,
    formattedDate: String,
    onOpen: () -> Unit,
    onOpenPdf: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.title,
                    fontFamily = FontFamily.Default,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    formattedDate,
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = MutedInk,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.fileName,
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = MutedInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Gold.copy(alpha = 0.08f)
                    ),
                ) {
                    Text(
                        "${item.segments.size} segments",
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (item.pdfStoragePath != null) {
                    IconButton(onClick = onOpenPdf) {
                        Icon(
                            Icons.Rounded.PictureAsPdf,
                            contentDescription = "Open PDF",
                            tint = Navy,
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                    )
                }
            }
        }
    }
}

private fun saveFileAndOpen(context: Context, bytes: ByteArray, fileName: String) {
    val dir = context.filesDir
    val file = File(dir, fileName)
    file.writeBytes(bytes)
}
