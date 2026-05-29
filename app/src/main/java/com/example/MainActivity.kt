package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TodoDatabase
import com.example.data.TodoRepository
import com.example.ui.TodoDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TodoViewModel
import com.example.viewmodel.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local Storage Architecture (Room + Repository)
        val database = TodoDatabase.getDatabase(applicationContext)
        val repository = TodoRepository(database.todoDao())
        val viewModelFactory = TodoViewModelFactory(repository)

        setContent {
            // Local state for user-controlled Dark Mode toggle
            val systemIsInDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemIsInDark) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val todoViewModel: TodoViewModel = viewModel(factory = viewModelFactory)
                    TodoDashboard(
                        viewModel = todoViewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}
