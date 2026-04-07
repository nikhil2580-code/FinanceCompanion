package com.nikhilkhairnar.financecompanion.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val emoji: String = "🎯",
    val createdAt: Long = System.currentTimeMillis()
)