package com.igent.mobile.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var openRouterKeyVisible by remember { mutableStateOf(false) }
    var groqKeyVisible by remember { mutableStateOf(false) }

    // Subsequent app launch auto-bypass check
    LaunchedEffect(uiState.isLoggedIn, uiState.hasEnteredApiKey, uiState.openRouterKey) {
        if (uiState.isLoggedIn && (uiState.hasEnteredApiKey || uiState.openRouterKey.isNotBlank())) {
            onNavigateToMain()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Igent Mobile - Authentication & API Setup") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Google OAuth & Secure AI Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sign in securely with Google and configure your AI API keys.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status feedback
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            uiState.successMessage?.let { success ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = success, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Section 1: Google OAuth Sign-in / Session Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Google Account Authentication",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    if (!uiState.isLoggedIn) {
                        Button(
                            onClick = { viewModel.signInWithGoogle() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Signed in as:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.googleUserEmail.ifBlank { "Authenticated User" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign Out")
                            }
                        }
                    }
                }
            }

            // Section 2: API Keys Configuration (Shown on first launch after login until API key is entered)
            if (uiState.isLoggedIn && !uiState.hasEnteredApiKey) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "AI API Keys Configuration (First-Time Setup)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Please enter your OpenRouter or Groq API key to proceed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = uiState.openRouterKey,
                            onValueChange = { viewModel.updateOpenRouterKey(it) },
                            label = { Text("OpenRouter API Key (sk-or-v1-...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (openRouterKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (openRouterKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { openRouterKeyVisible = !openRouterKeyVisible }) {
                                    Icon(imageVector = image, contentDescription = "Toggle OpenRouter Key visibility")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = uiState.groqKey,
                            onValueChange = { viewModel.updateGroqKey(it) },
                            label = { Text("Groq API Key (gsk_...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (groqKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (groqKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { groqKeyVisible = !groqKeyVisible }) {
                                    Icon(imageVector = image, contentDescription = "Toggle Groq Key visibility")
                                }
                            }
                        )

                        Button(
                            onClick = { viewModel.saveApiKeys() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save & Encrypt API Keys")
                            }
                        }
                    }
                }
            }

            // Action / Navigation Button
            if (uiState.isLoggedIn && (uiState.hasEnteredApiKey || uiState.openRouterKey.isNotBlank())) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onNavigateToMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proceed to Igent Main Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
