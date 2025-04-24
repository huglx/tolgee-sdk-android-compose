package cz.fit.cvut.demo.screens.packing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageDropdown
import cz.fit.cvut.feature.translation.presentation.common.component.Translate
import cz.fit.cvut.feature.translations_context.utils.LocalScreenId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingListScreen() {
    // State for the new item input
    var newItemText by remember { mutableStateOf("") }
    
    // Sample items for the packing list
    val packingItems = remember {
        mutableStateListOf(
            "packing.items.passport",
            "packing.items.maps",
            "packing.items.travel_guide",
            "packing.items.camera"
        )
    }
    
    // Background gradient colors
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4285F4), // Light blue
            Color(0xFF2A56C6)  // Darker blue
        )
    )
    
    CompositionLocalProvider(LocalScreenId provides "packing") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top language selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(Color.White)
                        )
                    ) {
                        Translate(keyName = "packing.translation_methods")
                    }
                    
                    TolgeeLanguageDropdown()
                }
                
                // App logo and title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "A°",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "What To Pack",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Main content card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F1FD)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // New item input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                placeholder = { 
                                    Translate(keyName = "packing.new_item_placeholder") 
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = Color.White
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    if (newItemText.isNotEmpty()) {
                                        // In a real app, we would add this to the database
                                        // and use proper item IDs
                                        packingItems.add(newItemText)
                                        newItemText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF888888)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Translate(keyName = "packing.button.add")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // List of items
                        LazyColumn {
                            items(packingItems) { item ->
                                PackingListItem(
                                    translationKey = item,
                                    onDelete = { packingItems.remove(item) }
                                )
                            }
                        }
                    }
                }
                
                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { /* Implement share functionality */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F3542)
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Translate(keyName = "packing.share")
                    }
                    
                    Button(
                        onClick = { /* Implement email functionality */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F3542)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Translate(keyName = "packing.send_email")
                    }
                }
            }
        }
    }
}

@Composable
fun PackingListItem(
    translationKey: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (translationKey.startsWith("packing.items.")) {
            Translate(
                keyName = translationKey,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = translationKey,
                modifier = Modifier.weight(1f)
            )
        }
        
        TextButton(
            onClick = onDelete,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Red
            )
        ) {
            Translate(keyName = "packing.button.delete")
        }
    }

    Divider(color = Color.LightGray, thickness = 1.dp)
} 