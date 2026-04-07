package com.nikhilkhairnar.financecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nikhilkhairnar.financecompanion.data.local.AppDatabase
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    val allTransactions: LiveData<List<Transaction>>
    val totalIncome: LiveData<Double?>
    val totalExpense: LiveData<Double?>
    val recentTransactions: LiveData<List<Transaction>>

    private val _searchQuery = MutableLiveData("")

    val searchResults: LiveData<List<Transaction>>

    val balance = MediatorLiveData<Double>()

    init {
        val dao = AppDatabase.getInstance(application).transactionDao()
        repository = TransactionRepository(dao)

        allTransactions = repository.allTransactions
        totalIncome = repository.totalIncome
        totalExpense = repository.totalExpense
        recentTransactions = repository.getRecentTransactions(5)

        searchResults = _searchQuery.switchMap { query ->
            if (query.isNullOrBlank()) repository.allTransactions
            else repository.search(query)
        }

        // Make balance reactive to income and expense changes
        balance.addSource(totalIncome) { calculateBalance() }
        balance.addSource(totalExpense) { calculateBalance() }
    }

    private fun calculateBalance() {
        val income = totalIncome.value ?: 0.0
        val expense = totalExpense.value ?: 0.0
        balance.value = income - expense
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}