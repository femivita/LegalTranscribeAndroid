package com.legal.transcriber.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.shared.viewmodel.TranscriptionState
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import com.legal.transcriber.ui.theme.CardWhite
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Separator
import com.legal.transcriber.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TranscriptionViewModel,
    onNavigateToHistory: () -> Unit,
    onTranscriptionComplete: (fileName: String) -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.transcriptionState.collectAsState()
    var hasNavigated by rememberSaveable { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: viewModel.defaultFileName()
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                hasNavigated = false
                viewModel.transcribe(bytes, fileName)
            }
        }
    }

    LaunchedEffect(state) {
        if (state is TranscriptionState.Success && !viewModel.loadedFromHistory && !hasNavigated) {
            hasNavigated = true
            onTranscriptionComplete(viewModel.fileName.value)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Legal Transcriber",
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
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is TranscriptionState.Loading -> LoadingContent()
                is TranscriptionState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (state as TranscriptionState.Error).message,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        UploadCard(onPickFile = {
                            viewModel.resetTranscriptionState()
                            filePicker.launch("audio/*")
                        })
                    }
                }
                else -> UploadCard(onPickFile = { filePicker.launch("audio/*") })
            }
        }
    }
}

@Composable
private fun UploadCard(onPickFile: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 44.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Upload Audio or Video",
                    fontFamily = FontFamily.Default,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "MP3, MP4, WAV, M4A, AAC, FLAC, OGG",
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = MutedInk,
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onPickFile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                    ),
                ) {
                    Icon(Icons.Rounded.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Choose File",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Powered by AssemblyAI — automatic speaker identification and timestamps",
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            color = MutedInk,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = Gold, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Uploading and transcribing…",
            fontFamily = FontFamily.Default,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Navy,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This may take a few minutes\ndepending on audio length",
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            color = MutedInk,
            textAlign = TextAlign.Center,
        )
    }
}
