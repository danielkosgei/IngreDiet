package com.thenewkenya.ingrediet.feature.shopping

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Shopping List",
                        style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface
                ),
                actions = {
                    if (items.isNotEmpty()) {
                        AnimatedContent(
                            targetState = isInEditMode,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith 
                                fadeOut(animationSpec = tween(200))
                            }
                        ) { editMode ->
                            IconButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(
                                    imageVector = if (editMode) Icons.Default.Check else Icons.Outlined.Edit,
                                    contentDescription = if (editMode) "Done" else "Edit",
                                    tint = if (editMode) colors.primary else colors.onSurface
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Add Item",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            when {
                isLoading -> {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }
                error != null && items.isEmpty() -> {
                    ErrorState(
                        message = error ?: "An unknown error occurred",
                        onRetry = { viewModel.refreshItems() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                items.isEmpty() -> {
                    EmptyShoppingList(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Error banner
                        if (error != null) {
                            item {
                                ErrorBanner(
                                    message = error ?: "An unknown error occurred",
                                    onRetry = { viewModel.refreshItems() },
                                    onDismiss = { viewModel.dismissError() }
                                )
                            }
                        }
                        
                        // Shopping Progress Summary
                        if (items.isNotEmpty()) {
                            item {
                                ShoppingProgressCard(
                                    totalItems = items.size,
                                    completedItems = checkedItems.size,
                                    colors = colors,
                                    typography = typography
                                )
                            }
                        }
                        
                        // Unchecked items section
                        if (uncheckedItems.isNotEmpty()) {
                            item {
                                ModernCategoryHeader(
                                    title = "To Buy",
                                    count = uncheckedItems.size,
                                    icon = Icons.Outlined.ShoppingCart,
                                    colors = colors,
                                    typography = typography
                                )
                            }
                            
                            items(
                                items = uncheckedItems,
                                key = { it.id }
                            ) { item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ) + fadeIn(),
                                    exit = slideOutHorizontally() + shrinkVertically() + fadeOut()
                                ) {
                                    ModernShoppingItem(
                                        item = item,
                                        isInEditMode = isInEditMode,
                                        onCheckedChange = { viewModel.toggleItem(item.id) },
                                        onDelete = { viewModel.removeItem(item.id) }
                                    )
                                }
                            }
                        }
                        
                        // Checked items section
                        if (checkedItems.isNotEmpty()) {
                            item {
                                ModernCategoryHeader(
                                    title = "Completed",
                                    count = checkedItems.size,
                                    icon = Icons.Outlined.CheckCircle,
                                    colors = colors,
                                    typography = typography
                                )
                            }
                            
                            items(
                                items = checkedItems,
                                key = { it.id }
                            ) { item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically() + fadeIn(),
                                    exit = slideOutHorizontally() + shrinkVertically() + fadeOut()
                                ) {
                                    ModernShoppingItem(
                                        item = item,
                                        isInEditMode = isInEditMode,
                                        onCheckedChange = { viewModel.toggleItem(item.id) },
                                        onDelete = { viewModel.removeItem(item.id) }
                                    )
                                }
                            }
                            
                            item {
                                ClearCompletedButton(
                                    onClear = { viewModel.clearCheckedItems() },
                                    colors = colors,
                                    typography = typography
                                )
                            }
                        }
                        
                        // Bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideAddDialog() },
                sheetState = bottomSheetState,
                windowInsets = WindowInsets(0, 0, 0, 0),
                dragHandle = {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(width = 32.dp, height = 4.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = colors.outline.copy(alpha = 0.4f)
                    ) {}
                }
            ) {
                AddItemBottomSheet(
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
}

@Composable
fun ShoppingProgressCard(
    totalItems: Int,
    completedItems: Int,
    colors: ColorScheme,
    typography: Typography
) {
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shopping Progress",
                        style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onPrimaryContainer
                    )
                    Text(
                        text = "$completedItems of $totalItems items",
                        style = typography.bodyMedium,
                        color = colors.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(40.dp),
                        color = colors.primary,
                        strokeWidth = 4.dp,
                        trackColor = colors.primary.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ModernCategoryHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: ColorScheme,
    typography: Typography
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = colors.onBackground
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = CircleShape,
            color = colors.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    style = typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernShoppingItem(
    item: ShoppingItem,
    isInEditMode: Boolean,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.isChecked -> colors.surfaceVariant.copy(alpha = 0.3f)
                else -> colors.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                item.isChecked -> colors.outline.copy(alpha = 0.3f)
                else -> colors.outline.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom animated checkbox
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                color = if (item.isChecked) colors.primary else Color.Transparent,
                border = if (!item.isChecked) {
                    BorderStroke(2.dp, colors.outline.copy(alpha = 0.5f))
                } else null,
                onClick = onCheckedChange
            ) {
                AnimatedVisibility(
                    visible = item.isChecked,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    ) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = colors.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = if (item.isChecked) {
                        colors.onSurface.copy(alpha = 0.6f)
                    } else {
                        colors.onSurface
                    }
                )
                
                if (item.category.isNotBlank()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = getCategoryColor(item.category, colors).copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    style = typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = getCategoryColor(item.category, colors),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isInEditMode,
                enter = slideInHorizontally(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.errorContainer.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun getCategoryColor(category: String, colors: ColorScheme): Color {
    return when (category.lowercase()) {
        "dairy" -> Color(0xFF2196F3)
        "produce" -> Color(0xFF4CAF50)
        "meat" -> Color(0xFFF44336)
        "bakery" -> Color(0xFFFF9800)
        "frozen" -> Color(0xFF00BCD4)
        "canned" -> Color(0xFF9C27B0)
        "beverages" -> Color(0xFF3F51B5)
        "snacks" -> Color(0xFFFFEB3B)
        else -> colors.primary
    }
}

@Composable
fun ClearCompletedButton(
    onClear: () -> Unit,
    colors: ColorScheme,
    typography: Typography
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = onClear,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.error
            ),
            border = BorderStroke(1.dp, colors.error.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Clear Completed",
                style = typography.labelMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun EmptyShoppingList(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern empty state icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colors.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your shopping list is empty",
            style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add items to get started with your shopping",
            style = typography.bodyLarge,
            color = colors.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    newItemText: String,
    newItemCategory: String,
    onNewItemTextChange: (String) -> Unit,
    onNewItemCategoryChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val categories = listOf("Dairy", "Produce", "Meat", "Bakery", "Frozen", "Canned", "Beverages", "Snacks", "General")
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp) // Extra padding for safe area
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Add New Item",
                style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface
            )
        }
        
        // Item name input
        OutlinedTextField(
            value = newItemText,
            onValueChange = onNewItemTextChange,
            label = { 
                Text(
                    "Item Name",
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = colors.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category selection
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newItemCategory,
                onValueChange = onNewItemCategoryChange,
                label = { 
                    Text(
                        "Category",
                        style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = getCategoryColor(newItemCategory, colors),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle dropdown",
                            tint = colors.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline.copy(alpha = 0.3f)
                ),
                readOnly = false
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(12.dp),
                                    shape = CircleShape,
                                    color = getCategoryColor(category, colors)
                                ) {}
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    category,
                                    style = typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onNewItemCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action button - full width
        Button(
            onClick = onAddItem,
            enabled = newItemText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            border = BorderStroke(0.dp, Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Add Item",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = colors.primary,
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Loading your shopping list...",
            style = typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Please wait a moment",
            style = typography.bodyMedium,
            color = colors.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(
    message: String, 
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(colors.errorContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = colors.error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Oops! Something went wrong",
            style = typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = typography.bodyLarge,
            color = colors.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Try Again",
                style = typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
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
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = colors.error.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            Surface(
                onClick = onRetry,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = colors.error.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = colors.error.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
} 