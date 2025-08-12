package com.thenewkenya.ingrediet.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateDpAsState
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State for toggle settings
    var biometricLoginEnabled by remember { mutableStateOf(false) }
    var dataSharingEnabled by remember { mutableStateOf(true) }
    var anonymizedDataEnabled by remember { mutableStateOf(true) }
    
    // Toast state
    var showSuccessToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy & Security",
                        style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Security Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Security",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Biometric login toggle
                    PrivacySecurityItem(
                        title = "Biometric Login",
                        description = "Use fingerprint to login",
                        icon = Icons.Outlined.Fingerprint,
                        hasSwitch = true,
                        isChecked = biometricLoginEnabled,
                        onCheckedChange = { biometricLoginEnabled = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Privacy Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Privacy",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Privacy Policy link
                    PrivacySecurityItem(
                        title = "Privacy Policy",
                        description = "View our privacy policy",
                        icon = Icons.Outlined.Visibility,
                        hasSwitch = false,
                        isChecked = false,
                        onCheckedChange = {},
                        onClick = { navController.navigate("privacy_policy") }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // Data sharing toggle
                    PrivacySecurityItem(
                        title = "Data Sharing",
                        description = "Allow app to share anonymized data",
                        icon = Icons.Outlined.DataObject,
                        hasSwitch = true,
                        isChecked = dataSharingEnabled,
                        onCheckedChange = { dataSharingEnabled = it }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // Anonymized data toggle
                    PrivacySecurityItem(
                        title = "Analytics",
                        description = "Help improve the app with anonymous usage data",
                        icon = Icons.Outlined.Security,
                        hasSwitch = true,
                        isChecked = anonymizedDataEnabled,
                        onCheckedChange = { anonymizedDataEnabled = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Management
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Data Management",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Clear temporary app data and cache",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = colors.primary
                        )
                        
                        Button(
                            onClick = { 
                                Log.d("PrivacySecurity", "Clear cache button clicked")
                                coroutineScope.launch {
                                    Log.d("PrivacySecurity", "Starting cache clear coroutine")
                                    clearAppCache(context)
                                    Log.d("PrivacySecurity", "Cache clear completed, showing toast")
                                    withContext(Dispatchers.Main) {
                                        Log.d("PrivacySecurity", "Setting showSuccessToast = true")
                                        showSuccessToast = true
                                        Log.d("PrivacySecurity", "showSuccessToast state: $showSuccessToast")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryContainer,
                                contentColor = colors.primary
                            ),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear App Cache")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Custom Success Toast Overlay - Absolutely positioned at bottom
        if (showSuccessToast) {
            Log.d("PrivacySecurity", "Rendering CustomSuccessToast overlay")
        }
        
        CustomSuccessToast(
            visible = showSuccessToast,
            message = "App cache cleared successfully",
            onDismiss = { 
                Log.d("PrivacySecurity", "Toast dismissed, setting showSuccessToast = false")
                showSuccessToast = false 
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        }
    }
}

@Composable
fun PrivacySecurityItem(
    title: String,
    description: String,
    icon: ImageVector,
    hasSwitch: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = typography.bodyLarge
            )
            
            Text(
                text = description,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        if (hasSwitch) {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        } else {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CustomSuccessToast(
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Log.d("CustomToast", "CustomSuccessToast called with visible: $visible")
    
    // Auto-dismiss after 3.5 seconds (longer for smoother experience)
    LaunchedEffect(visible) {
        Log.d("CustomToast", "LaunchedEffect triggered with visible: $visible")
        if (visible) {
            Log.d("CustomToast", "Toast is visible, setting 3.5 second delay")
            delay(3500)
            Log.d("CustomToast", "Delay completed, dismissing toast")
            onDismiss()
        }
    }
    
    // Animation values with smoother transitions
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseInOutCubic
        ),
        label = "toast_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(
            durationMillis = 450,
            easing = EaseInOutCubic
        ),
        label = "toast_scale"
    )
    
    val translateY by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseInOutCubic
        ),
        label = "toast_translate"
    )
    
    Log.d("CustomToast", "Checking visibility: visible=$visible, alpha=$alpha")
    
    if (visible || alpha > 0f) {
        Log.d("CustomToast", "Rendering toast UI")
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp, vertical = 32.dp), // Add bottom padding
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .alpha(alpha)
                    .scale(scale)
                    .offset(y = translateY),
                colors = CardDefaults.cardColors(
                    containerColor = colors.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = message,
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = colors.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private suspend fun clearAppCache(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            Log.d("CacheClearing", "Starting cache clearing process")
            
            // Clear app cache directory
            val cacheCleared = context.cacheDir.deleteRecursively()
            Log.d("CacheClearing", "Cache directory cleared: $cacheCleared")
            
            // Clear external cache if available
            context.externalCacheDir?.let { externalCache ->
                val externalCacheCleared = externalCache.deleteRecursively()
                Log.d("CacheClearing", "External cache cleared: $externalCacheCleared")
            }
            
            // Clear shared preferences related to cache (optional)
            val sharedPrefs = context.getSharedPreferences("recipe_cache", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            Log.d("CacheClearing", "Recipe cache preferences cleared")
            
            Log.d("CacheClearing", "Cache clearing process completed successfully")
            
        } catch (e: Exception) {
            Log.e("CacheClearing", "Error clearing cache", e)
            e.printStackTrace()
        }
    }
} 