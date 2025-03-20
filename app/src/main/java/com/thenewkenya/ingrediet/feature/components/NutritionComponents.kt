package com.thenewkenya.ingrediet.feature.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color

@Composable
fun NutritionItem(
    title: String,
    value: String,
    target: String,
    progress: Float,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                color = color,
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = value,
                style = typography.titleMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = title,
            style = typography.bodyMedium,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
        Text(
            text = "/$target",
            style = typography.bodySmall,
            color = colors.onBackground.copy(alpha = 0.5f)
        )
    }
}
