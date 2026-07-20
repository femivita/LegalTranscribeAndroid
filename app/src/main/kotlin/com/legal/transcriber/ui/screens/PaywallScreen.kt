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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.subscription.SubscriptionManager
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
) {
    val offerings by SubscriptionManager.offerings.collectAsState()
    val isLoading by SubscriptionManager.isLoading.collectAsState()
    val purchaseError by SubscriptionManager.purchaseError.collectAsState()

    val rcPackage = offerings?.current?.availablePackages?.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {},
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text("Close", fontFamily = FontFamily.Default, color = MutedInk)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream,
                ),
            )
        },
        containerColor = Cream,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Upgrade to Pro",
                fontFamily = FontFamily.Default,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Navy,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Unlock unlimited transcriptions and premium features",
                fontFamily = FontFamily.Default,
                fontSize = 15.sp,
                color = MutedInk,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            FeatureItem(Icons.Rounded.GraphicEq, "Unlimited Transcriptions", "No monthly limits on audio transcriptions")
            FeatureItem(Icons.Rounded.Description, "DOCX & PDF Export", "Export transcripts in professional formats")
            FeatureItem(Icons.AutoMirrored.Rounded.Chat, "Unlimited Chat", "Chat with AI about your transcripts without limits")
            FeatureItem(Icons.Rounded.Search, "Case Analysis", "AI-powered summaries and action notes")
            FeatureItem(Icons.Rounded.CloudUpload, "Cloud Sync", "Access your transcripts across all devices")

            Spacer(modifier = Modifier.height(32.dp))

            if (rcPackage != null) {
                Button(
                    onClick = {
                        SubscriptionManager.purchase(
                            pkg = rcPackage,
                            onSuccess = onDismiss,
                            onError = {}
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Start 2-Week Free Trial",
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                fontSize = 16.sp,
                            )
                            Text(
                                "${rcPackage.product.price.formatted}/month after trial",
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = White.copy(alpha = 0.8f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = {
                    SubscriptionManager.restorePurchases(
                        onSuccess = onDismiss,
                        onError = {}
                    )
                }) {
                    Text(
                        "Restore Purchases",
                        fontFamily = FontFamily.Default,
                        color = MutedInk,
                        fontSize = 14.sp,
                    )
                }
            } else {
                CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Loading subscription options...",
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = MutedInk,
                )
            }

            purchaseError?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    error,
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Cancel anytime. Your subscription will auto-renew unless cancelled at least 24 hours before the end of the current period.",
                fontFamily = FontFamily.Default,
                fontSize = 11.sp,
                color = MutedInk,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Gold.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                title,
                fontFamily = FontFamily.Default,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy,
            )
            Text(
                description,
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = MutedInk,
            )
        }
    }
}
