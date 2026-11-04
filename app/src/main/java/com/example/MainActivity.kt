package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JarvisHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure App Check with launch intent before UI or Firebase operations
        configureAppCheck(this, intent)

        // Step 2: Edge-to-edge support
        enableEdgeToEdge()

        // Step 3: Render JARVIS UI
        setContent {
            MyApplicationTheme {
                val viewModel: JarvisViewModel = viewModel()
                JarvisHomeScreen(viewModel = viewModel)
            }
        }
    }
}
