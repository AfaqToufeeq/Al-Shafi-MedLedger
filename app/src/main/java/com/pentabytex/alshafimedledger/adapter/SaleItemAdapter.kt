package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.SaleItem
import com.pentabytex.alshafimedledger.databinding.ItemSaleItemBinding

class SaleItemAdapter : ListAdapter<SaleItem, SaleItemAdapter.SaleItemViewHolder>(SaleItemDiffCallback()) {

    inner class SaleItemViewHolder(val binding: ItemSaleItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleItemViewHolder {
        val binding = ItemSaleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            val profitPerUnit = item.sellingPricePerUnit - item.purchasePricePerUnit
            tvItemNumber.text = "${position + 1}."
            tvMedicineName.text = item.medicineName
            tvQuantity.text = "Qty: ${item.quantitySold}"
            tvUnitPrice.text = "Sell/unit: Rs. ${item.sellingPricePerUnit}"
            tvTotal.text = "Total: Rs. ${item.totalSellingPrice}"

            // Determine icon and color
            val (profitLabel, profitDrawable) = when {
                item.sellingPricePerUnit > item.purchasePricePerUnit -> "Profit/unit" to R.drawable.ic_profit
                item.sellingPricePerUnit < item.purchasePricePerUnit -> "Loss/unit" to R.drawable.ic_loss
                else -> "Break-Even" to R.drawable.ic_balance
            }

            tvProfit.text = "$profitLabel: Rs. ${profitPerUnit}"
            tvProfit.setCompoundDrawablesWithIntrinsicBounds(0, 0, profitDrawable, 0)
            tvProfit.compoundDrawablePadding = 8

            val profitColor = if (item.sellingPricePerUnit > item.purchasePricePerUnit) {
                root.context.getColor(android.R.color.holo_green_dark)
            } else {
                root.context.getColor(android.R.color.holo_red_dark)
            }

            tvProfit.setTextColor(profitColor)
        }
    }

    class SaleItemDiffCallback : DiffUtil.ItemCallback<SaleItem>() {
        override fun areItemsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
            return oldItem.medicineId == newItem.medicineId
        }

        override fun areContentsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
            return oldItem == newItem
        }
    }
}
