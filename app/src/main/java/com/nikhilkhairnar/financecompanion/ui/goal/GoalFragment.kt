package com.nikhilkhairnar.financecompanion.ui.goal


import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nikhilkhairnar.financecompanion.data.model.Goal
import com.nikhilkhairnar.financecompanion.databinding.FragmentGoalBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.utils.showIf
import com.nikhilkhairnar.financecompanion.viewmodel.GoalViewModel
import com.nikhilkhairnar.financecompanion.viewmodel.TransactionViewModel

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalBinding? = null
    private val binding get() = _binding!!

    private val goalViewModel: GoalViewModel by activityViewModels()
    private val transactionViewModel: TransactionViewModel by activityViewModels()
    private lateinit var adapter: GoalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeGoals()
        setupSmartAlert()
        setupFab()
    }

    // ─── RecyclerView ────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = GoalAdapter(
            onAddSavings = { goal -> showAddSavingsDialog(goal) },
            onLongClick  = { goal -> showDeleteGoalDialog(goal) }
        )
        binding.rvGoals.apply {
            this.adapter = this@GoalsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // ─── Observe Goals ───────────────────────────────────────────

    // Track previously completed goal ids
    private val previouslyCompleted = mutableSetOf<Int>()

    private fun observeGoals() {
        goalViewModel.allGoals.observe(viewLifecycleOwner) { goals ->
            adapter.submitList(goals)

            val activeCount    = goals.count { !it.isCompleted }
            val completedCount = goals.count { it.isCompleted }
            binding.tvGoalsSummary.text =
                "$activeCount active • $completedCount completed"

            binding.layoutEmpty.showIf(goals.isEmpty())
            binding.rvGoals.showIf(goals.isNotEmpty())

            // Check for newly completed goals
            goals.filter { it.isCompleted }.forEach { goal ->
                if (!previouslyCompleted.contains(goal.id)) {
                    previouslyCompleted.add(goal.id)
                    showGoalCompletedDialog(goal)
                }
            }
        }
    }

    private fun showGoalCompletedDialog(goal: Goal) {
        AlertDialog.Builder(requireContext())
            .setTitle("🎉 Goal Achieved!")
            .setMessage(
                "Congratulations! You've reached your \"${goal.title}\" goal of " +
                        "${CurrencyUtils.format(goal.targetAmount)}!"
            )
            .setPositiveButton("Awesome! 🎊", null)
            .show()
    }

    // ─── Smart Alert ─────────────────────────────────────────────
    // Shows a banner if user is spending too much vs their goals

    private fun setupSmartAlert() {
        transactionViewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            transactionViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
                val e = expense ?: 0.0
                val i = income ?: 0.0

                if (i > 0) {
                    val spendingRatio = e / i
                    when {
                        spendingRatio >= 0.9 -> showAlert(
                            "⚠️ You've spent ${(spendingRatio * 100).toInt()}% of your income. Your goals may be at risk!"
                        )
                        spendingRatio >= 0.7 -> showAlert(
                            "You're spending a lot this month. Consider saving more toward your goals."
                        )
                        else -> hideAlert()
                    }
                } else {
                    hideAlert()
                }
            }
        }
    }

    private fun showAlert(message: String) {
        binding.cardAlert.visibility = View.VISIBLE
        binding.tvAlertMessage.text = message
    }

    private fun hideAlert() {
        binding.cardAlert.visibility = View.GONE
    }

    // ─── FAB ─────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fabAddGoal.setOnClickListener {
            val bottomSheet = AddGoalBottomSheet { title, amount, deadline, emoji ->
                val goal = Goal(
                    title = title,
                    targetAmount = amount,
                    deadline = deadline,
                    emoji = emoji
                )
                goalViewModel.insert(goal)
            }
            bottomSheet.show(parentFragmentManager, "AddGoalBottomSheet")
        }
    }

    // ─── Add Savings Dialog ──────────────────────────────────────

    private fun showAddSavingsDialog(goal: Goal) {
        val remaining = goal.targetAmount - goal.savedAmount
        val input = EditText(requireContext()).apply {
            hint = "Amount to add (₹)"
            inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add to \"${goal.title}\"")
            .setMessage("Remaining: ${CurrencyUtils.format(remaining)}")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                when {
                    amount == null || amount <= 0 -> {
                        // invalid input — do nothing
                    }
                    amount > remaining -> {
                        // cap at remaining so it doesn't overshoot
                        goalViewModel.addToSavings(goal, remaining)
                    }
                    else -> {
                        goalViewModel.addToSavings(goal, amount)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─── Delete Dialog ───────────────────────────────────────────

    private fun showDeleteGoalDialog(goal: Goal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Delete \"${goal.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                goalViewModel.delete(goal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}