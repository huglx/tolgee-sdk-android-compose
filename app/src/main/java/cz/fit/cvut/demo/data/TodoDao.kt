package cz.fit.cvut.demo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY id DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    
    @Insert
    suspend fun insertTodo(todo: TodoEntity): Long
    
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    
    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteTodoById(todoId: Long)
    
    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :todoId")
    suspend fun updateTodoCompletion(todoId: Long, isCompleted: Boolean)
} 