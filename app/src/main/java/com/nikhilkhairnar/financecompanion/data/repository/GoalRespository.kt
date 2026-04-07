package com.nikhilkhairnar.financecompanion.data.repository

import com.nikhilkhairnar.financecompanion.data.local.GoalDao
import com.nikhilkhairnar.financecompanion.data.model.Goal

class GoalRespository(private val dao: GoalDao) {

    val allGoals = dao.getAllGoals()
    val activeGoals = dao.getActiveGoals() //Unresolved reference 'getActiveGoals'.


    suspend fun insert(goal: Goal) = dao.insertGoal(goal)
    suspend fun update(goal: Goal) = dao.updateGoal(goal)
    suspend fun delete(goal: Goal) = dao.deleteGoal(goal)
    suspend fun getById(id: Int) = dao.getGoalById(id)

}