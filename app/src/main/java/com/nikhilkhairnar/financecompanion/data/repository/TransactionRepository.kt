package com.nikhilkhairnar.financecompanion.data.repository

import com.nikhilkhairnar.financecompanion.data.local.TransactionDao
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType

class TransactionRepository(private val dao: TransactionDao) {

    val allTransactions = dao.getAllTransactions()
    val totalIncome = dao.getTotalIncome()
    val totalExpense = dao.getTotalExpense()

    fun getRecentTransactions(limit: Int = 5) =
        dao.getRecentTransactions(limit)

    fun getByType(type: TransactionType) =
        dao.getTransactionsByType(type)

    fun getByCategory(category: String) =
        dao.getTransactionsByCategory(category)

    fun search(query: String) =
        dao.searchTransactions(query)

    fun getByDateRange(startDate: Long, endDate: Long) =
        dao.getTransactionsBetweenDates(startDate, endDate)

    fun getAllExpenses() =
        dao.getAllExpenses()

    suspend fun insert(transaction: Transaction) =
        dao.insertTransaction(transaction)

    suspend fun update(transaction: Transaction) =
        dao.updateTransaction(transaction)

    suspend fun delete(transaction: Transaction) =
        dao.deleteTransaction(transaction)
}