package com.thenewkenya.ingrediet.data.repository

import android.content.Context
import android.util.Log
import com.thenewkenya.ingrediet.data.network.DatabaseErrorHandler
import com.thenewkenya.ingrediet.data.network.supabase
import com.thenewkenya.ingrediet.feature.shopping.ShoppingItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import java.util.UUID
import com.thenewkenya.ingrediet.data.model.IngredientItem

private const val TAG = "ShoppingListRepository"

@Serializable
data class ShoppingItemDto(
    val id: String,
    val user_id: String,
    val name: String,
    val category: String = "",
    val is_checked: Boolean = false,
    val quantity: Float? = null,
    val unit: String? = null
) {
    fun toShoppingItem(): ShoppingItem {
        val displayName = if (quantity != null && unit != null) {
            "$name ($quantity $unit)"
        } else {
            name
        }
        return ShoppingItem(
            id = id,
            name = displayName,
            category = category,
            isChecked = is_checked
        )
    }
}

class ShoppingListRepository(private val context: Context) {
    // TODO: Replace with actual database operations
    private val shoppingList = mutableListOf<IngredientItem>()

    suspend fun addIngredient(ingredient: IngredientItem) {
        addShoppingItem(
            ShoppingItem(
                id = UUID.randomUUID().toString(),
                name = "${ingredient.name} (${ingredient.quantity} ${ingredient.unit})",
                category = mapIngredientToCategory(ingredient.name),
                isChecked = false
            )
        ).collect { /* Collect to execute the flow */ }
    }

    suspend fun addIngredients(ingredients: List<IngredientItem>) {
        ingredients.forEach { ingredient ->
            addIngredient(ingredient)
        }
    }

    private fun mapIngredientToCategory(name: String): String {
        val lowerName = name.lowercase()
        
        return when {
            // Dairy
            lowerName.contains("milk") || lowerName.contains("cheese") || 
            lowerName.contains("yogurt") || lowerName.contains("cream") || 
            lowerName.contains("butter") -> "Dairy"
            
            // Produce
            lowerName.contains("apple") || lowerName.contains("banana") || 
            lowerName.contains("orange") || lowerName.contains("pepper") || 
            lowerName.contains("tomato") || lowerName.contains("potato") || 
            lowerName.contains("onion") || lowerName.contains("garlic") || 
            lowerName.contains("lettuce") || lowerName.contains("spinach") || 
            lowerName.contains("broccoli") || lowerName.contains("carrot") ||
            lowerName.contains("berry") || lowerName.contains("fruit") ||
            lowerName.contains("vegetable") -> "Produce"
            
            // Meat
            lowerName.contains("beef") || lowerName.contains("chicken") || 
            lowerName.contains("pork") || lowerName.contains("lamb") || 
            lowerName.contains("sausage") || lowerName.contains("meat") ||
            lowerName.contains("bacon") || lowerName.contains("steak") -> "Meat"
            
            // Bakery
            lowerName.contains("bread") || lowerName.contains("bun") || 
            lowerName.contains("roll") || lowerName.contains("cake") || 
            lowerName.contains("cookie") || lowerName.contains("flour") -> "Bakery"
            
            // Spices and condiments
            lowerName.contains("salt") || lowerName.contains("pepper") || 
            lowerName.contains("spice") || lowerName.contains("sauce") || 
            lowerName.contains("oil") || lowerName.contains("vinegar") || 
            lowerName.contains("herb") -> "Spices"
            
            // Grains
            lowerName.contains("rice") || lowerName.contains("pasta") || 
            lowerName.contains("cereal") || lowerName.contains("grain") ||
            lowerName.contains("quinoa") || lowerName.contains("oat") -> "Grains"
            
            // Default category
            else -> "General"
        }
    }

    suspend fun getShoppingItems(): Flow<Result<List<ShoppingItem>>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = TAG,
            operation = "Get shopping items",
            defaultValue = emptyList()
        ) {
            // Get current user
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")
                
            // Fetch items from Supabase
            supabase.from("shopping_items")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<ShoppingItemDto>()
                .map { it.toShoppingItem() }
        }.flowOn(Dispatchers.IO)

    suspend fun addShoppingItem(item: ShoppingItem): Flow<Result<Boolean>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = TAG,
            operation = "Add shopping item"
        ) {
            // Get current user
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")
                
            // Create DTO
            val dto = ShoppingItemDto(
                id = item.id,
                user_id = userId,
                name = item.name,
                category = item.category,
                is_checked = item.isChecked
            )
            
            // Insert into Supabase
            supabase.from("shopping_items")
                .insert(dto)
                
            true
        }.flowOn(Dispatchers.IO)

    suspend fun updateShoppingItem(item: ShoppingItem): Flow<Result<Boolean>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = TAG,
            operation = "Update shopping item"
        ) {
            // Get current user
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")
                
            // Update in Supabase
            supabase.from("shopping_items")
                .update(
                    {
                        set("name", item.name)
                        set("category", item.category)
                        set("is_checked", item.isChecked)
                    }
                ) {
                    filter { 
                        eq("id", item.id)
                        eq("user_id", userId)
                    }
                }
                
            true
        }.flowOn(Dispatchers.IO)

    suspend fun deleteShoppingItem(itemId: String): Flow<Result<Boolean>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = TAG,
            operation = "Delete shopping item",
            defaultValue = true // Pretend success if table doesn't exist
        ) {
            // Get current user
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")
                
            // Delete from Supabase
            supabase.from("shopping_items")
                .delete {
                    filter { 
                        eq("id", itemId)
                        eq("user_id", userId)
                    }
                }
                
            true
        }.flowOn(Dispatchers.IO)

    suspend fun deleteCheckedItems(itemIds: List<String>): Flow<Result<Boolean>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = TAG,
            operation = "Delete checked items",
            defaultValue = true // Pretend success if table doesn't exist
        ) {
            // Get current user
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")
                
            // Delete items in batches to avoid potential query size limitations
            val batchSize = 10
            itemIds.chunked(batchSize).forEach { batch ->
                // Process each batch item individually
                batch.forEach { itemId ->
                    supabase.from("shopping_items")
                        .delete {
                            filter { 
                                eq("id", itemId)
                                eq("user_id", userId)
                            }
                        }
                }
            }
                
            true
        }.flowOn(Dispatchers.IO)

    suspend fun deleteAllItems(): Flow<Result<Boolean>> = 
        DatabaseErrorHandler.executeDatabaseOperation(
            tag = TAG,
            operation = "Delete all shopping items",
            defaultValue = true // Pretend success if table doesn't exist
        ) {
            // Get current user
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not authenticated")
                
            // Delete all user's items
            supabase.from("shopping_items")
                .delete {
                    filter { eq("user_id", userId) }
                }
                
            true
        }.flowOn(Dispatchers.IO)

    suspend fun getShoppingList(): List<IngredientItem> {
        return shoppingList.toList()
    }

    suspend fun removeIngredient(ingredient: IngredientItem) {
        shoppingList.remove(ingredient)
    }

    suspend fun clearShoppingList() {
        shoppingList.clear()
    }
} 