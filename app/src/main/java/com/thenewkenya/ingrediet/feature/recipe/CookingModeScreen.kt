package com.thenewkenya.ingrediet.feature.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.data.model.DetailedRecipe
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    navController: NavController,
    recipe: DetailedRecipe
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = recipe.instructions.size
    var isPaused by remember { mutableStateOf(false) }
    var stepTimer by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    
    // Timer effect
    LaunchedEffect(isTimerRunning, isPaused) {
        while (isTimerRunning && !isPaused) {
            delay(1000L)
            stepTimer++
        }
    }
    
    // Start timer when entering cooking mode
    LaunchedEffect(Unit) {
        isTimerRunning = true
    }
    
    val listState = rememberLazyListState()
    
    // Auto-scroll to current step
    LaunchedEffect(currentStep) {
        if (totalSteps > 0) {
            listState.animateScrollToItem(currentStep)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = recipe.name,
                            style = typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = "Step ${currentStep + 1} of $totalSteps",
                            style = typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Timer display
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colors.primaryContainer
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = formatTime(stepTimer),
                            style = typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface
                )
            )
        },
        bottomBar = {
            CookingNavigationBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                isPaused = isPaused,
                onPreviousStep = {
                    if (currentStep > 0) {
                        currentStep--
                        stepTimer = 0L
                    }
                },
                onNextStep = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                        stepTimer = 0L
                    }
                },
                onPauseResume = {
                    isPaused = !isPaused
                },
                onFinishCooking = {
                    navController.navigateUp()
                },
                colors = colors
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress indicator
            item {
                ProgressSection(
                    currentStep = currentStep,
                    totalSteps = totalSteps,
                    colors = colors,
                    typography = typography
                )
            }
            
            // Instructions
            items(totalSteps) { index ->
                InstructionStepCard(
                    stepNumber = index + 1,
                    instruction = recipe.instructions[index],
                    isActive = index == currentStep,
                    isCompleted = index < currentStep,
                    colors = colors,
                    typography = typography
                )
            }
            
            // Completion message
            if (currentStep >= totalSteps) {
                item {
                    CompletionCard(
                        recipeName = recipe.name,
                        totalTime = stepTimer,
                        colors = colors,
                        typography = typography
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    currentStep: Int,
    totalSteps: Int,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cooking Progress",
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.primary,
                trackColor = colors.primary.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${((currentStep + 1).toFloat() / totalSteps.toFloat() * 100).toInt()}% Complete",
                style = typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun InstructionStepCard(
    stepNumber: Int,
    instruction: String,
    isActive: Boolean,
    isCompleted: Boolean,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    val backgroundColor = when {
        isActive -> colors.primaryContainer
        isCompleted -> colors.secondaryContainer
        else -> colors.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val contentColor = when {
        isActive -> colors.onPrimaryContainer
        isCompleted -> colors.onSecondaryContainer
        else -> colors.onSurface
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Step number indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> colors.secondary
                            isActive -> colors.primary
                            else -> colors.outline.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = colors.onSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = stepNumber.toString(),
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isActive -> colors.onPrimary
                            else -> colors.onSurface
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Instruction text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (isActive) {
                    Text(
                        text = "Current Step",
                        style = typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Text(
                    text = instruction,
                    style = if (isActive) typography.bodyLarge else typography.bodyMedium,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                    color = contentColor,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun CookingNavigationBar(
    currentStep: Int,
    totalSteps: Int,
    isPaused: Boolean,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onPauseResume: () -> Unit,
    onFinishCooking: () -> Unit,
    colors: androidx.compose.material3.ColorScheme
) {
    Surface(
        color = colors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            OutlinedButton(
                onClick = onPreviousStep,
                enabled = currentStep > 0,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = "Previous step",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Pause/Resume button
            Button(
                onClick = onPauseResume,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondaryContainer
                )
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Next/Finish button
            if (currentStep < totalSteps - 1) {
                Button(
                    onClick = onNextStep,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = "Next step",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Button(
                    onClick = onFinishCooking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Finish",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finish")
                }
            }
        }
    }
}

@Composable
private fun CompletionCard(
    recipeName: String,
    totalTime: Long,
    colors: androidx.compose.material3.ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = colors.secondary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Recipe Complete!",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You've successfully cooked $recipeName",
                style = typography.bodyLarge,
                color = colors.onSecondaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Total cooking time: ${formatTime(totalTime)}",
                style = typography.bodyMedium,
                color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
        else -> String.format("%d:%02d", minutes, secs)
    }
} 