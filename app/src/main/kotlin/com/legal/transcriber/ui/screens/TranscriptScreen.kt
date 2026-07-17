package com.legal.transcriber.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.shared.models.AnalysisResult
import com.legal.transcriber.shared.viewmodel.AnalysisState
import com.legal.transcriber.shared.viewmodel.ExportState
import com.legal.transcriber.shared.viewmodel.SaveState
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.Ink
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Parchment
import com.legal.transcriber.ui.theme.Separator
import com.legal.transcriber.ui.theme.White
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptScreen(
    viewModel: TranscriptionViewModel,
    fileName: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val exportState by viewModel.exportState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    val segments = viewModel.segments
    var title by remember { mutableStateOf(viewModel.derivedTitle()) }

    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                val success = exportState as ExportState.Success
                val exportName = viewModel.exportFileName(title, success.format)
                saveFileAndOpen(context, success.bytes, exportName)
                viewModel.resetExportState()
            }
            is ExportState.Error -> {
                Toast.makeText(context, (exportState as ExportState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetExportState()
            }
            else -> {}
        }
    }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
            }
            is SaveState.Error -> {
                Toast.makeText(context, (saveState as SaveState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Transcript",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Navy,
                    navigationIconContentColor = Navy,
                ),
                actions = {
                    if (saveState is SaveState.Loading) {
                        CircularProgressIndicator(
                            color = Gold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(end = 16.dp).size(22.dp),
                        )
                    } else {
                        TextButton(onClick = {
                            viewModel.saveToHistory(title, fileName, segments)
                        }) {
                            Text("Save", fontFamily = FontFamily.Default, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gold)
                        }
                    }
                },
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title", fontFamily = FontFamily.Default, color = MutedInk) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportToDocx(title, segments) },
                            enabled = exportState !is ExportState.Loading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        ) {
                            Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DOCX", fontFamily = FontFamily.Default, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.exportToPdf(title, segments) },
                            enabled = exportState !is ExportState.Loading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontFamily = FontFamily.Default, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { viewModel.analyze(title, segments) },
                            enabled = segments.isNotEmpty() && analysisState !is AnalysisState.Loading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze", fontFamily = FontFamily.Default, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = White)
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = Separator.copy(alpha = 0.5f))

                // Segments list
                if (segments.isEmpty()) {
                    Text(
                        "No transcript content found.",
                        fontFamily = FontFamily.Default,
                        color = MutedInk,
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (analysisState is AnalysisState.Loading) {
                            item { AnalysisLoadingView() }
                        } else if (analysisState is AnalysisState.Success) {
                            item { AnalysisResultView(result = (analysisState as AnalysisState.Success).result) }
                        } else if (analysisState is AnalysisState.Error) {
                            item {
                                AnalysisErrorView(
                                    message = (analysisState as AnalysisState.Error).message,
                                    onRetry = { viewModel.analyze(title, segments) }
                                )
                            }
                        }

                        items(segments) { seg ->
                            SegmentCard(seg)
                        }
                    }
                }
            }

            // Export overlay
            if (exportState is ExportState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(White)
                            .padding(24.dp),
                    ) {
                        CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Exporting...", fontFamily = FontFamily.Default, fontSize = 14.sp, color = MutedInk)
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentCard(seg: com.legal.transcriber.shared.models.Segment) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    seg.speaker,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Navy,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    seg.timestamp,
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = MutedInk,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                seg.text,
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                color = Ink,
                lineHeight = 21.sp,
            )
        }
    }
}

@Composable
private fun AnalysisLoadingView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Parchment)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Analyzing transcript...",
            fontFamily = FontFamily.Default,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Navy,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Generating summary and action notes",
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            color = MutedInk,
        )
    }
}

@Composable
private fun AnalysisResultView(result: AnalysisResult) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Parchment)
            .animateContentSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Case Summary & Action Notes",
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Navy,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MutedInk,
                modifier = Modifier.size(18.dp),
            )
        }

        if (isExpanded) {
            if (result.summary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Summary",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    result.summary,
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = Ink,
                    lineHeight = 20.sp,
                )
            }

            if (result.actionNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Separator.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Action Notes",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                result.actionNotes.forEachIndexed { index, note ->
                    Row(modifier = Modifier.padding(top = if (index > 0) 6.dp else 0.dp)) {
                        Text(
                            "${index + 1}.",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            note,
                            fontFamily = FontFamily.Default,
                            fontSize = 14.sp,
                            color = Ink,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Parchment)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            message,
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            color = Color.Red,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text("Retry", fontFamily = FontFamily.Default, fontSize = 13.sp, color = Gold)
        }
    }
}

private fun saveFileAndOpen(context: Context, bytes: ByteArray, fileName: String) {
    val dir = context.filesDir
    val file = File(dir, fileName)
    file.writeBytes(bytes)

    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )

    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = when {
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "*/*"
        }
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        android.content.Intent.createChooser(shareIntent, "Share $fileName")
    )
}
