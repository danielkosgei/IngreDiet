package com.thenewkenya.ingrediet.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scrollState = rememberScrollState()
    
    // Get current date for last updated
    val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
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
            
            // Header
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
                        text = "IngreDiet Privacy Policy",
                        style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Last updated: $currentDate",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "This Privacy Policy describes how IngreDiet (\"we\", \"our\", or \"us\") collects, uses, and protects your personal information when you use our mobile application and services.",
                        style = typography.bodyMedium,
                        color = colors.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Controller Information
            PrivacySection(
                title = "1. Data Controller",
                content = """
                    IngreDiet is the data controller for the personal information we collect about you.
                    
                    Contact Information:
                    • Company: IngreDiet
                    • Location: Kenya
                    • Email: privacy@ingrediet.com
                    
                    Our servers are located in the European Union to ensure the highest level of data protection under GDPR regulations.
                """.trimIndent()
            )
            
            // Information We Collect
            PrivacySection(
                title = "2. Information We Collect",
                content = """
                    We collect the following types of information:
                    
                    Personal Information:
                    • Account details (name, email address)
                    • Profile information (dietary preferences, allergies, nutrition goals)
                    • Authentication data (encrypted passwords, biometric data if enabled)
                    
                    Usage Information:
                    • Recipes viewed, saved, and created
                    • Meal planning data and shopping lists
                    • App usage analytics (anonymized)
                    • Device information and app performance data
                    
                    Optional Information:
                    • Photos uploaded to recipes
                    • Social features usage (if enabled)
                    • Notification preferences
                """.trimIndent()
            )
            
            // How We Use Information
            PrivacySection(
                title = "3. How We Use Your Information",
                content = """
                    We use your information for the following purposes:
                    
                    Service Provision:
                    • Provide personalized meal planning and recipe recommendations
                    • Generate shopping lists based on your meal plans
                    • Track your nutrition goals and progress
                    • Enable recipe sharing and social features
                    
                    App Improvement:
                    • Analyze usage patterns to improve app functionality
                    • Develop new features based on user needs
                    • Ensure app security and prevent fraud
                    
                    Communication:
                    • Send important service updates
                    • Provide customer support
                    • Send notifications (with your consent)
                    
                    Legal Basis (GDPR):
                    • Contract performance: Providing app services
                    • Legitimate interest: App improvement and security
                    • Consent: Marketing communications and analytics
                """.trimIndent()
            )
            
            // Data Storage and Security
            PrivacySection(
                title = "4. Data Storage and Security",
                content = """
                    Data Location:
                    • Your data is stored on secure servers in the European Union
                    • We use industry-standard encryption for data transmission and storage
                    • Database backups are encrypted and stored securely
                    
                    Security Measures:
                    • End-to-end encryption for sensitive data
                    • Regular security audits and updates
                    • Access controls and authentication requirements
                    • Secure API connections (HTTPS/TLS)
                    
                    Data Retention:
                    • Account data: Retained while your account is active
                    • Analytics data: Anonymized after 24 months
                    • Cached data: Automatically cleared periodically
                    • Deleted accounts: Data purged within 30 days
                """.trimIndent()
            )
            
            // Your Rights (GDPR & Kenya DPA)
            PrivacySection(
                title = "5. Your Rights",
                content = """
                    Under GDPR and Kenya Data Protection Act, you have the following rights:
                    
                    Access Rights:
                    • Right to access your personal data
                    • Right to data portability (export your data)
                    • Right to be informed about data processing
                    
                    Control Rights:
                    • Right to rectification (correct inaccurate data)
                    • Right to erasure ("right to be forgotten")
                    • Right to restrict processing
                    • Right to object to processing
                    
                    Consent Rights:
                    • Right to withdraw consent at any time
                    • Right to opt-out of marketing communications
                    • Right to manage notification preferences
                    
                    To exercise these rights, contact us at privacy@ingrediet.com or use the in-app settings.
                """.trimIndent()
            )
            
            // Data Sharing
            PrivacySection(
                title = "6. Data Sharing and Third Parties",
                content = """
                    We do not sell your personal data. We may share data in the following circumstances:
                    
                    Service Providers:
                    • Cloud storage providers (EU-based, GDPR compliant)
                    • Authentication services (encrypted data only)
                    • Analytics providers (anonymized data only)
                    
                    Legal Requirements:
                    • When required by law or legal process
                    • To protect our rights and prevent fraud
                    • In case of business transfer (with your notice)
                    
                    User Consent:
                    • Recipe sharing (when you choose to share publicly)
                    • Social features (when you enable them)
                    
                    All third-party services are GDPR compliant and have appropriate data protection agreements.
                """.trimIndent()
            )
            
            // Kenyan Data Protection Compliance
            PrivacySection(
                title = "7. Kenya Data Protection Act Compliance",
                content = """
                    As a Kenyan company, we comply with the Kenya Data Protection Act (2019):
                    
                    Lawful Processing:
                    • We process data fairly and lawfully
                    • Clear purposes for data collection are specified
                    • Data minimization principle applied
                    
                    Data Subject Rights:
                    • Right to access personal data
                    • Right to correction of inaccurate data
                    • Right to deletion of personal data
                    • Right to data portability
                    
                    Accountability:
                    • Regular privacy impact assessments
                    • Data protection by design and default
                    • Breach notification procedures in place
                    
                    For complaints in Kenya, you may contact the Office of the Data Protection Commissioner.
                """.trimIndent()
            )
            
            // Children's Privacy
            PrivacySection(
                title = "8. Children's Privacy",
                content = """
                    IngreDiet is not intended for children under 13 years of age.
                    
                    • We do not knowingly collect personal information from children under 13
                    • If we discover we have collected such information, we will delete it immediately
                    • Parents who believe their child has provided information should contact us
                    
                    Age Verification:
                    • Users must confirm they are 13 or older during registration
                    • Account creation requires valid email verification
                """.trimIndent()
            )
            
            // Cookies and Tracking
            PrivacySection(
                title = "9. Cookies and Tracking",
                content = """
                    Our app uses the following tracking technologies:
                    
                    Essential Functions:
                    • Session management and authentication
                    • User preferences and settings storage
                    • App functionality and performance
                    
                    Analytics (with consent):
                    • Usage patterns and feature adoption
                    • Crash reporting and error tracking
                    • Performance optimization data
                    
                    You can manage tracking preferences in the app settings. Essential functions cannot be disabled as they are necessary for app operation.
                """.trimIndent()
            )
            
            // International Transfers
            PrivacySection(
                title = "10. International Data Transfers",
                content = """
                    Your data may be transferred between Kenya and the European Union:
                    
                    Safeguards:
                    • EU servers provide GDPR-level protection
                    • Appropriate technical and organizational measures in place
                    • Data transfer agreements comply with both jurisdictions
                    
                    Legal Basis:
                    • Adequacy decisions where applicable
                    • Standard contractual clauses (SCCs)
                    • Explicit consent for specific transfers
                """.trimIndent()
            )
            
            // Changes to Privacy Policy
            PrivacySection(
                title = "11. Changes to This Privacy Policy",
                content = """
                    We may update this Privacy Policy from time to time:
                    
                    • Material changes will be notified via app notification
                    • Non-material changes will be posted with updated date
                    • Continued use constitutes acceptance of changes
                    • You may review the policy anytime in-app
                    
                    We encourage you to review this policy periodically.
                """.trimIndent()
            )
            
            // Contact Information
            PrivacySection(
                title = "12. Contact Us",
                content = """
                    If you have questions about this Privacy Policy or our data practices:
                    
                    Email: privacy@ingrediet.com
                    App: Use "Contact Support" in Settings
                    
                    For GDPR-related inquiries:
                    • Email: gdpr@ingrediet.com
                    • Response time: Within 30 days
                    
                    For Kenya DPA-related inquiries:
                    • Email: kenya-privacy@ingrediet.com
                    • Office of the Data Protection Commissioner: https://odpc.go.ke
                    
                    We are committed to addressing your privacy concerns promptly and transparently.
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PrivacySection(
    title: String,
    content: String
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
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
                text = title,
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                style = typography.bodyMedium,
                color = colors.onSurface,
                lineHeight = typography.bodyMedium.lineHeight * 1.2
            )
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
} 