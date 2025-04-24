package cz.fit.cvut.demo.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.demo.data.TodoEntity
import cz.fit.cvut.demo.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    
    private val _newTodoText = MutableStateFlow("")
    val newTodoText: StateFlow<String> = _newTodoText
    
    val todos = repository.getAllTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun updateNewTodoText(text: String) {
        _newTodoText.value = text
    }
    
    fun addTodo() {
        val text = _newTodoText.value.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                repository.addTodo(text)
                _newTodoText.value = ""
            }
        }
    }
    
    fun toggleTodoCompletion(todoId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTodoCompletion(todoId, isCompleted)
        }
    }
    
    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }
} 