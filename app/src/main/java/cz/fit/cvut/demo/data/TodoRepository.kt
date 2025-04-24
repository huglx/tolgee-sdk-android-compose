package cz.fit.cvut.demo.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    
    suspend fun addTodo(title: String): Long {
        val todo = TodoEntity(title = title)
        return todoDao.insertTodo(todo)
    }
    
    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)
    
    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)
    
    suspend fun toggleTodoCompletion(todoId: Long, isCompleted: Boolean) = 
        todoDao.updateTodoCompletion(todoId, isCompleted)
} 