package com.nikhilkhairnar.financecompanion.ui.goal


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikhilkhairnar.financecompanion.data.model.Goal
import com.nikhilkhairnar.financecompanion.databinding.ItemGoalBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.utils.DateUtils
import com.nikhilkhairnar.financecompanion.utils.animateTo
import kotlin.math.min

class GoalAdapter(
    private val onAddSavings: (Goal) -> Unit,
    private val onLongClick: (Goal) -> Unit
) : ListAdapter<Goal, GoalAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: Goal) {
            binding.apply {

                // Emoji + title
                tvGoalEmoji.text = goal.emoji
                tvGoalTitle.text = goal.title

                // Deadline
                if (goal.deadline != null) {
                    val daysLeft = ((goal.deadline - System.currentTimeMillis())
                            / (1000 * 60 * 60 * 24)).toInt()
                    tvDeadline.text = when {
                        daysLeft < 0  -> "Deadline passed"
                        daysLeft == 0 -> "Due today!"
                        daysLeft == 1 -> "1 day left"
                        else          -> "$daysLeft days left"
                    }
                    tvDeadline.setTextColor(
                        root.context.getColor(
                            when {
                                daysLeft < 0  -> com.nikhilkhairnar.financecompanion.R.color.expense_red
                                daysLeft <= 7 -> com.nikhilkhairnar.financecompanion.R.color.expense_red
                                else -> com.nikhilkhairnar.financecompanion.R.color.text_secondary
                            }
                        )
                    )
                    tvDeadline.visibility = View.VISIBLE
                } else {
                    tvDeadline.visibility = View.GONE
                }

                // Amount text
                tvSavedAmount.text =
                    "${CurrencyUtils.format(goal.savedAmount)} of ${CurrencyUtils.format(goal.targetAmount)}"

                // Progress
                val progress = if (goal.targetAmount > 0)
                    ((goal.savedAmount / goal.targetAmount) * 100).toInt()
                else 0
                val clamped = min(progress, 100)
                progressBar.animateTo(clamped)
                tvPercentage.text = "$clamped%"

                // Remaining
                val remaining = goal.targetAmount - goal.savedAmount
                tvRemaining.text = if (remaining > 0)
                    "${CurrencyUtils.format(remaining)} remaining"
                else
                    "Goal reached! 🎉"

                // Completed state
                if (goal.isCompleted) {
                    tvCompletedBadge.visibility = View.VISIBLE
                    btnAddSavings.visibility = View.GONE
                    progressBar.progressDrawable?.setTint(
                        root.context.getColor(com.nikhilkhairnar.financecompanion.R.color.income_green)
                    )
                } else {
                    tvCompletedBadge.visibility = View.GONE
                    btnAddSavings.visibility = View.VISIBLE
                    progressBar.progressDrawable?.setTint(
                        root.context.getColor(com.nikhilkhairnar.financecompanion.R.color.primary)
                    )
                }

                // Buttons
                btnAddSavings.setOnClickListener { onAddSavings(goal) }
                root.setOnLongClickListener {
                    onLongClick(goal)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(old: Goal, new: Goal) = old.id == new.id
        override fun areContentsTheSame(old: Goal, new: Goal) = old == new
    }
}