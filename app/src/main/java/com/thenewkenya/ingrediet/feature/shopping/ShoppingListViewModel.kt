package com.thenewkenya.ingrediet.feature.shopping

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ShoppingItem(
    val id: String,
    val name: String,
    val isChecked: Boolean = false
)

class ShoppingListViewModel(
    context: Context
) : ViewModel() {
    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _newItemText = MutableStateFlow("")
    val newItemText: StateFlow<String> = _newItemText.asStateFlow()

    val uncheckedItems: List<ShoppingItem>
        get() = _items.value.filter { !it.isChecked }

    val checkedItems: List<ShoppingItem>
        get() = _items.value.filter { it.isChecked }

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            // TODO: Load items from repository
            // For now, using sample data
            _items.value = listOf(
                ShoppingItem(id = "1", name = "Milk", isChecked = false),
                ShoppingItem(id = "2", name = "Bread", isChecked = true),
                ShoppingItem(id = "3", name = "Eggs", isChecked = false)
            )
        }
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
        _newItemText.value = ""
    }

    fun updateNewItemText(text: String) {
        _newItemText.value = text
    }

    fun addItem() {
        val text = _newItemText.value.trim()
        if (text.isNotEmpty()) {
            val newItem = ShoppingItem(
                id = UUID.randomUUID().toString(),
                name = text,
                isChecked = false
            )
            _items.value = _items.value + newItem
        }
    }

    fun toggleItem(itemId: String) {
        _items.value = _items.value.map { item ->
            if (item.id == itemId) {
                item.copy(isChecked = !item.isChecked)
            } else {
                item
            }
        }
    }

    fun removeItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
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
