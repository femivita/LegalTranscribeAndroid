package com.legal.transcriber.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legal.transcriber.shared.auth.AuthService
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Separator
import com.legal.transcriber.ui.theme.White

@Composable
fun AuthScreen(authService: AuthService) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Legal Transcriber",
                fontFamily = FontFamily.Default,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Navy,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                if (isLogin) "Welcome back" else "Create your account",
                fontFamily = FontFamily.Default,
                fontSize = 16.sp,
                color = MutedInk,
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!isLogin) {
                AuthField(
                    icon = Icons.Rounded.Person,
                    placeholder = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            AuthField(
                icon = Icons.Rounded.Email,
                placeholder = "Email",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email,
            )
            Spacer(modifier = Modifier.height(16.dp))

            AuthField(
                icon = Icons.Rounded.Lock,
                placeholder = "Password",
                value = password,
                onValueChange = { password = it },
                isPassword = true,
            )

            if (!isLogin) {
                Spacer(modifier = Modifier.height(16.dp))
                AuthField(
                    icon = Icons.Rounded.Lock,
                    placeholder = "Confirm Password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    isPassword = true,
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    errorMessage!!,
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = androidx.compose.ui.graphics.Color.Red,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    errorMessage = null
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    if (!isLogin) {
                        if (name.isEmpty()) {
                            errorMessage = "Please enter your name"
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }
                        isLoading = true
                        authService.signUp(name, email, password) { _, error ->
                            isLoading = false
                            if (error != null) errorMessage = error
                        }
                    } else {
                        isLoading = true
                        authService.signIn(email, password) { _, error ->
                            isLoading = false
                            if (error != null) errorMessage = error
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(
                        if (isLogin) "Sign In" else "Create Account",
                        fontFamily = FontFamily.Default,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                isLogin = !isLogin
                errorMessage = null
            }) {
                Text(
                    if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Sign In",
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = Gold,
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AuthField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(placeholder, fontFamily = FontFamily.Default) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Gold) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedBorderColor = Gold,
            unfocusedBorderColor = Separator,
        ),
    )
}
