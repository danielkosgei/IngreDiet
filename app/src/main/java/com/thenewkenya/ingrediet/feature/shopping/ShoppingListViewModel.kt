package com.thenewkenya.ingrediet.feature.shopping

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thenewkenya.ingrediet.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: String,
    val name: String,
    val category: String = "",
    val isChecked: Boolean = false
)

class ShoppingListViewModel(
    private val context: Context
) : ViewModel() {
    private val repository = ShoppingListRepository(context)
    private val TAG = "ShoppingListViewModel"
    
    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _newItemText = MutableStateFlow("")
    val newItemText: StateFlow<String> = _newItemText.asStateFlow()
    
    private val _newItemCategory = MutableStateFlow("General")
    val newItemCategory: StateFlow<String> = _newItemCategory.asStateFlow()
    
    private val _isInEditMode = MutableStateFlow(false)
    val isInEditMode: StateFlow<Boolean> = _isInEditMode.asStateFlow()

    private val categories = listOf("Dairy", "Produce", "Meat", "Bakery", "Frozen", "Canned", "Beverages", "Snacks", "General")

    val uncheckedItems: List<ShoppingItem>
        get() = _items.value.filter { !it.isChecked }

    val checkedItems: List<ShoppingItem>
        get() = _items.value.filter { it.isChecked }

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getShoppingItems()
                .catch { e ->
                    Log.e(TAG, "Error loading items", e)
                    _error.value = e.message
                    _isLoading.value = false
                    
                    // Set some sample data when there's an error for a better user experience
                    if (_items.value.isEmpty()) {
                        setSampleData()
                    }
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { items ->
                            _items.value = items
                            Log.d(TAG, "Loaded ${items.size} items")
                        },
                        onFailure = { error ->
                            _error.value = error.message
                            Log.e(TAG, "Error loading items", error)
                            
                            // Set some sample data when there's an error for a better user experience
                            if (_items.value.isEmpty()) {
                                setSampleData()
                            }
                        }
                    )
                    _isLoading.value = false
                }
        }
    }

    private fun setSampleData() {
        _items.value = listOf(
            ShoppingItem(id = "1", name = "Milk", category = "Dairy", isChecked = false),
            ShoppingItem(id = "2", name = "Bread", category = "Bakery", isChecked = true),
            ShoppingItem(id = "3", name = "Eggs", category = "Dairy", isChecked = false),
            ShoppingItem(id = "4", name = "Apples", category = "Produce", isChecked = false),
            ShoppingItem(id = "5", name = "Chicken", category = "Meat", isChecked = false)
        )
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
        _newItemText.value = ""
        _newItemCategory.value = "General"
    }

    fun updateNewItemText(text: String) {
        _newItemText.value = text
    }
    
    fun updateNewItemCategory(category: String) {
        _newItemCategory.value = category
    }
    
    fun toggleEditMode() {
        _isInEditMode.value = !_isInEditMode.value
    }

    fun addItem() {
        val text = _newItemText.value.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                val newItem = ShoppingItem(
                    id = java.util.UUID.randomUUID().toString(),
                    name = text,
                    category = _newItemCategory.value.trim(),
                    isChecked = false
                )
                
                repository.addShoppingItem(newItem)
                    .catch { e -> 
                        Log.e(TAG, "Error adding item", e)
                        _error.value = e.message
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = {
                                _items.value = _items.value + newItem
                                Log.d(TAG, "Added item: ${newItem.name}")
                            },
                            onFailure = { error ->
                                _error.value = error.message
                                Log.e(TAG, "Error adding item", error)
                            }
                        )
                    }
            }
        }
    }

    fun toggleItem(itemId: String) {
        viewModelScope.launch {
            val item = _items.value.find { it.id == itemId } ?: return@launch
            val updatedItem = item.copy(isChecked = !item.isChecked)
            
            try {
                repository.updateShoppingItem(updatedItem)
                // Update the list immediately for better UX
                _items.value = _items.value.map { 
                    if (it.id == itemId) updatedItem else it 
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling item: ${e.message}", e)
                _error.value = "Failed to update item: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteShoppingItem(itemId)
                .catch { e -> 
                    Log.e(TAG, "Error removing item", e)
                    _error.value = e.message
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = {
                            _items.value = _items.value.filter { it.id != itemId }
                            Log.d(TAG, "Removed item: $itemId")
                        },
                        onFailure = { error ->
                            _error.value = error.message
                            Log.e(TAG, "Error removing item", error)
                        }
                    )
                }
        }
    }
    
    fun clearCheckedItems() {
        viewModelScope.launch {
            val checkedItemIds = _items.value.filter { it.isChecked }.map { it.id }
            
            repository.deleteCheckedItems(checkedItemIds)
                .catch { e -> 
                    Log.e(TAG, "Error clearing checked items", e)
                    _error.value = e.message
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = {
                            _items.value = _items.value.filter { !it.isChecked }
                            Log.d(TAG, "Cleared checked items")
                        },
                        onFailure = { error ->
                            _error.value = error.message
                            Log.e(TAG, "Error clearing checked items", error)
                        }
                    )
                }
        }
    }
    
    fun clearAllItems() {
        viewModelScope.launch {
            repository.deleteAllItems()
                .catch { e -> 
                    Log.e(TAG, "Error clearing all items", e)
                    _error.value = e.message
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = {
                            _items.value = emptyList()
                            Log.d(TAG, "Cleared all items")
                        },
                        onFailure = { error ->
                            _error.value = error.message
                            Log.e(TAG, "Error clearing all items", error)
                        }
                    )
                }
        }
    }
    
    fun refreshItems() {
        _error.value = null
        _isLoading.value = true
        loadItems()
    }

    fun dismissError() {
        _error.value = null
    }
}

class ShoppingListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
