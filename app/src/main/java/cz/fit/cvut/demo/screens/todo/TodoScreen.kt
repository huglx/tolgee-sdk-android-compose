package cz.fit.cvut.demo.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.fit.cvut.demo.data.TodoEntity
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageDropdown
import cz.fit.cvut.feature.translation.presentation.common.component.Translate
import cz.fit.cvut.feature.translation.presentation.common.component.t
import cz.fit.cvut.feature.translations_context.utils.LocalScreenId
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    onNavigateToTranslations: () -> Unit = {}
) {
    val viewModel: TodoViewModel = getKoin().get()
    val todos by viewModel.todos.collectAsState()
    val newTodoText by viewModel.newTodoText.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = t(keyName = "todo_appbar_title",))

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    TolgeeLanguageDropdown()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addTodo() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        startY = 0f,
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                TodoInputField(
                    value = newTodoText,
                    onValueChange = { viewModel.updateNewTodoText(it) },
                    onAddClick = { viewModel.addTodo() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Translate(
                        keyName = "todo_list_title",
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 4.dp)

                    )

                    Button(
                        onClick = onNavigateToTranslations,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Translate(keyName = "button.translations")
                    }
                }

                TodoList(
                    todos = todos,
                    onToggleCompletion = { todo, isCompleted ->
                        viewModel.toggleTodoCompletion(todo.id, isCompleted)
                    },
                    onDeleteTodo = { viewModel.deleteTodo(it) }
                )

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Translate(keyName = "todo_input_placeholder") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Translate(keyName = "todo_button_add")
        }
    }
}

@Composable
fun TodoList(
    todos: List<TodoEntity>,
    onToggleCompletion: (TodoEntity, Boolean) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(todos) { todo ->
            TodoItem(
                todo = todo,
                onToggleCompletion = onToggleCompletion,
                onDeleteTodo = onDeleteTodo
            )
        }
        
        if (todos.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Translate(
                        keyName = "todo_empty_message",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: TodoEntity,
    onToggleCompletion: (TodoEntity, Boolean) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { isChecked ->
                    onToggleCompletion(todo, isChecked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            
            IconButton(onClick = { onDeleteTodo(todo) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 