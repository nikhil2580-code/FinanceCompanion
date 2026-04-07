package com.nikhilkhairnar.financecompanion.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.nikhilkhairnar.financecompanion.data.local.AppDatabase
import com.nikhilkhairnar.financecompanion.data.model.Category
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.data.repository.TransactionRepository
import java.util.Calendar

// ── Data classes for UI ──────────────────────────────────────────

data class CategoryInsight(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float,       // % of total expenses
    val transactionCount: Int
)

data class WeeklyComparison(
    val thisMonthExpense: Double,
    val lastMonthExpense: Double,
    val percentageChange: Double,  // positive = more spending, negative = less
    val isHigher: Boolean
)

data class DailySpending(
    val dayLabel: String,          // "Mon", "Tue" etc.
    val amount: Double
)

data class InsightFact(
    val emoji: String,
    val title: String,
    val value: String
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    // Raw data
    val allTransactions: LiveData<List<Transaction>>

    // ── Computed LiveData ────────────────────────────────────────

    val categoryInsights = MediatorLiveData<List<CategoryInsight>>()
    val weeklyComparison = MediatorLiveData<WeeklyComparison?>()
    val dailySpending    = MediatorLiveData<List<DailySpending>>()
    val insightFacts     = MediatorLiveData<List<InsightFact>>()

    // Selected month offset (0 = this month, -1 = last month etc.)
    private val _monthOffset = MutableLiveData(0)

    init {
        val dao = AppDatabase.getInstance(application).transactionDao()
        repository = TransactionRepository(dao)
        allTransactions = repository.allTransactions

        // Recompute whenever transactions change
        categoryInsights.addSource(allTransactions) { computeAll(it) }
        weeklyComparison.addSource(allTransactions) { computeAll(it) }
        dailySpending.addSource(allTransactions)    { computeAll(it) }
        insightFacts.addSource(allTransactions)     { computeAll(it) }
    }

    private fun computeAll(transactions: List<Transaction>) {
        computeCategoryInsights(transactions)
        computeWeeklyComparison(transactions)
        computeDailySpending(transactions)
        computeInsightFacts(transactions)
    }

    // ── Category Breakdown ───────────────────────────────────────

    private fun computeCategoryInsights(transactions: List<Transaction>) {
        val expenses = transactions.filter {
            it.type == TransactionType.EXPENSE && isThisMonth(it.date)
        }
        val totalExpense = expenses.sumOf { it.amount }

        if (totalExpense == 0.0) {
            categoryInsights.value = emptyList()
            return
        }

        val insights = expenses
            .groupBy { it.category }
            .map { (category, txns) ->
                val total = txns.sumOf { it.amount }
                CategoryInsight(
                    category         = category,
                    totalAmount      = total,
                    percentage       = ((total / totalExpense) * 100).toFloat(),
                    transactionCount = txns.size
                )
            }
            .sortedByDescending { it.totalAmount }

        categoryInsights.value = insights
    }

    // ── This Month vs Last Month ─────────────────────────────────

    private fun computeWeeklyComparison(transactions: List<Transaction>) {
        val thisMonthExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && isThisMonth(it.date) }
            .sumOf { it.amount }

        val lastMonthExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && isLastMonth(it.date) }
            .sumOf { it.amount }

        val change = if (lastMonthExpenses > 0) {
            ((thisMonthExpenses - lastMonthExpenses) / lastMonthExpenses) * 100
        } else 0.0

        weeklyComparison.value = WeeklyComparison(
            thisMonthExpense   = thisMonthExpenses,
            lastMonthExpense   = lastMonthExpenses,
            percentageChange   = Math.abs(change),
            isHigher           = thisMonthExpenses > lastMonthExpenses
        )
    }

    // ── Daily Spending This Week ─────────────────────────────────

    private fun computeDailySpending(transactions: List<Transaction>) {
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val cal = Calendar.getInstance()

        // Get start of current week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis

        val result = dayLabels.mapIndexed { index, label ->
            val dayStart = weekStart + (index * 24 * 60 * 60 * 1000L)
            val dayEnd   = dayStart  + (24 * 60 * 60 * 1000L) - 1

            val total = transactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                            it.date in dayStart..dayEnd
                }
                .sumOf { it.amount }

            DailySpending(dayLabel = label, amount = total)
        }

        dailySpending.value = result
    }

    // ── Insight Facts ────────────────────────────────────────────

    private fun computeInsightFacts(transactions: List<Transaction>) {
        val thisMonth = transactions.filter { isThisMonth(it.date) }
        val expenses  = thisMonth.filter { it.type == TransactionType.EXPENSE }
        val incomes   = thisMonth.filter { it.type == TransactionType.INCOME }

        val facts = mutableListOf<InsightFact>()

        // Highest single expense
        expenses.maxByOrNull { it.amount }?.let { top ->
            facts.add(
                InsightFact(
                    emoji = "🏆",
                    title = "Biggest expense",
                    value = "${top.title} — ${formatAmount(top.amount)}"
                )
            )
        }

        // Most frequent category
        expenses
            .groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.let { (category, txns) ->
                facts.add(
                    InsightFact(
                        emoji = category.emoji,
                        title = "Most frequent",
                        value = "${category.displayName} (${txns.size} times)"
                    )
                )
            }

        // Savings rate
        val totalIncome  = incomes.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        if (totalIncome > 0) {
            val savingsRate = ((totalIncome - totalExpense) / totalIncome * 100)
                .coerceIn(0.0, 100.0)
                .toInt()
            facts.add(
                InsightFact(
                    emoji = if (savingsRate >= 20) "💚" else "⚠️",
                    title = "Savings rate",
                    value = "$savingsRate% of income saved"
                )
            )
        }

        // Average daily spend
        if (expenses.isNotEmpty()) {
            val cal = Calendar.getInstance()
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val avgDaily = totalExpense / dayOfMonth
            facts.add(
                InsightFact(
                    emoji = "📅",
                    title = "Avg. daily spend",
                    value = formatAmount(avgDaily)
                )
            )
        }

        // No spend days this week
        val dailyData = dailySpending.value ?: emptyList()
        val noSpendDays = dailyData.count { it.amount == 0.0 }
        if (noSpendDays > 0) {
            facts.add(
                InsightFact(
                    emoji = "🎉",
                    title = "No-spend days this week",
                    value = "$noSpendDays day${if (noSpendDays > 1) "s" else ""}"
                )
            )
        }

        insightFacts.value = facts
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun isThisMonth(timestamp: Long): Boolean {
        val txCal  = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance()
        return txCal.get(Calendar.YEAR)  == nowCal.get(Calendar.YEAR) &&
                txCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
    }

    private fun isLastMonth(timestamp: Long): Boolean {
        val txCal  = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        return txCal.get(Calendar.YEAR)  == nowCal.get(Calendar.YEAR) &&
                txCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
    }

    private fun formatAmount(amount: Double): String {
        return com.nikhilkhairnar.financecompanion.utils.CurrencyUtils.format(amount)
    }
}