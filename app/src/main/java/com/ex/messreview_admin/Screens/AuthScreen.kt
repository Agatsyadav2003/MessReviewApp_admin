@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.ex.messreview.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.UiMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.navigation.NavController
import com.ex.messreview_admin.R
import com.ex.messreview_admin.viewmodel.AuthState
import com.ex.messreview_admin.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavController, authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("caterer_screen")
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.messreviewlogo),
            contentDescription = "logo",
            modifier = Modifier
                .fillMaxWidth(0.5f) // Adjust the logo size as a proportion of the screen width
                .aspectRatio(1f) // Maintain aspect ratio
        )
        Spacer(modifier = Modifier.height(30.dp))
        UserTextField(username) { username = it }
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(
            password,
            isPasswordVisible,
            { password = it }) {
            isPasswordVisible = !isPasswordVisible
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
                authViewModel.login(username,password)},
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(3f) // Maintain aspect ratio
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(25.dp))
                .clip(RoundedCornerShape(30.dp))
        ) {
            Text("Login", color = MaterialTheme.colorScheme.surface, fontSize = MaterialTheme.typography.titleMedium.fontSize)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = "Username",
                color =MaterialTheme.colorScheme.onSecondary,
                style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.colorScheme.onSecondary,
            containerColor = Color(0xfffbfbfb)
        ),
        modifier = Modifier
            .requiredWidth(331.dp)
            .requiredHeight(54.dp)
    )
}

@Composable
fun PasswordField(
    value: String,
    isPasswordVisible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityChange: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = "Password",
                color = MaterialTheme.colorScheme.onSecondary,
                style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.colorScheme.onSecondary,
            containerColor = Color(0xfffbfbfb)
        ),
        modifier = Modifier
            .requiredWidth(331.dp)
            .requiredHeight(54.dp),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Toggle password visibility"
                )
            }
        }
    )
}




