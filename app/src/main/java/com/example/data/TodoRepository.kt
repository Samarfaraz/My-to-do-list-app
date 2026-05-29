package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allItems: Flow<List<TodoItem>> = todoDao.getAllItems()

    suspend fun insert(item: TodoItem): Long {
        return todoDao.insertItem(item)
    }

    suspend fun update(item: TodoItem) {
        todoDao.updateItem(item)
    }

    suspend fun delete(item: TodoItem) {
        todoDao.deleteItem(item)
    }

    suspend fun deleteById(id: Int) {
        todoDao.deleteById(id)
    }

    suspend fun deleteCompleted() {
        todoDao.deleteCompletedItems()
    }

    suspend fun updateCompletion(id: Int, isCompleted: Boolean) {
        todoDao.updateCompletionStatus(id, isCompleted)
    }
}
