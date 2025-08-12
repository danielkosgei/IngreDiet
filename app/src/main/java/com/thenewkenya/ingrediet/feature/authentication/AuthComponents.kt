package com.thenewkenya.ingrediet.feature.authentication

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.thenewkenya.ingrediet.R

@Composable
fun AppleLogo(
    modifier: Modifier = Modifier,
    color: Color
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_apple),
        contentDescription = "Apple Logo",
        modifier = modifier,
        tint = color
    )
} 