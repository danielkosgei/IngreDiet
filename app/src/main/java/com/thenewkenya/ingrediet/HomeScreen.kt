package com.thenewkenya.ingrediet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Twitch
import io.github.jan.supabase.compose.auth.ui.AuthForm
import io.github.jan.supabase.compose.auth.ui.FormComponent
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.email.OutlinedEmailField
import io.github.jan.supabase.compose.auth.ui.password.OutlinedPasswordField
import io.github.jan.supabase.compose.auth.ui.password.PasswordRule
import io.github.jan.supabase.compose.auth.ui.password.rememberPasswordRuleList
import io.github.jan.supabase.compose.auth.ui.phone.OutlinedPhoneField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Github

@OptIn(AuthUiExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    MaterialTheme(
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            AuthForm() {
                var password by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                val state = LocalAuthState.current
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedEmailField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail") },
                        mandatory = email.isNotBlank() //once an email is entered, it is mandatory. (which enable validation)
                    )
                    OutlinedPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        rules = rememberPasswordRuleList(PasswordRule.minLength(6), PasswordRule.containsSpecialCharacter(), PasswordRule.containsDigit(), PasswordRule.containsLowercase(), PasswordRule.containsUppercase())
                    )
                    FormComponent ("accept_terms") { valid ->
                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = valid.value,
                                onCheckedChange = { valid.value = it },
                            )
                            Text("Accept Terms", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Button (
                        onClick = {}, //Login with email and password,
                        enabled = state.validForm,
                    ) {
                        Text("Login")
                    }
                    OutlinedButton (
                        onClick = {}, //Login with Google,
                        content = { ProviderButtonContent(Apple) }
                    )
                    Button(
                        onClick = {}, //Login with Twitch,
                        content = { ProviderButtonContent(Google) }
                    )
                }
            }
        }
    }
}
