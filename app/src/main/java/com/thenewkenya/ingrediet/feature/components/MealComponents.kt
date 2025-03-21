package com.thenewkenya.ingrediet.feature.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlanItem
import com.thenewkenya.ingrediet.feature.mealplanner.MealPlannerViewModel
import com.thenewkenya.ingrediet.feature.mealplanner.MealTime
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun EnhancedMealPreviewCard(
    navController: NavController,
    colors: ColorScheme
) {
    val TextPrimary = MaterialTheme.colorScheme.onSurface
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val CardBackground = MaterialTheme.colorScheme.surface

    val context = LocalContext.current
    val mealPlannerViewModel = remember { 
        MealPlannerViewModel(context = context)
    }
    
    // Get today's meals from the meal planner
    val mealPlans by mealPlannerViewModel.mealPlans.collectAsState(initial = emptyMap<DayOfWeek, List<MealPlanItem>>())
    val isLoading by mealPlannerViewModel.isLoading.collectAsState(initial = true)
    val today = LocalDate.now().dayOfWeek
    val todaysMeals = mealPlans[today] ?: emptyList()
    
    // Load meal plans when composable enters composition
    LaunchedEffect(Unit) {
        mealPlannerViewModel.loadMealPlans()
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { navController.navigate("mealplanner") }
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Header with title and "View All" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Today's Meals",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                }
                
                TextButton(
                    onClick = { navController.navigate("mealplanner") },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colors.primary
                    )
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    // Show loading skeleton
                    MealPlannerSkeleton(TextSecondary)
                } else if (todaysMeals.isEmpty()) {
                    // Show empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals planned for today",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate("mealplanner") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Plan Your Meals")
                        }
                    }
                } else {
                    // Show today's meals
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Sort meals by meal time
                        val sortedMeals = todaysMeals.sortedBy { 
                            when(it.time) {
                                MealTime.Breakfast -> 0
                                MealTime.Lunch -> 1
                                MealTime.Dinner -> 2
                                MealTime.Snacks -> 3
                            }
                        }
                        
                        sortedMeals.take(3).forEachIndexed { index, meal ->
                            if (index > 0) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = TextSecondary.copy(alpha = 0.1f)
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        meal.recipeId?.let { recipeId ->
                                            navController.navigate("recipe/$recipeId")
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Meal time indicator
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = colors.primary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = meal.time.toString().first().toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (meal.description?.isNotEmpty() == true) meal.description else meal.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = meal.time.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary
                                            )
                                        )
                                        
                                        // Calorie information
                                        if (meal.calories > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(TextSecondary.copy(alpha = 0.3f), CircleShape)
                                            )
                                            Text(
                                                text = "${meal.calories} kcal",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                if (meal.imageUrl?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(meal.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    // Placeholder for missing image
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                color = colors.primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = colors.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // If there are more meals than we show
                        if (todaysMeals.size > 3) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = TextSecondary.copy(alpha = 0.1f)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("mealplanner") }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+${todaysMeals.size - 3} more meals",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight, 
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealPlannerSkeleton(textSecondary: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(3) { index ->
            if (index > 0) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = textSecondary.copy(alpha = 0.1f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skeleton for circle indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            textSecondary.copy(alpha = 0.1f),
                            CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .background(
                                textSecondary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .background(
                                textSecondary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                
                // Skeleton for image
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            textSecondary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
} 