package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TodoItem
import com.example.ui.theme.*
import com.example.viewmodel.TodoStats
import com.example.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDashboard(
    viewModel: TodoViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.todoItems.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val currentPriority by viewModel.selectedPriorityFilter.collectAsStateWithLifecycle()
    val currentSort by viewModel.sortBy.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Categories and priorities constants
    val categories = listOf("All", "Personal", "Work", "Shopping", "Wellness", "Academic")
    val priorities = listOf("All", "HIGH", "MEDIUM", "LOW")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("todo_scaffold"),
        topBar = {
            // Emptied topBar to support custom full-bleed layout.
            // Spacing is fully managed inside the inner Column with statusBarsPadding()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.resetForm()
                    showAddDialog = true 
                },
                // Matches secondary/primaryContainer hue and rounded-xl corners in HTML
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            
            // 0. BOLD TYPOGRAPHY DESIGN HEADER (pt-12 px-6 pb-6 equivalent)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Brand/CheckCircle decorative element
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "AeroTask",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Right Actions: Theme switch button and Sammer Faraz initials badge (SF)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Refresh else Icons.Default.Settings,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SF",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Material 3 Large Bold Header text: "Tasks"
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.testTag("large_header_title")
                )
            }
            
            // 1. STATS BANNER
            StatsBannerCard(
                stats = stats, 
                onClearCompleted = { viewModel.clearCompletedTodos() }
            )

            // 2. SEARCH BAR & SORT SELECTOR PANEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateFilters(query = it) },
                    placeholder = { Text("Search tasks...", fontSize = 14.sp) },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp)
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateFilters(query = "") }) {
                                Icon(
                                    imageVector = Icons.Default.Close, 
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("sort_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Sort tasks",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        val sortOptions = listOf("Date Created", "Priority", "Title", "Due Date")
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateFilters(sort = option)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (currentSort == option) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 3. HORIZONTAL RUNNING CHIPS: CATEGORY FILTERS
            ScrollableRowFilters(
                options = categories,
                selectedOption = currentCategory,
                onSelected = { viewModel.updateFilters(category = it) },
                label = "Category",
                modifier = Modifier.testTag("category_filters_row")
            )

            // 4. SECOND ROW CHIPS: PRIORITY FILTERS
            RowFilters(
                options = priorities,
                selectedOption = currentPriority,
                onSelected = { viewModel.updateFilters(priority = it) },
                label = "Priority",
                modifier = Modifier.testTag("priority_filters_row")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 5. MAIN WORKSPACE LIST (Animated Transition when changing items)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (items.isEmpty()) {
                    EmptyStateView(
                        hasQueryFilters = searchQuery.isNotEmpty() || currentCategory != "All" || currentPriority != "All"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("todo_items_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = items,
                            key = { it.id }
                        ) { item ->
                            TodoItemRow(
                                item = item,
                                onToggleComplete = { viewModel.toggleTodoCompleted(item) },
                                onEdit = {
                                    viewModel.editTodo(item)
                                    showAddDialog = true
                                },
                                onDelete = { viewModel.deleteTodo(item) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    // 6. ADD & EDIT TASK MODAL DIALOG
    if (showAddDialog) {
        TaskEditorDialog(
            viewModel = viewModel,
            onDismiss = { 
                viewModel.resetForm()
                showAddDialog = false 
            },
            onSave = {
                viewModel.insertOrUpdateTodo()
                showAddDialog = false
            }
        )
    }
}

// ----------------------------------------------------
// STATS CARD COMPONENT
// ----------------------------------------------------
@Composable
fun StatsBannerCard(
    stats: TodoStats,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Task Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.completedCount} of ${stats.totalCount} completed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (stats.completedCount > 0) {
                    TextButton(
                        onClick = onClearCompleted,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("clear_completed_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Completed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated progress value
            val animatedProgress by animateFloatAsState(
                targetValue = stats.completionRate,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "Progress bar calculation"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            if (stats.highPriorityPendingCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PriorityHigh)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stats.highPriorityPendingCount} high priority tasks pending!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PriorityHigh
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// FILTER CHIPS ROW COMPONENTS
// ----------------------------------------------------
@Composable
fun ScrollableRowFilters(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = selectedOption == option
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelected(option) },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (option != "All") {
                                Icon(
                                    imageVector = getCategoryIcon(option),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(option, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        selectedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        disabledSelectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }
        }
    }
}

@Composable
fun RowFilters(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = selectedOption == option
                val displayLabel = when(option) {
                    "HIGH" -> "High 🔥"
                    "MEDIUM" -> "Med ⚠️"
                    "LOW" -> "Low 💤"
                    else -> "All"
                }

                val customColor = when(option) {
                    "HIGH" -> PriorityHigh
                    "MEDIUM" -> PriorityMedium
                    "LOW" -> PriorityLow
                    else -> MaterialTheme.colorScheme.primary
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelected(option) },
                    label = { Text(displayLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = customColor.copy(alpha = 0.25f),
                        selectedLabelColor = customColor,
                        selectedLeadingIconColor = customColor,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        selectedBorderColor = customColor,
                        disabledBorderColor = Color.Transparent,
                        disabledSelectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }
        }
    }
}

// ----------------------------------------------------
// SINGLE TODO ITEM ROW COMPONENT
// ----------------------------------------------------
@Composable
fun TodoItemRow(
    item: TodoItem,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (item.priority) {
        "HIGH" -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        "LOW" -> PriorityLow
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val containerAlpha = if (item.isCompleted) 0.5f else 1.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (item.isCompleted) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                width = 1.2.dp,
                color = priorityColor.copy(alpha = 0.25f)
            )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox completion selector (Custom square checkbox matching the HTML spec)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onToggleComplete() }
                    .testTag("checkbox_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Incomplete",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Transparent)
                            .border(2.dp, priorityColor, RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text contents Column
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = containerAlpha))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getCategoryIcon(item.category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.15f * containerAlpha))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.priority,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = priorityColor.copy(alpha = containerAlpha)
                        )
                    }

                    // Due date if exists
                    item.dueDate?.let { dueDateMs ->
                        val overdue = dueDateMs < System.currentTimeMillis() && !item.isCompleted
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (overdue) PriorityHigh.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = containerAlpha)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (overdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formatDueDate(dueDateMs),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = if (item.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions panel: Edit and Delete
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!item.isCompleted) {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// ILLUSTRATED EMPTY STATE COMPONENT
// ----------------------------------------------------
@Composable
fun EmptyStateView(
    hasQueryFilters: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasQueryFilters) Icons.Default.Search else Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (hasQueryFilters) "No Matching Tasks Found" else "All Cleared!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasQueryFilters) {
                "Try relaxing your search terms or picking another category combination."
            } else {
                "Your slate is completely clean. Click + to compose your next milestone."
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ----------------------------------------------------
// THE ADD/EDIT TASK MODAL DIALOG COMPONENT
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    viewModel: TodoViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val title by viewModel.inputTitle.collectAsStateWithLifecycle()
    val description by viewModel.inputDescription.collectAsStateWithLifecycle()
    val priority by viewModel.inputPriority.collectAsStateWithLifecycle()
    val category by viewModel.inputCategory.collectAsStateWithLifecycle()
    val dueDate by viewModel.inputDueDate.collectAsStateWithLifecycle()
    val isEditing = viewModel.editingItem.collectAsStateWithLifecycle().value != null

    val categories = listOf("Personal", "Work", "Shopping", "Wellness", "Academic")
    val priorities = listOf("LOW", "MEDIUM", "HIGH")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("task_editor_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Task" else "Create New Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.inputTitle.value = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("What needs to be done?") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.inputDescription.value = it },
                    label = { Text("Description") },
                    placeholder = { Text("Add optional details...") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_description_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Category Tag",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        InputChip(
                            selected = isSelected,
                            onClick = { viewModel.inputCategory.value = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Selection
                Text(
                    text = "Priority Level",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { prio ->
                        val isSelected = priority == prio
                        val color = when (prio) {
                            "HIGH" -> PriorityHigh
                            "MEDIUM" -> PriorityMedium
                            "LOW" -> PriorityLow
                            else -> MaterialTheme.colorScheme.secondary
                        }

                        InputChip(
                            selected = isSelected,
                            onClick = { viewModel.inputPriority.value = prio },
                            label = { Text(prio, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = color.copy(alpha = 0.25f),
                                selectedLabelColor = color
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date Shortcuts
                Text(
                    text = "Set Due Date",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("None", "Today", "Tomorrow", "In 3 Days").forEach { option ->
                        val currentText = if (dueDate == null) "None" else formatDueDate(dueDate)
                        
                        // Check if selected
                        val isSelected = when (option) {
                            "None" -> dueDate == null
                            "Today" -> {
                                val c = Calendar.getInstance()
                                dueDate != null && isSameDay(dueDate!!, c.timeInMillis)
                            }
                            "Tomorrow" -> {
                                val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                dueDate != null && isSameDay(dueDate!!, c.timeInMillis)
                            }
                            "In 3 Days" -> {
                                val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
                                dueDate != null && isSameDay(dueDate!!, c.timeInMillis)
                            }
                            else -> false
                        }

                        InputChip(
                            selected = isSelected,
                            onClick = {
                                when (option) {
                                    "None" -> viewModel.inputDueDate.value = null
                                    "Today" -> viewModel.inputDueDate.value = System.currentTimeMillis()
                                    "Tomorrow" -> {
                                        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                        viewModel.inputDueDate.value = c.timeInMillis
                                    }
                                    "In 3 Days" -> {
                                        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
                                        viewModel.inputDueDate.value = c.timeInMillis
                                    }
                                }
                            },
                            label = { Text(option, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Dismiss & Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_dismiss_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = title.trim().isNotEmpty(),
                        modifier = Modifier.testTag("dialog_save_button")
                    ) {
                        Text(if (isEditing) "Save Shifts" else "Add Item")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// UTILITY DATE FORMATTING HELPERS
// ----------------------------------------------------
private fun formatDueDate(timestamp: Long?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val today = Calendar.getInstance()
    val check = Calendar.getInstance().apply { timeInMillis = timestamp }
    
    return when {
         today.get(Calendar.YEAR) == check.get(Calendar.YEAR) &&
         today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) -> "Today"
         
         today.get(Calendar.YEAR) == check.get(Calendar.YEAR) &&
         today.get(Calendar.DAY_OF_YEAR) + 1 == check.get(Calendar.DAY_OF_YEAR) -> "Tomorrow"
         
         else -> sdf.format(Date(timestamp))
    }
}

private fun isSameDay(ms1: Long, ms2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ms1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ms2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun getCategoryIcon(category: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category?.lowercase(Locale.getDefault())) {
        "personal" -> Icons.Default.Person
        "work" -> Icons.Default.Build
        "shopping" -> Icons.Default.ShoppingCart
        "wellness" -> Icons.Default.Favorite
        "academic" -> Icons.Default.Info
        else -> Icons.Default.Star
    }
}
