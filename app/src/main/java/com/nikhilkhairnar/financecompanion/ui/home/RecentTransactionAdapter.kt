package com.nikhilkhairnar.financecompanion.ui.home


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikhilkhairnar.financecompanion.R
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.databinding.ItemRecentTransactionBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.utils.DateUtils


class RecentTransactionAdapter(
    private val onClick: (Transaction) -> Unit
) : ListAdapter<Transaction, RecentTransactionAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemRecentTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                // Category emoji
                tvEmoji.text = transaction.category.emoji

                // Title
                tvTitle.text = transaction.title

                // Date
                tvDate.text = DateUtils.formatDate(transaction.date)

                // Amount with color
                val isIncome = transaction.type == TransactionType.INCOME
                tvAmount.text = CurrencyUtils.formatWithSign(
                    transaction.amount,
                    isIncome
                )
                tvAmount.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        if (isIncome) R.color.income_green else R.color.expense_red
                    )
                )

                // Click listener
                root.setOnClickListener { onClick(transaction) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil — only redraws rows that actually changed
    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(old: Transaction, new: Transaction) =
            old.id == new.id

        override fun areContentsTheSame(old: Transaction, new: Transaction) =
            old == new
    }
}