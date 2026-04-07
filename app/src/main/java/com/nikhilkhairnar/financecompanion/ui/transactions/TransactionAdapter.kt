package com.nikhilkhairnar.financecompanion.ui.transactions


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikhilkhairnar.financecompanion.R
import com.nikhilkhairnar.financecompanion.data.model.Transaction
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.databinding.ItemTransactionsBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.utils.DateUtils

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val onItemLongClick: (Transaction) -> Boolean
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemTransactionsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                tvEmoji.text = transaction.category.emoji
                tvTitle.text = transaction.title
                tvCategory.text = transaction.category.displayName
                tvDate.text = DateUtils.formatDate(transaction.date)

                val isIncome = transaction.type == TransactionType.INCOME

                tvAmount.text = CurrencyUtils.formatWithSign(
                    transaction.amount, isIncome
                )
                tvAmount.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        if (isIncome) R.color.income_green else R.color.expense_red
                    )
                )

                root.setOnClickListener { onItemClick(transaction) }
                root.setOnLongClickListener { onItemLongClick(transaction) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(old: Transaction, new: Transaction) =
            old.id == new.id
        override fun areContentsTheSame(old: Transaction, new: Transaction) =
            old == new
    }
}