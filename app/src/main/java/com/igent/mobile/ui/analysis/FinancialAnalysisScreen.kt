package com.igent.mobile.ui.analysis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.igent.mobile.domain.model.FinancialAnalysisResult
import com.igent.mobile.domain.model.FinancialAnalysisType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAnalysisScreen(
    viewModel: FinancialAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Analysis & Audit Suite") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tool Selector Chips
            Text(
                text = "Select Analysis Tool",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialAnalysisType.values().forEach { type ->
                    FilterChip(
                        selected = uiState.selectedType == type,
                        onClick = { viewModel.onTypeChanged(type) },
                        label = { Text(type.displayName.substringBefore(" /")) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = if (uiState.selectedType == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Description card for selected tool
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when(uiState.selectedType) {
                            FinancialAnalysisType.BUY_SELL -> Icons.Default.TrendingUp
                            FinancialAnalysisType.FUND_PICKER -> Icons.Default.PieChart
                            FinancialAnalysisType.SCAM_CHECK -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = uiState.selectedType.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.selectedType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Input field
            OutlinedTextField(
                value = uiState.targetQuery,
                onValueChange = { viewModel.onTargetQueryChanged(it) },
                label = { 
                    Text(
                        when(uiState.selectedType) {
                            FinancialAnalysisType.BUY_SELL -> "Enter Stock Symbol (e.g. LUCK, ENGRO, OGDC)"
                            FinancialAnalysisType.FUND_PICKER -> "Enter Fund Name (e.g. Meezan Islamic, NBP Fund)"
                            FinancialAnalysisType.SCAM_CHECK -> "Enter Entity / Scheme Name or URL"
                        }
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { viewModel.runAnalysis() }) {
                        Icon(Icons.Default.Search, contentDescription = "Run Analysis")
                    }
                }
            )

            // Quick suggestion chips
            val suggestions = when(uiState.selectedType) {
                FinancialAnalysisType.BUY_SELL -> listOf("LUCK", "ENGRO", "OGDC", "HUBC", "TRG")
                FinancialAnalysisType.FUND_PICKER -> listOf("Meezan Islamic Fund", "Al-Meezan Mutual", "NBP Balanced Fund", "ABL Stock Fund")
                FinancialAnalysisType.SCAM_CHECK -> listOf("Binance Cloud Mining VIP", "Daily Profit Club PK", "Fast Cash Crypto Scheme", "SECP Regulated Broker")
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Quick Suggestions:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.take(3).forEach { item ->
                        SuggestionChip(
                            onClick = { viewModel.runAnalysis(item) },
                            label = { Text(item, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }

            // Run Button
            Button(
                onClick = { viewModel.runAnalysis() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Running AI Analysis & Audit...")
                } else {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run ${uiState.selectedType.displayName}")
                }
            }

            // Error Message
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Results Section
            AnimatedVisibility(visible = uiState.result != null) {
                uiState.result?.let { result ->
                    AnalysisResultCard(result = result)
                }
            }
        }
    }
}

@Composable
fun AnalysisResultCard(result: FinancialAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Target & Verdict Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANALYSIS REPORT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = result.targetName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Score Badge
                Surface(
                    shape = CircleShape,
                    color = when {
                        result.score >= 75 -> Color(0xFF2E7D32) // Green
                        result.score >= 50 -> Color(0xFFF57C00) // Orange
                        else -> Color(0xFFC62828) // Red
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${result.score}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Divider()

            // Verdict text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Verdict: ${result.verdict}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Key Metrics grid/row
            if (result.metrics.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "Key Metrics", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        result.metrics.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Key Points
            if (result.keyPoints.isNotEmpty()) {
                Text(text = "Key Takeaways & Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                result.keyPoints.forEach { point ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).align(Alignment.Top)
                        )
                        Text(text = point, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Detailed Summary:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
