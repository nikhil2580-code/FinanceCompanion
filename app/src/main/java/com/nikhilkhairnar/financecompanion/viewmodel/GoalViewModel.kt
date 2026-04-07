package com.nikhilkhairnar.financecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikhilkhairnar.financecompanion.data.local.AppDatabase
import com.nikhilkhairnar.financecompanion.data.model.Goal
import com.nikhilkhairnar.financecompanion.data.repository.GoalRespository
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GoalRespository

    init {
        val dao = AppDatabase.getInstance(application).goalDao()
        repository = GoalRespository(dao)
    }

    val allGoals = repository.allGoals
    val activeGoals = repository.activeGoals

    fun insert(goal: Goal) = viewModelScope.launch {
        repository.insert(goal)
    }

    fun update(goal: Goal) = viewModelScope.launch {
        repository.update(goal)
    }

    fun delete(goal: Goal) = viewModelScope.launch {
        repository.delete(goal)
    }

    fun addToSavings(goal: Goal, amount: Double) = viewModelScope.launch {
        val newSavedAmount = goal.savedAmount + amount
        val updated = goal.copy(
            savedAmount = newSavedAmount,
            isCompleted = newSavedAmount >= goal.targetAmount
        )
        repository.update(updated)
    }
}
