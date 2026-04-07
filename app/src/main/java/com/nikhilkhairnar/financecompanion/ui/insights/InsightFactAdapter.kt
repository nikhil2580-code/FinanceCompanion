package com.nikhilkhairnar.financecompanion.ui.insights


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nikhilkhairnar.financecompanion.databinding.ItemInsghtFactBinding
import com.nikhilkhairnar.financecompanion.viewmodel.InsightFact

class InsightFactAdapter :
    ListAdapter<InsightFact, InsightFactAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemInsghtFactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fact: InsightFact) {
            binding.tvFactEmoji.text = fact.emoji
            binding.tvFactTitle.text = fact.title
            binding.tvFactValue.text = fact.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInsghtFactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<InsightFact>() {
        override fun areItemsTheSame(old: InsightFact, new: InsightFact) =
            old.title == new.title
        override fun areContentsTheSame(old: InsightFact, new: InsightFact) =
            old == new
    }
}