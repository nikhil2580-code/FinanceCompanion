package com.nikhilkhairnar.financecompanion.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { INCOME, EXPENSE }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val title: String,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)