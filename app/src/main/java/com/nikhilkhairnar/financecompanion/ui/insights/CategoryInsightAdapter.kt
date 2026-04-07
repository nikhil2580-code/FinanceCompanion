package com.nikhilkhairnar.financecompanion.ui.insights


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikhilkhairnar.financecompanion.databinding.ItemCatetgoryInsightBinding
import com.nikhilkhairnar.financecompanion.utils.CurrencyUtils
import com.nikhilkhairnar.financecompanion.viewmodel.CategoryInsight

class CategoryInsightAdapter :
    ListAdapter<CategoryInsight, CategoryInsightAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemCatetgoryInsightBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(insight: CategoryInsight) {
            binding.apply {
                tvCatEmoji.text      = insight.category.emoji
                tvCatName.text       = insight.category.displayName
                tvCatCount.text      = "${insight.transactionCount} transaction${
                    if (insight.transactionCount > 1) "s" else ""
                }"
                tvCatAmount.text     = CurrencyUtils.format(insight.totalAmount)
                tvCatPercentage.text = "${insight.percentage.toInt()}%"

                // Animate progress bar
                progressCategory.progress = insight.percentage.toInt()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCatetgoryInsightBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<CategoryInsight>() {
        override fun areItemsTheSame(old: CategoryInsight, new: CategoryInsight) =
            old.category == new.category
        override fun areContentsTheSame(old: CategoryInsight, new: CategoryInsight) =
            old == new
    }
}