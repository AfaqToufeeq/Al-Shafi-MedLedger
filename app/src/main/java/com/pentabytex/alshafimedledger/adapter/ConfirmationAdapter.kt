package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.databinding.ConfirmationChildLayoutBinding
import com.pentabytex.alshafimedledger.data.models.SaleItem

class ConfirmationAdapter : ListAdapter<SaleItem, ConfirmationAdapter.ConfirmationViewHolder>(
    SaleDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfirmationViewHolder {
        val binding = ConfirmationChildLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConfirmationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConfirmationViewHolder, position: Int) {
        val sale = getItem(position)
        holder.bind(sale)
    }

    inner class ConfirmationViewHolder(private val binding: ConfirmationChildLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(saleItem: SaleItem) {
            binding.tvMedicineName.text = saleItem.medicineName
            binding.tvQuantity.text = "x${saleItem.quantitySold}"
            binding.tvPrice.text = "Rs ${saleItem.totalSellingPrice}"
        }
    }
}

class SaleDiffCallback : DiffUtil.ItemCallback<SaleItem>() {
    override fun areItemsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
        return oldItem.medicineId == newItem.medicineId
    }

    override fun areContentsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
        return oldItem == newItem
    }
}
