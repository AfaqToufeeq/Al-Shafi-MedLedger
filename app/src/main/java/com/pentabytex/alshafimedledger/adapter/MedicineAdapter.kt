package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.ItemMedicineBinding

class MedicineAdapter(
    private val onItemClick: (Medicine) -> Unit,
    private val onDeleteClick: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(DiffCallback) {

    private val expandedItemIds = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val item = getItem(position)
        val isExpanded = expandedItemIds.contains(item.id)
        holder.bind(item, isExpanded)
    }

    inner class MedicineViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine, isExpanded: Boolean) = with(binding) {
            tvMedicineName.text = medicine.name
            tvTypeVolume.text = "${medicine.type} • ${medicine.volume}"
            tvQuantity.text = medicine.totalStock.toString()
            tvPurchasePrice.text = "Rs. %.2f".format(medicine.purchasePrice)
            tvSellingPrice.text = "Rs. %.2f".format(medicine.sellingPrice)

            val profit = medicine.sellingPrice - medicine.purchasePrice
            val profitPercentage = if (medicine.purchasePrice != 0.0) {
                (profit / medicine.purchasePrice) * 100
            } else {
                0.0
            }

            tvProfit.text = "Rs. %.2f".format(profit)
            tvProfitPercent.text = "%.1f%%".format(profitPercentage)

            // Toggle visibility based on expansion state
            detailsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            ivToggleDetails.setImageResource(
                if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )

            // Handle toggle click
            ivToggleDetails.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val currentItem = getItem(currentPosition)
                    if (expandedItemIds.contains(currentItem.id)) {
                        expandedItemIds.remove(currentItem.id)
                    } else {
                        expandedItemIds.add(currentItem.id)
                    }
                    notifyItemChanged(currentPosition)
                }
            }

            root.setOnClickListener { onItemClick(medicine) }
            ivDelete.setOnClickListener { onDeleteClick(medicine) }

        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Medicine>() {
            override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
                return oldItem == newItem
            }
        }
    }
}
