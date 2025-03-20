package com.thenewkenya.ingrediet.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateRecipeViewModel : ViewModel() {
    private val _recipeName = MutableStateFlow("")
    val recipeName: StateFlow<String> = _recipeName.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _cookingTime = MutableStateFlow("")
    val cookingTime: StateFlow<String> = _cookingTime.asStateFlow()

    private val _calories = MutableStateFlow("")
    val calories: StateFlow<String> = _calories.asStateFlow()

    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients: StateFlow<List<String>> = _ingredients.asStateFlow()

    private val _instructions = MutableStateFlow<List<String>>(emptyList())
    val instructions: StateFlow<List<String>> = _instructions.asStateFlow()

    fun updateRecipeName(name: String) {
        _recipeName.value = name
    }

    fun updateDescription(desc: String) {
        _description.value = desc
    }

    fun updateCookingTime(time: String) {
        _cookingTime.value = time
    }

    fun updateCalories(cal: String) {
        _calories.value = cal
    }

    fun addIngredient() {
        _ingredients.value = _ingredients.value + ""
    }

    fun removeIngredient(ingredient: String) {
        _ingredients.value = _ingredients.value - ingredient
    }

    fun addInstruction() {
        _instructions.value = _instructions.value + ""
    }

    fun removeInstruction(instruction: String) {
        _instructions.value = _instructions.value - instruction
    }

    fun saveRecipe() {
        viewModelScope.launch {
            // TODO: Implement recipe saving logic
            // This should:
            // 1. Validate all fields
            // 2. Create a Recipe object
            // 3. Save to the repository
            // 4. Navigate back to the previous screen
        }
    }
} 