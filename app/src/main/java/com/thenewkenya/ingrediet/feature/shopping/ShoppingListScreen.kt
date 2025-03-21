package com.thenewkenya.ingrediet.feature.shopping

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.thenewkenya.ingrediet.feature.shopping.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: ShoppingListViewModel = viewModel(
        factory = ShoppingListViewModelFactory(LocalContext.current)
    )
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    val items by viewModel.items.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val newItemText by viewModel.newItemText.collectAsState()
    val newItemCategory by viewModel.newItemCategory.collectAsState()
    val isInEditMode by viewModel.isInEditMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val uncheckedItems = items.filter { !it.isChecked }
    val checkedItems = items.filter { it.isChecked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                ),
                actions = {
                    if (items.isNotEmpty()) {
                        if (isInEditMode) {
                            IconButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(Icons.Default.Check, contentDescription = "Done")
                            }
                        } else {
                            IconButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Edit")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading state
            if (isLoading) {
                LoadingState(modifier = Modifier.align(Alignment.Center))
            } 
            // Show error state if there's an error and no items
            else if (error != null && items.isEmpty()) {
                ErrorState(
                    message = error ?: "An unknown error occurred",
                    onRetry = { viewModel.refreshItems() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Show empty state if no items and no error
            else if (items.isEmpty()) {
                EmptyShoppingList(modifier = Modifier.align(Alignment.Center))
            }
            // Show items
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show error banner if there's an error but we have items to show
                    if (error != null) {
                        item {
                            ErrorBanner(
                                message = error ?: "An unknown error occurred",
                                onRetry = { viewModel.refreshItems() },
                                onDismiss = { viewModel.dismissError() }
                            )
                        }
                    }
                    
                    // Unchecked items
                    if (uncheckedItems.isNotEmpty()) {
                        item {
                            CategoryHeader(
                                title = "To Buy",
                                count = uncheckedItems.size,
                                colors = colors,
                                typography = typography
                            )
                        }
                        
                        items(uncheckedItems) { item ->
                            ShoppingItem(
                                item = item,
                                isInEditMode = isInEditMode,
                                onCheckedChange = { viewModel.toggleItem(item.id) },
                                onDelete = { viewModel.removeItem(item.id) }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Checked items
                    if (checkedItems.isNotEmpty()) {
                        item {
                            CategoryHeader(
                                title = "Already in Cart",
                                count = checkedItems.size,
                                colors = colors,
                                typography = typography
                            )
                        }
                        
                        items(checkedItems) { item ->
                            ShoppingItem(
                                item = item,
                                isInEditMode = isInEditMode,
                                onCheckedChange = { viewModel.toggleItem(item.id) },
                                onDelete = { viewModel.removeItem(item.id) }
                            )
                        }
                        
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.clearCheckedItems() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Clear Completed Items")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddItemDialog(
                newItemText = newItemText,
                newItemCategory = newItemCategory,
                onNewItemTextChange = viewModel::updateNewItemText,
                onNewItemCategoryChange = viewModel::updateNewItemCategory,
                onAddItem = {
                    viewModel.addItem()
                    viewModel.hideAddDialog()
                },
                onDismiss = { viewModel.hideAddDialog() }
            )
        }
    }
}

@Composable
fun CategoryHeader(
    title: String,
    count: Int,
    colors: ColorScheme,
    typography: Typography
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = colors.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Badge(
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        ) {
            Text(text = count.toString())
        }
        Spacer(modifier = Modifier.weight(1f))
    }
    HorizontalDivider(
        color = colors.outlineVariant,
        thickness = 1.dp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItem(
    item: ShoppingItem,
    isInEditMode: Boolean,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isChecked) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    uncheckedColor = colors.outline
                )
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = if (item.isChecked) colors.onSurface.copy(alpha = 0.6f) else colors.onSurface
                )
                
                if (item.category.isNotBlank()) {
                    Text(
                        text = item.category,
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isInEditMode,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyShoppingList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Text(
            text = "Your shopping list is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add your first item",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    newItemText: String,
    newItemCategory: String,
    onNewItemTextChange: (String) -> Unit,
    onNewItemCategoryChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf("Dairy", "Produce", "Meat", "Bakery", "Frozen", "Canned", "Beverages", "Snacks", "General")
    var expanded by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Item",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Item name input
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = onNewItemTextChange,
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category dropdown using a simpler approach
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newItemCategory,
                        onValueChange = onNewItemCategoryChange,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle dropdown"
                                )
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    onNewItemCategoryChange(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAddItem,
                        enabled = newItemText.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading your shopping list...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ErrorState(
    message: String, 
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
} 