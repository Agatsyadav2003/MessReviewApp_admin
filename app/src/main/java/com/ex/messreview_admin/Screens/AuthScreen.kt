@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.ex.messreview.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ex.messreview_admin.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginClicked: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
                if (username.isEmpty() || password.isEmpty()) {Toast.makeText(context, "Enter Credentials", Toast.LENGTH_SHORT).show()}
                else{
                    val email = formatUsername(username)
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onLoginClicked(username, password)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }},
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
fun formatUsername(username: String): String {
    return if (username.matches(Regex("admin\\d{4}"))) {
        "$username@gmail.com"
    } else {
        username
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
                color = Color(0xFF838383),
                style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
        },
        colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xfffbfbfb)),
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
                color = Color(0xFF808080),
                style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
        },
        colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xfffbfbfb)),
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

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    AuthScreen(onLoginClicked = { _, _ -> })
}
