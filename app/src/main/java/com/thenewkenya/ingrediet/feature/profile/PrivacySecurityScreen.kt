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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    
    // State for toggle settings
    var biometricLoginEnabled by remember { mutableStateOf(false) }
    var saveLoginDataEnabled by remember { mutableStateOf(true) }
    var dataSharingEnabled by remember { mutableStateOf(true) }
    var anonymizedDataEnabled by remember { mutableStateOf(true) }

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
                    
                    // Password change option
                    PrivacySecurityItem(
                        title = "Change Password",
                        description = "Update your account password",
                        icon = Icons.Outlined.Password,
                        hasSwitch = false,
                        isChecked = false,
                        onCheckedChange = {},
                        onClick = { navController.navigate("profile/account") }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // Biometric login toggle
                    PrivacySecurityItem(
                        title = "Biometric Login",
                        description = "Use fingerprint to login",
                        icon = Icons.Outlined.Fingerprint,
                        hasSwitch = true,
                        isChecked = biometricLoginEnabled,
                        onCheckedChange = { biometricLoginEnabled = it }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = colors.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // Save login toggle
                    PrivacySecurityItem(
                        title = "Save Login Data",
                        description = "Remember your login between sessions",
                        icon = Icons.Outlined.Key,
                        hasSwitch = true,
                        isChecked = saveLoginDataEnabled,
                        onCheckedChange = { saveLoginDataEnabled = it }
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
                        onClick = { /* Navigate to privacy policy */ }
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
                        text = "Clear data or delete your account",
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
                            tint = colors.error
                        )
                        
                        Button(
                            onClick = { /* Implement delete account flow */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.errorContainer,
                                contentColor = colors.error
                            ),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        ) {
                            Text("Delete Account")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
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