package com.nikhilkhairnar.financecompanion.ui.home

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.nikhilkhairnar.financecompanion.R
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.databinding.FragmentHomeBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by activityViewModels()
    private lateinit var recentAdapter: RecentTransactionAdapter
    private var balanceAnimator: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting()
        setupRecyclerView()
        setupPieChart()
        observeData()
        setupClickListeners()
    }

    // ─── Greeting ───────────────────────────────────────────────

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning 🌅"
            hour < 17 -> "Good Afternoon ☀️"
            else      -> "Good Evening 🌙"
        }
        binding.tvGreeting.text = greeting

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonth.text = monthFormat.format(Date())
    }

    // ─── RecyclerView ────────────────────────────────────────────

    private fun setupRecyclerView() {
        recentAdapter = RecentTransactionAdapter { transaction ->
            // Navigate to edit screen — Phase 5
        }
        binding.rvRecentTransactions.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    // ─── Pie Chart ───────────────────────────────────────────────

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 62f
            setHoleColor(Color.WHITE)
            setDrawCenterText(true)
            centerText = "Expenses"
            setCenterTextSize(14f)
            legend.isEnabled = true
            legend.textSize = 11f
            setEntryLabelTextSize(11f)
            setUsePercentValues(true)
            animateY(1000)
        }
    }

    private fun updatePieChart(transactions: List<Transaction>) {
        val expenses = transactions.filter {
            it.type == TransactionType.EXPENSE
        }

        if (expenses.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            binding.tvChartEmpty.visibility = View.VISIBLE
            return
        }

        binding.pieChart.visibility = View.VISIBLE
        binding.tvChartEmpty.visibility = View.GONE

        // Group by category and sum amounts
        val categoryTotals = expenses
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total.toFloat(), category.displayName)
        }

        val colors = listOf(
            Color.parseColor("#6C63FF"),
            Color.parseColor("#2ECC71"),
            Color.parseColor("#E74C3C"),
            Color.parseColor("#F39C12"),
            Color.parseColor("#3498DB"),
            Color.parseColor("#9B59B6"),
            Color.parseColor("#1ABC9C"),
            Color.parseColor("#E67E22"),
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 11f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(11f)
        }

        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
    }

    // ─── Observe LiveData ────────────────────────────────────────

    private fun observeData() {

        // Total income
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            val value = income ?: 0.0
            binding.tvTotalIncome.text = CurrencyUtils.format(value)
            updateBalance()
        }

        // Total expense
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            val value = expense ?: 0.0
            binding.tvTotalExpense.text = CurrencyUtils.format(value)
            updateBalance()
        }

        // Recent transactions list
        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            recentAdapter.submitList(transactions)    //

            // Show/hide empty state
            if (transactions.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvRecentTransactions.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvRecentTransactions.visibility = View.VISIBLE
            }
        }

        // All transactions for pie chart
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            updatePieChart(transactions)
        }
    }

    // Balance = Income - Expense
    private fun animateBalance(targetAmount: Double) {
        balanceAnimator?.cancel()
        balanceAnimator = ValueAnimator.ofFloat(0f, targetAmount.toFloat()).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val value = anim.animatedValue as Float
                _binding?.tvBalance?.text = CurrencyUtils.format(value.toDouble())
            }
            start()
        }
    }

    private fun updateBalance() {
        val income  = viewModel.totalIncome.value  ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        val balance = income - expense
        animateBalance(balance)
    }

    // ─── Click Listeners ─────────────────────────────────────────

    private fun setupClickListeners() {
        // "See all" → go to Transactions tab
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.transactionsFragment)
        }
    }

    // ─── Cleanup ─────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        balanceAnimator?.cancel()
        _binding = null
    }
}