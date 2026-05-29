package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val category: String = "Personal", // "Personal", "Work", "Wellness", "Academic", "Other"
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
