package com.legal.transcriber

import android.content.Context
import android.os.Bundle
import com.legal.transcriber.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.legal.transcriber.shared.SharedFactory
import com.legal.transcriber.shared.auth.AndroidTokenStorage
import com.legal.transcriber.shared.auth.AuthState
import com.legal.transcriber.subscription.SubscriptionManager
import com.legal.transcriber.ui.navigation.MainTabView
import com.legal.transcriber.ui.screens.AuthScreen
import com.legal.transcriber.ui.screens.OnboardingScreen
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.LegalTranscriberTheme
import com.legal.transcriber.ui.theme.Navy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenStorage = AndroidTokenStorage(applicationContext)
        val components = SharedFactory.create(tokenStorage = tokenStorage)
        val viewModel = components.viewModel
        val authService = components.authService

        SubscriptionManager.configure(application = application, viewModel = viewModel)
        SubscriptionManager.setActivity(this)

        val hasCompletedOnboarding = runBlocking {
            applicationContext.dataStore.data.first()[booleanPreferencesKey("onboarding_completed")] ?: false
        }

        setContent {
            LegalTranscriberTheme {
                val authState by viewModel.authState.collectAsState()
                var showOnboarding by remember { mutableStateOf(!hasCompletedOnboarding) }

                when {
                    showOnboarding -> {
                        OnboardingScreen(onContinue = {
                            showOnboarding = false
                            runBlocking {
                                applicationContext.dataStore.edit { prefs ->
                                    prefs[booleanPreferencesKey("onboarding_completed")] = true
                                }
                            }
                        })
                    }
                    authState is AuthState.Loading || authState is AuthState.Error -> {
                        SplashScreen()
                    }
                    authState is AuthState.Unauthenticated -> {
                        AuthScreen(authService = authService)
                    }
                    authState is AuthState.Authenticated -> {
                        MainTabView(viewModel = viewModel, authService = authService)
                    }
                    else -> {
                        SplashScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Legal Transcriber",
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Gold)
        }
    }
}
