package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.data.models.QuickAccessButton
import com.pentabytex.alshafimedledger.databinding.ItemQuickAccessButtonBinding


class QuickAccessAdapter( val action: (String) -> Unit) :
    ListAdapter<QuickAccessButton, QuickAccessAdapter.ViewHolder>(QuickAccessDiffCallback()) {

    inner class ViewHolder(private val binding: ItemQuickAccessButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QuickAccessButton) {
            binding.quickAccessIcon.setImageResource(item.iconRes)
            binding.quickAccessTitle.text = item.title
            binding.root.setOnClickListener { action.invoke(item.title) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuickAccessButtonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class QuickAccessDiffCallback : DiffUtil.ItemCallback<QuickAccessButton>() {
    override fun areItemsTheSame(oldItem: QuickAccessButton, newItem: QuickAccessButton): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: QuickAccessButton, newItem: QuickAccessButton): Boolean {
        return oldItem == newItem
    }
}
