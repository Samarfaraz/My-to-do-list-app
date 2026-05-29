package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.TodoDatabase
import com.example.data.TodoRepository
import com.example.ui.TodoDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TodoViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AeroTask", appName)
  }

  @Test
  fun `render dashboard with viewModel succeeds`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val repository = TodoRepository(database.todoDao())
    val viewModel = TodoViewModel(repository)

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = false) {
        TodoDashboard(
          viewModel = viewModel,
          isDarkTheme = false,
          onToggleTheme = {}
        )
      }
    }
    
    composeTestRule.waitForIdle()
    database.close()
  }
}
