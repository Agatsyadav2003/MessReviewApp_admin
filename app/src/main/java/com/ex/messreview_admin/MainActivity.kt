package com.ex.messreview_admin

import AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ex.messreview_admin.ui.theme.MessReviewTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           MessReviewTheme {
               Surface(
                   modifier = Modifier.fillMaxSize(),
               )
               {
                   AppNavigation()
               }
           }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppNavigation()
}