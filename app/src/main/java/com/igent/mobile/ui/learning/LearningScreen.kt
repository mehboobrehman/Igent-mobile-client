package com.igent.mobile.ui.learning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.igent.mobile.data.local.entity.CourseEntity
import com.igent.mobile.data.local.entity.CourseModuleEntity
import com.igent.mobile.data.local.entity.QuizEntity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.selectedCourse != null) uiState.selectedCourse!!.title else "PSX Investor Learning Academy"
                    ) 
                },
                navigationIcon = {
                    if (uiState.selectedCourse != null) {
                        IconButton(onClick = { viewModel.clearSelectedCourse() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Courses")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.selectedCourse == null) {
                CourseCatalogList(
                    courses = uiState.courses,
                    onCourseSelected = { viewModel.selectCourse(it) }
                )
            } else {
                CourseDetailView(
                    course = uiState.selectedCourse!!,
                    modules = uiState.modules,
                    quizzes = uiState.quizzes,
                    uiState = uiState,
                    onMarkModuleCompleted = { viewModel.markModuleCompleted(it) },
                    onOptionSelected = { viewModel.selectQuizOption(it) },
                    onSubmitAnswer = { viewModel.submitQuizAnswer() },
                    onNextQuiz = { viewModel.nextQuiz() }
                )
            }
        }
    }
}

@Composable
fun CourseCatalogList(
    courses: List<CourseEntity>,
    onCourseSelected: (CourseEntity) -> Unit
) {
    if (courses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Master Stock Investing & Financial Literacy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(courses) { course ->
                Card(
                    onClick = { onCourseSelected(course) },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = { },
                                label = { Text(course.category) }
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = course.difficulty,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = course.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${course.estimatedMinutes} mins",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseDetailView(
    course: CourseEntity,
    modules: List<CourseModuleEntity>,
    quizzes: List<QuizEntity>,
    uiState: LearningUiState,
    onMarkModuleCompleted: (String) -> Unit,
    onOptionSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuiz: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Modules (${modules.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Interactive Quizzes (${quizzes.size})") }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(modules) { module ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Module ${module.orderIndex}: ${module.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (module.isCompleted) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Text(
                                text = module.content,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (!module.isCompleted) {
                                OutlinedButton(
                                    onClick = { onMarkModuleCompleted(module.moduleId) },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Completed")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive Quiz Player
            if (quizzes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quizzes available for this course yet.")
                }
            } else {
                val currentQuiz = quizzes.getOrNull(uiState.currentQuizIndex)
                if (currentQuiz != null) {
                    QuizPlayerCard(
                        quiz = currentQuiz,
                        currentIndex = uiState.currentQuizIndex,
                        totalCount = quizzes.size,
                        selectedOptionIndex = uiState.selectedOptionIndex,
                        feedback = uiState.quizFeedback,
                        onOptionSelected = onOptionSelected,
                        onSubmitAnswer = onSubmitAnswer,
                        onNextQuiz = onNextQuiz
                    )
                }
            }
        }
    }
}

@Composable
fun QuizPlayerCard(
    quiz: QuizEntity,
    currentIndex: Int,
    totalCount: Int,
    selectedOptionIndex: Int?,
    feedback: QuizFeedback?,
    onOptionSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuiz: () -> Unit
) {
    val options = remember(quiz.optionsJson) {
        try {
            Json.decodeFromString<List<String>>(quiz.optionsJson)
        } catch (e: Exception) {
            listOf("Option A", "Option B", "Option C", "Option D")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of $totalCount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = quiz.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            options.forEachIndexed { index, optionText ->
                val isSelected = selectedOptionIndex == index
                Card(
                    onClick = { onOptionSelected(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(index) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = optionText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedVisibility(visible = feedback != null) {
                feedback?.let { fb ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (fb.isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (fb.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (fb.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (fb.isCorrect) "Correct Answer!" else "Incorrect",
                                    fontWeight = FontWeight.Bold,
                                    color = if (fb.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = fb.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (feedback == null) {
                    Button(
                        onClick = onSubmitAnswer,
                        enabled = selectedOptionIndex != null
                    ) {
                        Text("Submit Answer")
                    }
                } else {
                    Button(onClick = onNextQuiz) {
                        Text(if (currentIndex + 1 < totalCount) "Next Question" else "Finish Quiz")
                    }
                }
            }
        }
    }
}
