package com.igent.mobile.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.igent.mobile.ui.components.DisclaimerComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioLedgerScreen(
    viewModel: PortfolioLedgerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var symbolInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("BUY") }
    var qtyInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    val totalValue = state.holdings.sumOf { it.totalValue }
    val totalCost = state.holdings.sumOf { it.quantity * it.averagePrice }
    val totalPnl = totalValue - totalCost
    val totalPnlPct = if (totalCost > 0) (totalPnl / totalCost) * 100.0 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio & Ledger", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Trade")
                    }
                    IconButton(onClick = { viewModel.resetPortfolio() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Reset Portfolio")
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
            // Portfolio Summary Card
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
                            text = "Portfolio Valuation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rs ${String.format(java.util.Locale.US, "%,.2f", totalValue)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Cost: Rs ${String.format(java.util.Locale.US, "%,.2f", totalCost)}")
                            Text(
                                text = "P&L: Rs ${String.format(java.util.Locale.US, "%,.2f", totalPnl)} (${String.format(java.util.Locale.US, "%.2f", totalPnlPct)}%)",
                                color = if (totalPnl >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Holdings (${state.holdings.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.holdings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No holdings in portfolio yet. Tap '+' to record a trade.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.holdings) { holding ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${holding.symbol} (${holding.name})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rs ${String.format(java.util.Locale.US, "%,.2f", holding.totalValue)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Qty: ${holding.quantity} @ Avg Rs ${holding.averagePrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "P&L: ${String.format(java.util.Locale.US, "%.2f", holding.unrealizedPnl)} (${String.format(java.util.Locale.US, "%.2f", holding.unrealizedPnlPct)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (holding.unrealizedPnl >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Record Trade") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = symbolInput,
                        onValueChange = { symbolInput = it },
                        label = { Text("Symbol (e.g. LUCK)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = typeInput,
                        onValueChange = { typeInput = it },
                        label = { Text("Type (BUY / SELL)") },
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
                    viewModel.addTrade(symbolInput, typeInput, qtyInput, priceInput)
                    if (state.errorMessage == null) {
                        showAddDialog = false
                        symbolInput = ""
                        qtyInput = ""
                        priceInput = ""
                    }
                }) {
                    Text("Save Trade")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
