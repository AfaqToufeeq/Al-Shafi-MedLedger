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
    private val onDeleteClick: (Medicine) -> Unit,
    private val onItemLongClick: (Medicine) -> Unit,
    private val onSelectionChanged: (Medicine, Boolean) -> Unit
) : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(DiffCallback) {

    private val expandedItemIds = mutableSetOf<String>()
    private val selectedItems = mutableSetOf<String>() // Track selected items

    private var isSelectionMode = false

    fun setSelectionMode(enable: Boolean, medicine: Medicine) {
        isSelectionMode = enable

        if (enable) {
            selectedItems.add(medicine.id)
        } else {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val item = getItem(position)
        val isExpanded = expandedItemIds.contains(item.id)
        val isItemSelected = selectedItems.contains(item.id)
        holder.bind(item, isExpanded, isItemSelected, isSelectionMode)
    }

    inner class MedicineViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine, isExpanded: Boolean, isSelected: Boolean, isSelectionMode: Boolean) = with(binding) {
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

            // Handle checkbox visibility and click
            // Show checkboxes only in selection mode
            cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            ivDelete.visibility = if (isSelectionMode) View.GONE else View.VISIBLE
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = isSelected

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                toggleSelection(medicine, isChecked)
            }

            root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(medicine, !isSelected)
                } else {
                    onItemClick(medicine)
                }
            }

            root.setOnLongClickListener {
                if (!isSelectionMode) {
                    onItemLongClick(medicine)
                }
                true
            }

            ivDelete.setOnClickListener { onDeleteClick(medicine) }

        }

        // Function to toggle selection state and notify
        private fun toggleSelection(medicine: Medicine, isChecked: Boolean) {
            if (isChecked) {
                selectedItems.add(medicine.id)
            } else {
                selectedItems.remove(medicine.id)
            }
            onSelectionChanged(medicine, isChecked)
            notifyItemChanged(adapterPosition)
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
