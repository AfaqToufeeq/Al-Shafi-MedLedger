package com.pentabytex.alshafimedledger.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.data.models.SaleItem
import com.pentabytex.alshafimedledger.databinding.ItemSaleMedicineBinding
import com.pentabytex.alshafimedledger.utils.RsFormatHelper

class SaleMedicineAdapter(
    private val onSaleUpdated: (position: Int, saleItem: SaleItem) -> Unit,
    private val onDeleteClick: (Medicine) -> Unit
    ) : ListAdapter<Medicine, SaleMedicineAdapter.SaleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SaleViewHolder(private val binding: ItemSaleMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var quantityWatcher: TextWatcher? = null
        private var priceWatcher: TextWatcher? = null

        fun bind(medicine: Medicine) = with(binding) {

            ivDelete.setOnClickListener { onDeleteClick(medicine) }

            val totalStock = medicine.totalStock.coerceAtLeast(1)
            val costPerUnit = medicine.purchasePrice / totalStock
            val remainingStock = (totalStock - medicine.soldStock).coerceAtLeast(0)
            val medName = "${medicine.name} ${medicine.type} • ${medicine.volume}"


            tvMedicineName.text = medName
            tvAvailableStock.text = "Available Stock: $remainingStock units"
            tvPurchasePricePerUnit.text = "Cost/unit: ${RsFormatHelper.formatPrice(costPerUnit, 2)}"
            tvTotalPurchasePrice.text = "Total Cost: ${RsFormatHelper.formatPrice(medicine.purchasePrice, 2)}"


            // Remove old watchers to prevent duplicated callbacks
            clearTextWatchers()

            // Reset inputs and summary
            etQuantity.setText("")
            etSellingPrice.setText("")

            // TextWatcher for quantity and price input fields
            val inputWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val quantity = etQuantity.text.toString().toIntOrNull() ?: 0
                    val sellPerUnit = etSellingPrice.text.toString().toDoubleOrNull() ?: 0.0

                    // Validate quantity input against stock
                    quantityInputLayout.error = if (quantity >= remainingStock) {
                        root.context.getString(
                            R.string.error_quantity_exceeds_stock, remainingStock
                        )
                    } else null

                    // Calculate totals and profit
                    val totalSelling = quantity * sellPerUnit
                    val profit = medicine.sellingPrice - medicine.purchasePrice

                   tvTotalSell.text ="Total Selling: ${RsFormatHelper.formatPrice(totalSelling, 0)}"
                    tvProfit.text = "Profit: ${RsFormatHelper.formatPrice(profit, 0)}"

                    val profitPerUnit = sellPerUnit - costPerUnit

                    val (profitLabel, profitDrawable) = when {
                        sellPerUnit > costPerUnit -> "Profit/unit" to R.drawable.ic_profit
                        sellPerUnit < costPerUnit -> "Loss/unit" to R.drawable.ic_loss
                        else -> "Break-Even" to R.drawable.ic_balance
                    }

                    tvProfitPerUnit.text = "Rs ${RsFormatHelper.formatPrice(profitPerUnit, 2)} $profitLabel"
                    tvProfitPerUnit.setCompoundDrawablesWithIntrinsicBounds(0, 0, profitDrawable, 0)
                    tvProfitPerUnit.compoundDrawablePadding = 8

                    val profitColor = if (sellPerUnit > costPerUnit) {
                        root.context.getColor(android.R.color.holo_green_dark)
                    } else {
                        root.context.getColor(android.R.color.holo_red_dark)
                    }

                    tvProfitPerUnit.setTextColor(profitColor)


                    // Notify listener of updated sale if position valid
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val saleItem = SaleItem(
                            medicineId = medicine.id,
                            medicineName = medName,
                            quantitySold = quantity,
                            sellingPricePerUnit = sellPerUnit,
                            purchasePricePerUnit = costPerUnit,
                            totalSellingPrice = totalSelling,
                            totalPurchaseCost = medicine.purchasePrice
                        )
                        onSaleUpdated(pos, saleItem)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            quantityWatcher = inputWatcher
            priceWatcher = inputWatcher

            etQuantity.addTextChangedListener(quantityWatcher)
            etSellingPrice.addTextChangedListener(priceWatcher)
        }

        private fun clearTextWatchers() = with(binding) {
            quantityWatcher?.let { etQuantity.removeTextChangedListener(it) }
            priceWatcher?.let { etSellingPrice.removeTextChangedListener(it) }
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Medicine>() {
            override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
                oldItem == newItem
        }
    }
}
