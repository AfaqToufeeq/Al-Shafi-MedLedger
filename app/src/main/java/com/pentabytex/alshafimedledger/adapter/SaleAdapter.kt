package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.Sale
import com.pentabytex.alshafimedledger.databinding.ItemSaleBinding
import java.text.DateFormat
import java.util.Date

class SaleAdapter(
    private val onDeleteClick: (Sale) -> Unit,
    private val onItemClick: (Sale) -> Unit
) : ListAdapter<Sale, SaleAdapter.SaleViewHolder>(SaleDiffCallback()) {
    private val expandedItemIds = mutableSetOf<String>()

    inner class SaleViewHolder(val binding: ItemSaleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val sale = getItem(position)
        val isExpanded = expandedItemIds.contains(sale.saleId)
        holder.binding.apply {
            tvCustomerName.text = sale.customerName
            tvTimestamp.text = DateFormat.getDateTimeInstance().format(Date(sale.timestamp))
            tvNotes.text = sale.notes
            tvTotalCost.text = "Total: Rs. ${sale.totalPrice}"

            // Toggle visibility based on expansion state
            rvSaleItems.visibility = if (isExpanded) View.VISIBLE else View.GONE
            ivToggleDetails.setImageResource(
                if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )

            ivToggleDetails.setOnClickListener {
                val currentPosition = position
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val currentItem = getItem(currentPosition)
                    if (expandedItemIds.contains(currentItem.saleId)) {
                        expandedItemIds.remove(currentItem.saleId)
                    } else {
                        expandedItemIds.add(currentItem.saleId)
                    }
                    notifyItemChanged(currentPosition)
                }
            }

            ivDelete.setOnClickListener { onDeleteClick.invoke(sale) }
            root.setOnClickListener { onItemClick.invoke(sale) }

            // Set up child RecyclerView
            val saleItemAdapter = SaleItemAdapter()
            rvSaleItems.layoutManager = LinearLayoutManager(holder.itemView.context)
            rvSaleItems.adapter = saleItemAdapter
            saleItemAdapter.submitList(sale.saleItems)
        }
    }

    class SaleDiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale): Boolean {
            return oldItem.saleId == newItem.saleId
        }

        override fun areContentsTheSame(oldItem: Sale, newItem: Sale): Boolean {
            return oldItem == newItem
        }
    }
}
