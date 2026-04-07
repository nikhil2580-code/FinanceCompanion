package com.nikhilkhairnar.financecompanion.ui.insights

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nikhilkhairnar.financecompanion.R
import com.nikhilkhairnar.financecompanion.databinding.FragmentInsightsBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.viewmodel.InsightsViewModel
import com.nikhilkhairnar.financecompanion.viewmodel.WeeklyComparison
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    // Use viewModels() here — not activityViewModels — so it recomputes fresh
    private val viewModel: InsightsViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryInsightAdapter
    private lateinit var factAdapter: InsightFactAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupBarChart()
        setupCategoryRecyclerView()
        setupFactsRecyclerView()
        observeData()
    }

    // ─── Header ──────────────────────────────────────────────────

    private fun setupHeader() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = monthFormat.format(Date())
    }

    // ─── Bar Chart Setup ─────────────────────────────────────────

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled  = false
            legend.isEnabled       = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setTouchEnabled(false)
            setScaleEnabled(false)
            animateY(800)

            // X Axis
            xAxis.apply {
                position        = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity     = 1f
                textColor       = Color.parseColor("#6B7280")
                textSize        = 11f
            }

            // Left Y Axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor       = Color.parseColor("#F0F0F0")
                setDrawAxisLine(false)
                textColor       = Color.parseColor("#6B7280")
                textSize        = 10f
                valueFormatter  = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 1000) "₹${(value / 1000).toInt()}k"
                        else "₹${value.toInt()}"
                    }
                }
            }

            // Right Y Axis — hide it
            axisRight.isEnabled = false
        }
    }

    // ─── RecyclerViews ───────────────────────────────────────────

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryInsightAdapter()
        binding.rvCategories.apply {
            adapter       = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupFactsRecyclerView() {
        factAdapter = InsightFactAdapter()
        binding.rvFacts.apply {
            adapter       = factAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    // ─── Observe All Data ────────────────────────────────────────

    private fun observeData() {

        // Show/hide content based on whether transactions exist
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val hasData = transactions.isNotEmpty()
            binding.layoutEmpty.visibility   = if (hasData) View.GONE    else View.VISIBLE
            binding.layoutContent.visibility = if (hasData) View.VISIBLE else View.GONE
        }

        // Month comparison
        viewModel.weeklyComparison.observe(viewLifecycleOwner) { comparison ->
            comparison?.let { updateComparisonCard(it) }
        }

        // Bar chart
        viewModel.dailySpending.observe(viewLifecycleOwner) { dailyData ->
            if (dailyData.isEmpty()) return@observe

            val entries = dailyData.mapIndexed { index, day ->
                BarEntry(index.toFloat(), day.amount.toFloat())
            }
            val labels = dailyData.map { it.dayLabel }

            val dataSet = BarDataSet(entries, "Daily Spending").apply {
                color           = Color.parseColor("#6C63FF")
                valueTextColor  = Color.parseColor("#6B7280")
                valueTextSize   = 9f
                valueFormatter  = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) "" else "₹${value.toInt()}"
                    }
                }
            }

            binding.barChart.apply {
                data = BarData(dataSet).apply { barWidth = 0.6f }
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.labelCount     = labels.size
                invalidate()
            }
        }

        // Category breakdown
        viewModel.categoryInsights.observe(viewLifecycleOwner) { insights ->
            categoryAdapter.submitList(insights.take(5)) // show top 5
        }

        // Insight facts
        viewModel.insightFacts.observe(viewLifecycleOwner) { facts ->
            factAdapter.submitList(facts)
        }
    }

    // ─── Month Comparison Card ───────────────────────────────────

    private fun updateComparisonCard(comparison: WeeklyComparison) {
        binding.tvThisMonthAmount.text =
            CurrencyUtils.format(comparison.thisMonthExpense)
        binding.tvLastMonthAmount.text =
            CurrencyUtils.format(comparison.lastMonthExpense)

        val changeText = "${comparison.percentageChange.toInt()}%"
        binding.tvChangePercent.text = changeText

        if (comparison.isHigher) {
            binding.tvChangeArrow.text = "▲"
            binding.tvChangeArrow.setTextColor(
                requireContext().getColor(R.color.expense_red)
            )
            binding.tvComparisonSummary.text =
                "You spent ${comparison.percentageChange.toInt()}% more than last month"
        } else {
            binding.tvChangeArrow.text = "▼"
            binding.tvChangeArrow.setTextColor(
                requireContext().getColor(R.color.income_green)
            )
            binding.tvComparisonSummary.text =
                "Great! You spent ${comparison.percentageChange.toInt()}% less than last month 🎉"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}