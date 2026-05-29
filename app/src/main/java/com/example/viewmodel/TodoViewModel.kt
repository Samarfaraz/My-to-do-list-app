package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TodoDatabase
import com.example.data.TodoItem
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")
    val selectedPriorityFilter = MutableStateFlow("All")
    val sortBy = MutableStateFlow("Date Created") // "Date Created", "Priority", "Title", "Due Date"

    // Form states
    val inputTitle = MutableStateFlow("")
    val inputDescription = MutableStateFlow("")
    val inputPriority = MutableStateFlow("MEDIUM") // "LOW", "MEDIUM", "HIGH"
    val inputCategory = MutableStateFlow("Personal") // "Personal", "Work", "Wellness", "Academic", "Other"
    val inputDueDate = MutableStateFlow<Long?>(null)
    val editingItem = MutableStateFlow<TodoItem?>(null)

    // Derived UI state list, combining database results with filter & sort states reactive flows
    val todoItems: StateFlow<List<TodoItem>> = combine(
        repository.allItems,
        searchQuery,
        selectedCategoryFilter,
        selectedPriorityFilter,
        sortBy
    ) { items, query, category, priority, sortOrder ->
        // 1. Filter by search query
        var filtered = items.filter {
            it.title.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true)
        }

        // 2. Filter by category
        if (category != "All") {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 3. Filter by priority
        if (priority != "All") {
            filtered = filtered.filter { it.priority.equals(priority, ignoreCase = true) }
        }

        // 4. Custom sorting
        // Note: Repository already sorts completed tasks to the bottom, this secondary sorting handles the relative order within groups.
        when (sortOrder) {
            "Priority" -> {
                filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted } // completed items always at bottom
                        .thenBy { 
                            when (it.priority) {
                                "HIGH" -> 0
                                "MEDIUM" -> 1
                                "LOW" -> 2
                                else -> 3
                            }
                        }
                        .thenByDescending { it.createdAt }
                )
            }
            "Title" -> {
                filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenBy { it.title.lowercase() }
                )
            }
            "Due Date" -> {
                filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenBy { it.dueDate ?: Long.MAX_VALUE }
                        .thenByDescending { it.createdAt }
                )
            }
            else -> { // "Date Created" -> newest first
                filtered.sortedWith(
                    compareBy<TodoItem> { it.isCompleted }
                        .thenByDescending { it.createdAt }
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Stats
    val stats: StateFlow<TodoStats> = repository.allItems.map { items ->
        val total = items.size
        val completed = items.count { it.isCompleted }
        val pending = total - completed
        val highPriorityPending = items.count { !it.isCompleted && it.priority == "HIGH" }
        
        TodoStats(
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            highPriorityPendingCount = highPriorityPending,
            completionRate = if (total > 0) completed.toFloat() / total else 0f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoStats()
    )

    // Actions
    fun insertOrUpdateTodo() {
        val titleValue = inputTitle.value.trim()
        if (titleValue.isEmpty()) return

        viewModelScope.launch {
            val currentEditing = editingItem.value
            if (currentEditing != null) {
                // Update
                val updated = currentEditing.copy(
                    title = titleValue,
                    description = inputDescription.value.trim(),
                    priority = inputPriority.value,
                    category = inputCategory.value,
                    dueDate = inputDueDate.value
                )
                repository.update(updated)
            } else {
                // Insert
                val newItem = TodoItem(
                    title = titleValue,
                    description = inputDescription.value.trim(),
                    priority = inputPriority.value,
                    category = inputCategory.value,
                    dueDate = inputDueDate.value,
                    isCompleted = false
                )
                repository.insert(newItem)
            }
            resetForm()
        }
    }

    fun toggleTodoCompleted(item: TodoItem) {
        viewModelScope.launch {
            repository.updateCompletion(item.id, !item.isCompleted)
        }
    }

    fun editTodo(item: TodoItem) {
        editingItem.value = item
        inputTitle.value = item.title
        inputDescription.value = item.description
        inputPriority.value = item.priority
        inputCategory.value = item.category
        inputDueDate.value = item.dueDate
    }

    fun deleteTodo(item: TodoItem) {
        viewModelScope.launch {
            repository.delete(item)
            // If deleting the item currently being edited, reset form
            if (editingItem.value?.id == item.id) {
                resetForm()
            }
        }
    }

    fun clearCompletedTodos() {
        viewModelScope.launch {
            repository.deleteCompleted()
        }
    }

    fun resetForm() {
        editingItem.value = null
        inputTitle.value = ""
        inputDescription.value = ""
        inputPriority.value = "MEDIUM"
        inputCategory.value = "Personal"
        inputDueDate.value = null
    }

    fun updateFilters(query: String? = null, category: String? = null, priority: String? = null, sort: String? = null) {
        query?.let { searchQuery.value = it }
        category?.let { selectedCategoryFilter.value = it }
        priority?.let { selectedPriorityFilter.value = it }
        sort?.let { sortBy.value = it }
    }
}

data class TodoStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val highPriorityPendingCount: Int = 0,
    val completionRate: Float = 0f
)

// ViewModel Provider Factory
class TodoViewModelFactory(private val repository: TodoRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
