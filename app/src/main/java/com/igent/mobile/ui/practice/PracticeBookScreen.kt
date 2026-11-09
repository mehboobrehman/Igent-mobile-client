package com.igent.mobile.ui.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.igent.mobile.ui.components.DisclaimerComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeBookScreen(
    viewModel: PracticeBookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showTradeDialog by remember { mutableStateOf(false) }

    var symbolInput by remember { mutableStateOf("") }
    var sideInput by remember { mutableStateOf("buy") }
    var qtyInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    val totalPortfolioVal = state.cash + state.positions.sumOf { it.qty * it.cost / maxOf(1.0, it.qty) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice Book (Paper Trading)", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showTradeDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Execute Trade")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            DisclaimerComponent()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Virtual Capital & Cash",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rs ${String.format(java.util.Locale.US, "%,.2f", state.cash)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Virtual Portfolio: Rs ${String.format(java.util.Locale.US, "%,.2f", totalPortfolioVal)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Open Practice Positions (${state.positions.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.positions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No open virtual positions. Tap '+' to execute a practice trade.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.positions) { pos ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${pos.symbol} - Qty: ${pos.qty}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cost Basis: Rs ${String.format(java.util.Locale.US, "%,.2f", pos.cost)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTradeDialog) {
        AlertDialog(
            onDismissRequest = { showTradeDialog = false },
            title = { Text("Execute Practice Trade") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = symbolInput,
                        onValueChange = { symbolInput = it },
                        label = { Text("Symbol (e.g. LUCK)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sideInput,
                        onValueChange = { sideInput = it },
                        label = { Text("Side (buy / sell)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("Quantity") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price (Rs)") },
                        singleLine = true
                    )
                    if (state.errorMessage != null) {
                        Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.executeTrade(symbolInput, sideInput, qtyInput, priceInput)
                    if (state.errorMessage == null) {
                        showTradeDialog = false
                        symbolInput = ""
                        qtyInput = ""
                        priceInput = ""
                    }
                }) {
                    Text("Execute")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTradeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
