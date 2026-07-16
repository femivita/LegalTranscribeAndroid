package com.legal.transcriber.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.shared.models.ChatMessage
import com.legal.transcriber.shared.models.ChatSession
import com.legal.transcriber.shared.viewmodel.ChatHistoryState
import com.legal.transcriber.shared.viewmodel.ChatState
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.Ink
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Parchment
import com.legal.transcriber.ui.theme.Separator
import com.legal.transcriber.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: TranscriptionViewModel,
    onOpenConversation: () -> Unit,
) {
    val chatHistoryState by viewModel.chatHistoryState.collectAsState()

    LaunchedEffect(Unit) {
        if (chatHistoryState is ChatHistoryState.Loading) {
            viewModel.loadChatHistory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AI Assistant",
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
                actions = {
                    IconButton(onClick = {
                        viewModel.startNewChat()
                        onOpenConversation()
                    }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "New Chat", tint = Gold)
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
            when (val state = chatHistoryState) {
                is ChatHistoryState.Loading -> {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ChatHistoryState.Success -> {
                    if (state.sessions.isEmpty()) {
                        ChatEmptyView {
                            viewModel.startNewChat()
                            onOpenConversation()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            itemsIndexed(state.sessions) { index, session ->
                                ChatListItem(
                                    session = session,
                                    onOpen = {
                                        viewModel.loadChatSession(session)
                                        onOpenConversation()
                                    },
                                    onDelete = { viewModel.deleteChatSession(session) },
                                )
                                if (index < state.sessions.lastIndex) {
                                    HorizontalDivider(
                                        color = Separator.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    ChatEmptyView {
                        viewModel.startNewChat()
                        onOpenConversation()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyView(onNewChat: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.Chat,
            contentDescription = null,
            tint = Separator,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "AI Legal Assistant",
            fontFamily = FontFamily.Default,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Navy,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Ask me to redraft, summarize, or clarify anything from your transcript. I understand Nigerian Pidgin English too.",
            fontFamily = FontFamily.Default,
            fontSize = 14.sp,
            color = MutedInk,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNewChat,
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start New Chat", fontFamily = FontFamily.Default, color = White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ChatListItem(
    session: ChatSession,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                session.title,
                fontFamily = FontFamily.Default,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                formatChatTime(session.createdAt),
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = MutedInk,
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        session.messages.lastOrNull()?.let { lastMsg ->
            Text(
                stripMarkdown(lastMsg.content),
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = MutedInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    viewModel: TranscriptionViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val chatState by viewModel.chatState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var pickedFileName by remember { mutableStateOf("") }
    var pickedFileBytes by remember { mutableStateOf<ByteArray?>(null) }

    val listState = rememberLazyListState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "file"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                pickedFileBytes = bytes
                pickedFileName = fileName
            }
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    LaunchedEffect(chatState) {
        if (chatState is ChatState.Loading && chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Chat",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                    titleContentColor = Navy,
                    navigationIconContentColor = Navy,
                ),
                actions = {
                    if (chatMessages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Clear",
                                tint = MutedInk,
                            )
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets,
            )
        },
        containerColor = Cream,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White),
            ) {
                HorizontalDivider(thickness = 0.5.dp, color = Separator)
                if (pickedFileName.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Parchment)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = Gold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            pickedFileName,
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            color = Navy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            pickedFileBytes = null
                            pickedFileName = ""
                        }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Remove",
                                tint = MutedInk,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    IconButton(
                        onClick = { filePicker.launch("*/*") },
                        enabled = chatState !is ChatState.Loading,
                    ) {
                        Icon(
                            Icons.Rounded.AttachFile,
                            contentDescription = "Attach",
                            tint = Navy,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about your transcript...", fontFamily = FontFamily.Default, color = MutedInk) },
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = Separator,
                            unfocusedBorderColor = Separator,
                        ),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = inputText.isNotBlank() && chatState !is ChatState.Loading
                    IconButton(
                        onClick = {
                            val trimmed = inputText.trim()
                            if (pickedFileBytes != null && pickedFileName.isNotEmpty()) {
                                viewModel.sendChatMessageWithFile(trimmed, pickedFileBytes!!, pickedFileName)
                                pickedFileBytes = null
                                pickedFileName = ""
                                inputText = ""
                            } else if (trimmed.isNotEmpty()) {
                                viewModel.sendChatMessage(trimmed)
                                inputText = ""
                            }
                        },
                        enabled = canSend,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = if (canSend) Gold else Separator,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (chatMessages.isEmpty()) {
            ChatWelcomeView(onSuggestion = { suggestion ->
                viewModel.sendChatMessage(suggestion)
            })
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(chatMessages) { message ->
                    ChatBubble(message)
                }
                if (chatState is ChatState.Loading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(White)
                                    .border(0.5.dp, Separator, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    color = Gold,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Thinking...", fontFamily = FontFamily.Default, fontSize = 13.sp, color = MutedInk)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatWelcomeView(onSuggestion: (String) -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.Chat,
            contentDescription = null,
            tint = Separator,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "AI Legal Assistant",
            fontFamily = FontFamily.Default,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Navy,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Ask me to redraft, summarize, or clarify anything from your transcript.",
            fontFamily = FontFamily.Default,
            fontSize = 14.sp,
            color = MutedInk,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SuggestionChip("Redraft this transcript in formal legal language", onClick = onSuggestion)
            SuggestionChip("What are the key legal issues in this transcript?", onClick = onSuggestion)
            SuggestionChip("Summarize this transcript in 3 bullet points", onClick = onSuggestion)
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Parchment)
            .border(0.5.dp, Separator, RoundedCornerShape(8.dp))
            .clickable { onClick(text) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            color = Navy,
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    val attachmentFileName: String? = if (message.content.startsWith("\uD83D\uDCCE")) {
        message.content.substringAfter("\uD83D\uDCCE").substringBefore("\n").takeIf { it.isNotBlank() }
    } else null

    val textContent: String = if (message.content.startsWith("\uD83D\uDCCE")) {
        message.content.substringAfter("\n", "")
    } else {
        message.content
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isUser) Navy.copy(alpha = 0.08f) else White,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    0.5.dp,
                    if (isUser) Navy.copy(alpha = 0.15f) else Separator,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (isUser) "You" else "AI Assistant",
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUser) Gold else Navy,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (message.timestamp.isNotBlank()) {
                    Text(
                        formatChatTime(message.timestamp),
                        fontFamily = FontFamily.Default,
                        fontSize = 10.sp,
                        color = MutedInk,
                    )
                }
            }

            if (attachmentFileName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Parchment)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Description, contentDescription = null, tint = Gold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        attachmentFileName,
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Navy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (textContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = parseMarkdown(textContent),
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = Ink,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

private fun stripMarkdown(raw: String): String {
    var result = raw
    if (result.startsWith("\uD83D\uDCCE")) {
        val idx = result.indexOf("\n")
        result = if (idx >= 0) result.substring(idx + 1) else ""
    }
    result = result.replace("**", "")
    result = result.replace("*", "")
    result = result.replace("###", "")
    result = result.replace("##", "")
    result = result.replace("#", "")
    result = result.replace("_", "")
    return result.trim()
}

private fun formatChatTime(isoString: String): String {
    return try {
        val instant = java.time.Instant.parse(isoString)
        val localTime = instant.toString().substring(11, 16)
        localTime
    } catch (_: Exception) {
        ""
    }
}

private fun parseMarkdown(raw: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = raw.split("\n")
        for ((lineIndex, line) in lines.withIndex()) {
            if (lineIndex > 0) append("\n")

            val trimmed = line.trimStart()
            val leadingSpaces = line.length - trimmed.length

            when {
                trimmed.startsWith("### ") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Navy))
                    append(" ".repeat(leadingSpaces) + trimmed.substring(4))
                    pop()
                }
                trimmed.startsWith("## ") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy))
                    append(" ".repeat(leadingSpaces) + trimmed.substring(3))
                    pop()
                }
                trimmed.startsWith("# ") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Navy))
                    append(" ".repeat(leadingSpaces) + trimmed.substring(2))
                    pop()
                }
                else -> {
                    var i = 0
                    val content = line
                    while (i < content.length) {
                        when {
                            content.startsWith("**", i) -> {
                                val end = content.indexOf("**", i + 2)
                                if (end >= 0) {
                                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                    append(content.substring(i + 2, end))
                                    pop()
                                    i = end + 2
                                } else {
                                    append(content[i])
                                    i++
                                }
                            }
                            content.startsWith("*", i) && !content.startsWith("**", i) -> {
                                val end = content.indexOf("*", i + 1)
                                if (end >= 0) {
                                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                                    append(content.substring(i + 1, end))
                                    pop()
                                    i = end + 1
                                } else {
                                    append(content[i])
                                    i++
                                }
                            }
                            else -> {
                                append(content[i])
                                i++
                            }
                        }
                    }
                }
            }
        }
    }
}
