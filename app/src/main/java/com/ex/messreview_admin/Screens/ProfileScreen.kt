package com.ex.messreview.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ex.messreview_admin.R


@Composable
fun ProfileScreen() {
    Image(
        modifier = Modifier
            .fillMaxSize(),
            painter = painterResource(id = R.drawable.bg1),
            contentDescription = "HomeScreenBg",
            contentScale = ContentScale.FillHeight

    )
    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Occupies 1/3rd of the screen height
        ) {
            Image(
                painter = painterResource(id = R.drawable.pfp), // Replace with your image resource
                contentDescription = "Profile Image",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp) // Adjust size as needed
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(1.dp)) // Add vertical spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            // Occupies 1/3rd of the screen height
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "User Icon",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "21BCE1902", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(0.1f)) // Expands space between icons
                Icon(
                    imageVector = Icons.Filled.Home, // Replace with appropriate icon
                    contentDescription = "Mess Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SRRC", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun profilePreview() {
    ProfileScreen()
}