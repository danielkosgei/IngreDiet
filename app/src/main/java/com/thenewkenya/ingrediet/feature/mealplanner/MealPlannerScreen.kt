package com.thenewkenya.ingrediet.feature.mealplanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(
    navController: NavController,
    viewModel: MealPlannerViewModel = remember { MealPlannerViewModel() }
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Planner") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add meal to plan */ },
                containerColor = colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Week selector
            item {
                val currentWeekValue = viewModel.currentWeek.collectAsState().value
                WeekSelector(
                    currentWeek = currentWeekValue,
                    onWeekChanged = viewModel::updateWeek
                )
            }

            // Daily meal plans
            items(DayOfWeek.values()) { day ->
                DayMealPlan(
                    day = day,
                    meals = viewModel.getMealsForDay(day)
                )
            }
        }
    }
}

@Composable
fun WeekSelector(
    currentWeek: String,
    onWeekChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* TODO: Previous week */ }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous week")
        }
        Text(
            text = currentWeek,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { /* TODO: Next week */ }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Next week")
        }
    }
}

@Composable
fun DayMealPlan(
    day: DayOfWeek,
    meals: List<MealPlanItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (meals.isEmpty()) {
                Text(
                    text = "No meals planned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                meals.forEach { meal ->
                    MealItem(meal = meal)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MealItem(meal: MealPlanItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${meal.calories} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        IconButton(onClick = { /* TODO: Edit meal */ }) {
            Icon(Icons.Default.Add, contentDescription = "Edit meal")
        }
    }
} 