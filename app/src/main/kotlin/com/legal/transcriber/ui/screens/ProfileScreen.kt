package com.legal.transcriber.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.shared.auth.AuthService
import com.legal.transcriber.shared.auth.AuthState
import com.legal.transcriber.shared.viewmodel.HistoryState
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import com.legal.transcriber.subscription.SubscriptionManager
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Separator
import com.legal.transcriber.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: TranscriptionViewModel,
    authService: AuthService,
    onShowPaywall: () -> Unit,
) {
    val authState by viewModel.authState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val isPro by SubscriptionManager.isPro.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profile",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (authState is AuthState.Authenticated) {
                val user = (authState as AuthState.Authenticated).user
                val displayName = user.displayName
                    ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                    ?: "User"
                Text(
                    displayName,
                    fontFamily = FontFamily.Default,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    user.email ?: "",
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = MutedInk,
                )
            } else {
                Text("Loading...", fontFamily = FontFamily.Default, fontSize = 16.sp, color = MutedInk)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White),
            ) {
                ProfileRow(
                    icon = Icons.Rounded.Description,
                    title = "Transcripts Saved",
                ) {
                    if (historyState is HistoryState.Success) {
                        Text(
                            "${(historyState as HistoryState.Success).items.size}",
                            fontFamily = FontFamily.Default,
                            fontSize = 16.sp,
                            color = Navy,
                        )
                    }
                }

                androidx.compose.material3.Divider(color = Separator, thickness = 0.5.dp)

                ProfileRow(
                    icon = Icons.Rounded.WorkspacePremium,
                    title = "Subscription",
                ) {
                    if (isPro) {
                        Text(
                            "Pro",
                            fontFamily = FontFamily.Default,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                        )
                    } else {
                        TextButton(onClick = onShowPaywall) {
                            Text(
                                "Upgrade",
                                fontFamily = FontFamily.Default,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Gold,
                            )
                        }
                    }
                }

                androidx.compose.material3.Divider(color = Separator, thickness = 0.5.dp)

                ProfileRow(
                    icon = Icons.Rounded.Schedule,
                    title = "Member Since",
                ) {
                    Text("July 2026", fontFamily = FontFamily.Default, fontSize = 16.sp, color = Navy)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = White,
                    contentColor = Color.Red,
                ),
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", fontFamily = FontFamily.Default) },
            text = { Text("Are you sure you want to sign out?", fontFamily = FontFamily.Default) },
            confirmButton = {
                TextButton(onClick = {
                    authService.signOut()
                    showSignOutDialog = false
                }) { Text("Sign Out", color = Color.Red, fontFamily = FontFamily.Default) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel", fontFamily = FontFamily.Default) }
            }
        )
    }
}

@Composable
private fun ProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, fontFamily = FontFamily.Default, fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF262629))
        Spacer(modifier = Modifier.weight(1f))
        trailing()
    }
}
